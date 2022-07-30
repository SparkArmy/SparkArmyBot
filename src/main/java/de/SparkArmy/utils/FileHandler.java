package de.SparkArmy.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class FileHandler {

    private static final Logger logger = MainUtil.logger;
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

    public static boolean createFile(String path, String fileName){
        try {
            return new File(path + "/" + fileName).createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    public static File getDirectoryInUserDirectory(String directoryName){
        if (createDirectory(directoryName)){
            return getFile(userDirectory.getAbsolutePath() + "/" + directoryName);
        }
        return null;
    }

    public static File getFileInDirectory(File directory,String filename){
        return getFile(directory.getAbsolutePath() + "/" + filename);
    }

    public static boolean writeValuesInFile(File path,String filename,Object value){

        try {
            FileWriter fileWriter = fileWriter(getFileInDirectory(path,filename));
            if (fileWriter == null) return false;
            fileWriter.write(String.valueOf(value));
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getFileContent(File file){
        try {
            return new String(Files.readAllBytes(Path.of(file.getAbsolutePath())));
        } catch (IOException e) {
            logger.severe("Can't read the File content from " + file.getAbsolutePath());
            return null;
        }
    }

    public static String getFileContent(File path,String filename){
        File file = new File(path.getAbsolutePath() + "/" + filename);
        try {
            return new String(Files.readAllBytes(Path.of(file.getAbsolutePath())));
        } catch (IOException e) {
            logger.severe("Can't read the File content from " + file.getAbsolutePath());
            return null;
        }
    }

    public static String getFileContent(String path){
        File file = new File(path);
        try {
            return new String(Files.readAllBytes(Path.of(file.getAbsolutePath())));
        } catch (IOException e) {
            logger.severe("Can't read the File content from " + file.getAbsolutePath());
            return null;
        }
    }

    private static File getFile(String path){
        return new File(path);
    }

    private static FileWriter fileWriter(File path){
        try {
            return new FileWriter(path);
        } catch (IOException e) {
            logger.severe("Can't create a file-writer");
            return null;
        }
    }

/*
* Exit-Codes
* 10 - Can't create Directory
* 11 - Can't read a Directory
* 12 - Can't read a File
* */


}
