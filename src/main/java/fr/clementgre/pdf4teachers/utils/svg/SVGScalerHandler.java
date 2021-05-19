package fr.clementgre.pdf4teachers.utils.svg;

public class SVGScalerHandler extends SVGSimpleTransformHandler{
    
    private float scaleX, scaleY, translateX, translateY;
    
    // Translate Y/X should be relative to original coordinates
    // Width and Height too.
    public SVGScalerHandler(float scaleX, float scaleY, float translateX, float translateY){
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.translateX = translateX;
        this.translateY = translateY;
    }
    public SVGScalerHandler(float scaleX, float scaleY, float translateX, float translateY,
                            boolean invertX, boolean invertY, float width, float height){
        
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.translateX = translateX;
        this.translateY = translateY;
        
        if(invertX){
            this.translateX -= width;
            this.scaleX = -this.scaleX;
        }
        if(invertY){
            this.translateY -= height;
            this.scaleY = -this.scaleY;
        }
    }
    
    protected float manageX(float x, boolean rel){
        if(rel) return x * scaleX;
        else return (x+translateX) * scaleX;
    }
    protected float manageY(float y, boolean rel){
        if(rel) return y * scaleY;
        else return (y+translateY) * scaleY;
    }
}
