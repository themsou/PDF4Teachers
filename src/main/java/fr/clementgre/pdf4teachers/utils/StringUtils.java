package fr.clementgre.pdf4teachers.utils;

import java.util.Arrays;

public class StringUtils {

    public static String removeBefore(String string, String rejex){
        if(rejex.isEmpty()) return string;
        int index = string.indexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(index + rejex.length());

        return "";
    }
    public static String removeBeforeNotEscaped(String string, String rejex){

        int fromIndex = 0;
        while(true){

            int index = string.indexOf(rejex, fromIndex);
            if(index == -1) return string;

            if(!string.startsWith("\\", index-1)){
                if(index < string.length()) return string.substring(index + rejex.length());
                return "";
            }else{
                fromIndex = index + 1;
            }

        }
    }
    public static String removeBeforeLastRejex(String string, String rejex){
        if(rejex.isEmpty()) return string;
        int index = string.lastIndexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(index + rejex.length());

        return "";
    }
    public static String removeAfterLastRejex(String string, String rejex){
        if(rejex.isEmpty()) return string;
        int index = string.lastIndexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);

        return "";
    }
    public static String removeAfterLastRejexIgnoringCase(String string, String rejex){
        if(rejex.isEmpty()) return string;
        int index = string.toLowerCase().lastIndexOf(rejex.toLowerCase());

        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);

        return "";
    }
    public static String removeAfter(String string, String rejex){
        if(rejex.isEmpty()) return "";
        int index = string.indexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);

        return "";
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }


    public static Double getDouble(String text){
        try{
            return Double.parseDouble(text);
        }catch (NumberFormatException e){
            return null;
        }
    }
    public static Integer getInt(String text){
        try{
            return Integer.parseInt(text);
        }catch (NumberFormatException e){
            return null;
        }
    }
    public static int getAlwaysInt(String text){
        try{
            return Integer.parseInt(text);
        }catch (NumberFormatException e){
            return 0;
        }
    }
    public static long getAlwaysLong(String text){
        try{
            return Long.parseLong(text);
        }catch (NumberFormatException e){
            return 0;
        }
    }
    public static double getAlwaysDouble(String text){
        try{
            return Double.parseDouble(text);
        }catch (NumberFormatException e){
            return 0;
        }
    }


    public static String[] cleanArray(String[] array) {
        return Arrays.stream(array).filter(x -> !x.isBlank()).toArray(String[]::new);
    }
}
