package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.control.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImageGridView extends ShapesGridView<ImageGridElement>{
    
    private final int imageRenderSize;
    private final boolean hasContextMenu;
    
    public ImageGridView(boolean defineCellSizeAsRowNumber, int imageRenderSize, Slider zoomSlider, boolean contextmenu){
        super(defineCellSizeAsRowNumber, zoomSlider);
        this.imageRenderSize = imageRenderSize;
        this.hasContextMenu = contextmenu;
    }
    
    @Override
    protected void setup(){
        setCellFactory(param -> new ImageGridCell(hasContextMenu));
        super.setup();
    }
    
    
    @Override
    protected void sort(String sortType, boolean order){
        int multiple = (order ? 1 : -1);
        if(SORT_FILE_EDIT_TIME.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareTimeWith(o2) * multiple);
        }else if(SORT_USE.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareUseWith(o2) * multiple);
        }else if(SORT_LAST_USE.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareLastUseTimeWith(o2) * multiple);
        }else if(SORT_NAME.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareNameWith(o2) * multiple);
        }else if(SORT_SIZE.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareSizeWith(o2) * multiple);
        }else{ // SORT_FOLDER (Default)
            getItems().sort((o1, o2) -> o1.compareDirectoryWith(o2) * multiple);
        }
    }
    
    @Override
    protected List<ImageGridElement> filter(List<ImageGridElement> items){
        if(TR.tr("galleryWindow.filterAndEditCombo.everywhere").equals(filterType)){
            return items;
        }else if(TR.tr("galleryWindow.filterAndEditCombo.favourites").equals(filterType)){
            return items.stream().filter(ImageGridElement::isFavorite).collect(Collectors.toList());
        }else{
            return items.stream().filter((e) -> e.getImageIdDirectory().equals(filterType)).collect(Collectors.toList());
        }
    }
    
    public void editImages(List<ImageGridElement> newImagesList){
        List<ImageGridElement> actualImages = getAllItems();
    
        // Remove images that are not anymore into the list
        List<ImageGridElement> toRemove = new ArrayList<>();
        
        for(ImageGridElement image : actualImages){
            if(!newImagesList.contains(image)) toRemove.add(image);
        }
        removeItems(toRemove);
        
        // Add images that was added to the list
        List<ImageGridElement> toAdd = new ArrayList<>();
        
        for(ImageGridElement image : newImagesList){
            if(!actualImages.contains(image)) toAdd.add(image);
        }
        addItems(toAdd);
    }
    
    @Override
    public void resetUseData(){
        for(ImageGridElement element : getAllItems()){
            element.resetUseData();
        }
        getSortManager().simulateCall();
    }
    
    public int getImageRenderSize(){
        return imageRenderSize;
    }
}
