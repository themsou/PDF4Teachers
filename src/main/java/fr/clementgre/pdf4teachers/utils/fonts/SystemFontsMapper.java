package fr.clementgre.pdf4teachers.utils.fonts;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Originally created by azakhary on 4/24/2015
 * see https://github.com/UnderwaterApps/overlap2d/blob/master/overlap2d/src/com/uwsoft/editor/proxy/FontManager.java
 * (Apache 2.0 license)
 * Edited by Clement Grennerat
 */
public class SystemFontsMapper{

    //              Family, Paths
    private final HashMap<String, FontPaths> systemFontMap = new HashMap<>();
    
    public static String[] getSystemFontNames() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }
    
    private String[] getSystemFontsDirs() {
        String[] result;
        if (Main.isWindows()){
            result = new String[2];
            result[0] = System.getenv("WINDIR") + File.separator + "Fonts";
            result[1] = System.getenv("LOCALAPPDATA") + File.separator + "Microsoft\\Windows\\Fonts";
            return result;
            
        }else if (Main.isOSX()){
            result = new String[3];
            result[0] = System.getProperty("user.home") + File.separator + "Library/Fonts";
            result[1] = "/Library/Fonts";
            result[2] = "/System/Library/Fonts";
            return result;
            
        }else if (Main.isLinux()){
            String[] pathsToCheck = {
                    System.getProperty("user.home") + File.separator + ".fonts",
                    "/usr/share/fonts/truetype",
                    "/usr/share/fonts/TTF"
            };
            ArrayList<String> resultList = new ArrayList<>();
            
            for (int i = pathsToCheck.length - 1; i >= 0; i--) {
                String path = pathsToCheck[i];
                File tmp = new File(path);
                if (tmp.exists() && tmp.isDirectory() && tmp.canRead()) {
                    resultList.add(path);
                }
            }
            
            if(resultList.isEmpty()){
                MainWindow.showNotification(AlertIconType.WARNING, "Error: unable to load system fonts, no directory is readable", 30);
                result = new String[0];
            }else{
                result = new String[resultList.size()];
                result = resultList.toArray(result);
            }
            return result;
        }
        return null;
    }
    public List<File> getAllSystemFontFiles() {
        // only retrieving ttf files
        String[] extensions = new String[]{"ttf", "otf", "ttc"};
        String[] paths = getSystemFontsDirs();
        ArrayList<File> files = new ArrayList<>();
    
        for(String path : paths){
            File fontDirectory = new File(path);
            if(fontDirectory.exists()) files.addAll(FilesUtils.listFiles(fontDirectory, extensions, true));
        }
        return files;
    }
    
    public void loadFontsFromSystemFiles(){
        String[] systemFonts = getSystemFontNames();
        System.out.println("Indexing system fonts... (" + systemFonts.length + " fonts)");
        
        new Thread(() -> {
            long time = System.currentTimeMillis();
            int pdfBoxErrors = 0;
            
            systemFontMap.clear();
            for(File file : getAllSystemFontFiles()){
                try{
                    Font[] fonts = Font.loadFonts(new FileInputStream(file.getAbsolutePath()), -1);
                    if(fonts == null) continue;
                    for(Font font : fonts){
                        if(StringUtils.contains(systemFonts, font.getFamily())){
                            if(!addFontToMap(font, file.getAbsolutePath())){
                                pdfBoxErrors++; break;
                            }
                        }
                    }
                }catch(IOException e){ e.printStackTrace(); }
            }
            
            double ping = (System.currentTimeMillis() - time) / 1000d;
            System.out.println("Loaded " + systemFontMap.size() + "/" + systemFonts.length + " fonts in " + ping + "s (" + pdfBoxErrors + " unable to load due to PDFBox restrictions, usually missing tables)");
            
            Platform.runLater(FontUtils::fontsUpdated);
        }, "System fonts loader").start();
    }
    public void loadFontsFromCache(List<FontPaths> fontPathss){
        for(FontPaths fontPaths : fontPathss){
            systemFontMap.put(fontPaths.getName(), fontPaths);
        }
        Platform.runLater(FontUtils::fontsUpdated);
    }
    
    private boolean addFontToMap(Font font, String path){
        
        // Check if PDFBox is able to load the font, otherwise, cancel the add.
        PDDocument doc = new PDDocument();
        try{
            PDType0Font.load(doc, new FileInputStream(path));
            doc.close();
        }catch(IOException e){
            try{
                doc.close();
            }catch(IOException ioException){ ioException.printStackTrace(); }
            return false;
        }
    
        FontPaths paths;
        if(systemFontMap.containsKey(font.getFamily())){
            paths = systemFontMap.get(font.getFamily());
        }else{
            paths = new FontPaths(font.getFamily());
        }
        paths.addPathAuto(path, FontUtils.getFontWeight(font, false), FontUtils.getFontPosture(font).equals(FontPosture.ITALIC));
        systemFontMap.put(font.getFamily(), paths);
        return true;
    }
    
    public FontPaths getFontPathsFromName(String fontFamily) {
        return systemFontMap.get(fontFamily);
    }
    public HashMap<String, FontPaths> getFonts() {
        return systemFontMap;
    }
    public boolean hasFont(String fontFamily) {
        return systemFontMap.containsKey(fontFamily);
    }
}
