package de.SparkArmy.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class FileHandler {

    public static final File userDirectory = new File(System.getProperty("user.dir"));
    private static final Logger logger = MainUtil.logger;

    public static boolean createDirectory(String path, String directoryName) {
        File newDirectory = new File(path + "/" + directoryName);
        if (newDirectory.exists()) return true;
        return newDirectory.mkdirs();
    }

    public static boolean createDirectory(String directoryName) {
        return createDirectory(userDirectory.getAbsolutePath(),directoryName);
    }

    public static boolean createFile(File path, String fileName) {
        try {
            return new File(path.getAbsolutePath() + "/" + fileName).createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean createFile(String path, String fileName){
        try {
            return new File(path + "/" + fileName).createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    public static File getDirectoryInUserDirectory(String directoryName) {
        if (createDirectory(directoryName)) {
            return getFile(userDirectory.getAbsolutePath() + "/" + directoryName);
        }
        return null;
    }

    public static File getFileInDirectory(File directory, String filename) {
        return getFile(directory.getAbsolutePath() + "/" + filename);
    }

    public static boolean writeValuesInFile(File path, String filename, Object value) {
        try {
            FileWriter fileWriter = FileHandler.fileWriter(getFileInDirectory(path, filename));
            if (null == fileWriter) return false;
            fileWriter.write(String.valueOf(value));
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean writeValuesInFile(File file, Object value){
        try {
        FileWriter fileWriter = fileWriter(file);
        if (null == fileWriter) return false;
        fileWriter.write(String.valueOf(value));
        fileWriter.close();
        return true;
    } catch (IOException e) {
        return false;
    }
    }

    public static boolean writeValuesInFile(String path, Object value){
        try {
            FileWriter fileWriter = fileWriter(getFile(path));
            if (null == fileWriter) return false;
            fileWriter.write(String.valueOf(value));
            fileWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getFileContent(File file) {
        try {
            return Files.readString(Path.of(file.getAbsolutePath()));
        } catch (IOException e) {
            logger.severe("Can't read the File content from " + file.getAbsolutePath());
            return null;
        }
    }

    public static String getFileContent(File path, String filename) {
        File file = new File(path.getAbsolutePath() + "/" + filename);
        try {
            return Files.readString(Path.of(file.getAbsolutePath()));
        } catch (IOException e) {
            logger.severe("Can't read the File content from " + file.getAbsolutePath());
            return null;
        }
    }

    public static String getFileContent(String path) {
        File file = new File(path);
        try {
            return Files.readString(Path.of(file.getAbsolutePath()));
        } catch (IOException e) {
            FileHandler.logger.severe("Can't read the File content from " + file.getAbsolutePath());
            return null;
        }
    }

    public static List<File> getFilesInDirectory(File path){
        if (null == path){
            FileHandler.logger.info("FILEHANDLER: The path is null");
            return null;
        }

        if (path.isFile()){
            FileHandler.logger.info("FILEHANDLER: The path is a file");
            return null;
        }
        if (null == path.listFiles()){
            FileHandler.logger.info("FILEHANDLER: Error to listFiles");
            return null;
        }
        return Arrays.stream(Objects.requireNonNull(path.listFiles())).toList();
    }

    private static File getFile(String path) {
        return new File(path);
    }

    private static FileWriter fileWriter(File path) {
        try {
            return new FileWriter(path, StandardCharsets.UTF_8);
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
