package fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections;

import fr.clementgre.pdf4teachers.panel.sidebar.texts.ListsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;

public class TextTreeFavorites extends TextTreeSection {

    public ListsManager listsManager;

    public TextTreeFavorites(){
        super(TR.tr("Éléments Favoris"), FAVORITE_TYPE);

        listsManager = new ListsManager(this);
        setupGraphics();
    }

    @Override
    public void setupSortManager(){
        sortManager.setup(sortCell.pane, TR.tr("Ajout"),
                TR.tr("Ajout"), TR.tr("Nom"), TR.tr("Utilisation"),
                "\n",
                TR.tr("Police"), TR.tr("Taille"), TR.tr("Couleur"));
    }

    @Override
    public void setupGraphics(){
        super.setupGraphics();

        pane.getChildren().add(pane.getChildren().size()-1, listsManager.saveListBtn);
        pane.getChildren().add(pane.getChildren().size()-1, listsManager.loadListBtn);
    }

    @Override
    public void updateGraphics(){
        super.updateGraphics();
        listsManager.updateGraphics();
    }
}