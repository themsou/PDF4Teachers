package fr.themsou.document.editions.elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import fr.themsou.yaml.Config;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Font;

public class TextElement extends Text implements Element {

	private IntegerProperty realX = new SimpleIntegerProperty();
	private IntegerProperty realY = new SimpleIntegerProperty();

	ContextMenu menu = new ContextMenu();

	private int pageNumber;
	private int shiftX = 0;
	private int shiftY = 0;

	public TextElement(int x, int y, Font font, String text, Color color, int pageNumber, PageRenderer page) {

		this.pageNumber = pageNumber;
		this.realX.set(x);
		this.realY.set(y);

		setFont(font);
		setText(text);
		setFill(color);

		setBoundsType(TextBoundsType.LOGICAL);
		setTextOrigin(VPos.BASELINE);

		if(page == null) return;

		setCursor(Cursor.MOVE);
		layoutXProperty().bind(page.widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
		layoutYProperty().bind(page.heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));

		checkLocation(getLayoutX(), getLayoutY());
		textProperty().addListener((observable, oldValue, newValue) -> {
			checkLocation(getLayoutX(), getLayoutY());
		});

		// enable shadow if this element is selected
		MainWindow.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if(oldValue == this && newValue != this){
				setEffect(null);
				menu.hide();
			}else if(oldValue != this && newValue == this){
				DropShadow ds = new DropShadow();
				ds.setOffsetY(3.0f);
				ds.setColor(Color.color(0f, 0f, 0f));
				setEffect(ds);
				setCache(true);
				requestFocus();
			}
		});

		// MENU

		NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Supprimer"), -1, false);
		item1.setAccelerator("Suppr");
		item1.setToolTip(TR.tr("Supprime cet élément. Il sera donc retiré de l'édition."));
		NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Dupliquer"), -1, false);
		item2.setToolTip(TR.tr("Crée un second élément identique à celui-ci."));
		NodeMenuItem item3 = new NodeMenuItem(new HBox(), TR.tr("Ajouter aux éléments précédents"), -1, false);
		item3.setToolTip(TR.tr("Ajoute cet élément à la liste des éléments précédents."));
		NodeMenuItem item4 = new NodeMenuItem(new HBox(), TR.tr("Ajouter aux éléments Favoris"), -1, false);
		item4.setToolTip(TR.tr("Ajoute cet élément à la liste des éléments favoris."));
		menu.getItems().addAll(item1, item2, item4, item3);
		Builders.setMenuSize(menu);

		item1.setOnAction(e -> delete());
		item2.setOnAction(e -> {
			PageRenderer page1 = MainWindow.mainScreen.document.pages.get(0);
			if (MainWindow.mainScreen.document.getCurrentPage() != -1)
				page1 = MainWindow.mainScreen.document.pages.get(MainWindow.mainScreen.document.getCurrentPage());

			TextElement realElement = (TextElement) this.clone();
			realElement.setRealX(realElement.getRealX() + 10);
			realElement.setRealY(realElement.getRealY() + 10);
			page1.addElement(realElement, true);
			MainWindow.mainScreen.selectedProperty().setValue(realElement);
		});
		item3.setOnAction(e -> MainWindow.lbTextTab.addSavedElement(this.toNoDisplayTextElement(TextTreeItem.LAST_TYPE, true)));
		item4.setOnAction(e -> MainWindow.lbTextTab.addSavedElement(this.toNoDisplayTextElement(TextTreeItem.FAVORITE_TYPE, true)));

		// MOUSE EVENT

		setOnMousePressed(e -> {
			e.consume();

			shiftX = (int) e.getX();
			shiftY = (int) e.getY();
			menu.hide();
			select();

			if(e.getButton() == MouseButton.SECONDARY){
				menu.show(getPage(), e.getScreenX(), e.getScreenY());
			}
		});

		setOnMouseReleased(e -> {
			Edition.setUnsave();
			double itemX = getLayoutX() + e.getX() - shiftX;
			double itemY = getLayoutY() + e.getY() - shiftY;

			checkSwitchLocation(itemX, itemY);

			PageRenderer newPage = MainWindow.mainScreen.document.getPreciseMouseCurrentPage();
			if(newPage != null){
				if(newPage.getPage() != getPageNumber()){
					MainWindow.mainScreen.setSelected(null);

					switchPage(newPage.getPage());
					itemY = newPage.getPreciseMouseY() - shiftY;
					checkSwitchLocation(itemX, itemY);

					layoutXProperty().bind(getPage().widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
					layoutYProperty().bind(getPage().heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));

					MainWindow.mainScreen.setSelected(this);
				}
			}

			checkLocation(getLayoutX(), getLayoutY());
			MainWindow.lbTextTab.onFileTextSortManager.simulateCall();

			if(Main.DEBUG) Tooltip.install(this, new Tooltip("p: " + getPageNumber() + "\nx: " + getRealX() + "\ny: " + getRealY()));
		});
		setOnMouseDragged(e -> {
			double itemX = getLayoutX() + e.getX() - shiftX;
			double itemY = getLayoutY() + e.getY() - shiftY;

			checkSwitchLocation(itemX, itemY);
		});
	}

	// CHECK LOCATION

	@Override
	public void checkLocation(double itemX, double itemY){

		setBoundsType(TextBoundsType.VISUAL);
		double linesHeight = getLayoutBounds().getHeight();
		double lineHeight = getBaselineOffset();
		double bottomLinesHeight = linesHeight - lineHeight;

		if(itemY < lineHeight) itemY = lineHeight;
		if(itemY > getPage().getHeight()-bottomLinesHeight) itemY = getPage().getHeight()-bottomLinesHeight;

		if(itemX < 0) itemX = 0;
		if(itemX > getPage().getWidth() - getLayoutBounds().getWidth()) itemX = getPage().getWidth() - getLayoutBounds().getWidth();

		setBoundsType(TextBoundsType.LOGICAL);

		realX.set((int) (itemX / getPage().getWidth() * Element.GRID_WIDTH));
		realY.set((int) (itemY / getPage().getHeight() * Element.GRID_HEIGHT));

	}
	@Override
	public void checkSwitchLocation(double itemX, double itemY){


		setBoundsType(TextBoundsType.VISUAL);
		double linesHeight = getLayoutBounds().getHeight();
		double lineHeight = getBaselineOffset();
		double bottomLinesHeight = linesHeight - lineHeight;

		if(getPageNumber() == 0) if(itemY < lineHeight) itemY = lineHeight;
		if(getPageNumber() == MainWindow.mainScreen.document.totalPages-1) if(itemY > getPage().getHeight()-bottomLinesHeight) itemY = getPage().getHeight()-bottomLinesHeight;

		if(itemX < 0) itemX = 0;
		if(itemX > getPage().getWidth() - getLayoutBounds().getWidth()) itemX = getPage().getWidth() - getLayoutBounds().getWidth();

		setBoundsType(TextBoundsType.LOGICAL);

		realX.set((int) (itemX / getPage().getWidth() * Element.GRID_WIDTH));
		realY.set((int) (itemY / getPage().getHeight() * Element.GRID_HEIGHT));
	}

	// SELECT - DELETE - SWITCH PAGE

	@Override
	public void select() {

		MainWindow.leftBar.getSelectionModel().select(1);
		MainWindow.mainScreen.setSelected(this);
		MainWindow.lbTextTab.selectItem();
		toFront();
		getPage().toFront();
	}
	@Override
	public void delete() {
		if(getPage() != null){
			getPage().removeElement(this, true);
		}
	}
	@Override
	public void switchPage(int page){
		getPage().switchElementPage(this, MainWindow.mainScreen.document.pages.get(page));
	}

	// READER AND WRITERS

	public LinkedHashMap<Object, Object> getYAMLData(){
		LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
		data.put("x", getRealX());
		data.put("y", getRealY());
		data.put("color", getFill().toString());
		data.put("font", getFont().getFamily());
		data.put("size", getFont().getSize());
		data.put("bold", Element.getFontWeight(getFont()) == FontWeight.BOLD);
		data.put("italic", Element.getFontPosture(getFont()) == FontPosture.ITALIC);
		data.put("text", getText());

		return data;
	}
	public static TextElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage, int page){

		int x = (int) Config.getLong(data, "x");
		int y = (int) Config.getLong(data, "y");
		double fontSize = Config.getDouble(data, "size");
		boolean isBold = Config.getBoolean(data, "bold");
		boolean isItalic = Config.getBoolean(data, "italic");
		String fontName = Config.getString(data, "font");
		Color color = Color.valueOf(Config.getString(data, "color"));
		String text = Config.getString(data, "text");

		Font font = Element.getFont(fontName, isBold, isItalic, (int) fontSize);
		return new TextElement(x, y, font, text, color, page, hasPage ? MainWindow.mainScreen.document.pages.get(page) : null);
	}

	public static TextElement readDataAndGive(DataInputStream reader, boolean hasPage) throws IOException {

		byte page = reader.readByte();
		short x = reader.readShort();
		short y = reader.readShort();
		double fontSize = reader.readFloat();
		boolean isBold = reader.readBoolean();
		boolean isItalic = reader.readBoolean();
		String fontName = reader.readUTF();
		short colorRed = (short) (reader.readByte() + 128);
		short colorGreen = (short) (reader.readByte() + 128);
		short colorBlue = (short) (reader.readByte() + 128);
		String text = reader.readUTF();

		Font font = Element.getFont(fontName, isItalic, isBold, (int) fontSize);
		return new TextElement(x, y, font, text, Color.rgb(colorRed, colorGreen, colorBlue), page, hasPage ? MainWindow.mainScreen.document.pages.get(page) : null);

	}
	public static void readDataAndCreate(DataInputStream reader) throws IOException {
		TextElement element = readDataAndGive(reader, true);
		if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
			MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElementSimple(element);
	}
	public static void readYAMLDataAndCreate(HashMap<String, Object> data, int page){
		TextElement element = readYAMLDataAndGive(data, true, page);
		if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
			MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElementSimple(element);
	}

	// COORDINATES GETTERS AND SETTERS

	@Override
	public int getRealX() {
		return realX.get();
	}
	@Override
	public IntegerProperty RealXProperty() {
		return realX;
	}
	@Override
	public void setRealX(int x) {
		this.realX.set(x);
	}
	@Override
	public int getRealY() {
		return realY.get();
	}
	@Override
	public IntegerProperty RealYProperty() {
		return realY;
	}
	@Override
	public void setRealY(int y) {
		this.realY.set(y);
	}

	// PAGE GETTERS ANS SETTERS

	@Override
	public PageRenderer getPage() {
		if(MainWindow.mainScreen.document == null) return null;
		if(MainWindow.mainScreen.document.pages.size() > pageNumber){
			return MainWindow.mainScreen.document.pages.get(pageNumber);
		}
		return null;
	}
	@Override
	public int getPageNumber() {
		return pageNumber;
	}
	@Override
	public void setPage(PageRenderer page) {
		this.pageNumber = page.getPage();
	}
	@Override
	public void setPage(int pageNumber){
		this.pageNumber = pageNumber;
	}

	// TRANSFORMATIONS

	@Override
	public Element clone() {
		return new TextElement(getRealX(), getRealY(), getFont(), getText(), (Color) getFill(), pageNumber, getPage());
	}
	public TextTreeItem toNoDisplayTextElement(int type, boolean hasCore){
		if(hasCore) return new TextTreeItem(getFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000, this);
		else return new TextTreeItem(getFont(), getText(), (Color) getFill(), type, 0, System.currentTimeMillis()/1000);
	}

}