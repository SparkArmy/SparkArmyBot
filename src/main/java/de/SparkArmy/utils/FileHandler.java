package de.SparkArmy.utils;

import java.io.File;
import java.io.IOException;

public class FileHandler {

    public static final File userDirectory = new File(System.getProperty("user.dir"));

    public static boolean createDirectory(String path,String directoryName){
        File newDirectory = new File(path + "/" + directoryName);
        if (newDirectory.exists()) return true;
        return newDirectory.mkdirs();
    }

    public static boolean createDirectory(String directoryName){
        File newDirectory = new File(userDirectory.getAbsolutePath() + "/" + directoryName);
        if (newDirectory.exists()) return true;
        return newDirectory.mkdirs();
    }

    public static boolean createFile(File path, String fileName){
        try {
            return new File(path.getAbsolutePath() + "/" + fileName).createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    public static File getDirectory(String path){
        return new File(path);
    }

    public static File getDirectoryInUserDirectory(String directoryName){
        return new File(userDirectory.getAbsolutePath() + "/" + directoryName);
    }
}
