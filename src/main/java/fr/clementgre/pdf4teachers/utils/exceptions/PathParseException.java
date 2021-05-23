package fr.clementgre.pdf4teachers.utils.exceptions;

import org.apache.batik.parser.ParseException;

public class PathParseException extends Exception{
    
    private ParseException batikException;
    
    public PathParseException(ParseException batikException){
        super(batikException.getMessage(), batikException.getCause());
        this.batikException = batikException;
    }
    
    public ParseException getBatikException(){
        return batikException;
    }
    
    public String getMessage(){
        return "Unable to parse vector path [Error message: " + batikException.getMessage() + "]";
    }
}
