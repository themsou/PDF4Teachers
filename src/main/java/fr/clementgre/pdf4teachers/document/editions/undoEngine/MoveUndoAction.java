package fr.clementgre.pdf4teachers.document.editions.undoEngine;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;

import java.lang.ref.WeakReference;

public class MoveUndoAction extends UndoAction{
    
    protected final WeakReference<Element> element;
    protected int realX;
    protected int realY;
    private int page;
    
    public MoveUndoAction(UType undoType, Element element){
        super(undoType);
        this.element = new WeakReference<>(element);
        this.realX = element.getRealX();
        this.realY = element.getRealY();
        this.page = element.getPageNumber();
    }
    
    @Override
    public boolean undoAndInvert(){
        Element element = this.element.get();
        if(element != null){
            
            int oldRealX = element.getRealX();
            int oldRealY = element.getRealY();
            int oldPage = element.getPageNumber();
        
            element.setRealX(realX);
            element.setRealY(realY);
            if(oldPage != page){
                element.switchPage(page);
            }
            
            // invert
            realX = oldRealX;
            realY = oldRealY;
            page = oldPage;
            return true;
        }
        
        return false;
    }
}