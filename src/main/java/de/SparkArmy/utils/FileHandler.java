package de.SparkArmy.utils;

import de.SparkArmy.controller.LoggingMarker;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class FileHandler {

    public static final File userDirectory = new File(System.getProperty("user.dir"));
    private static final Logger logger = Util.logger;

    public static boolean createDirectory(String path, String directoryName) {
        File newDirectory = new File(path + "/" + directoryName);
        if (newDirectory.exists()) return true;
        return newDirectory.mkdirs();
    }

    public static boolean createDirectory(String directoryName) {
        return createDirectory(userDirectory.getAbsolutePath(),directoryName);
    }

    public static boolean createFile(@NotNull File path, String fileName) {
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

    public static @NotNull File getDirectoryInUserDirectory(String directoryName) {
        if (createDirectory(directoryName)) {
            return getFile(userDirectory.getAbsolutePath() + "/" + directoryName);
        }
        throw new RuntimeException("Directory can't be null");
    }

    @Contract("_, _ -> new")
    public static @NotNull File getFileInDirectory(@NotNull File directory, String filename) {
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

    public static void writeValuesInFile(String path, Object value) {
        try {
            FileWriter fileWriter = fileWriter(getFile(path));
            if (null == fileWriter) return;
            fileWriter.write(String.valueOf(value));
            fileWriter.close();
        } catch (IOException ignored) {
        }
    }

    public static @Nullable String getFileContent(@NotNull File file) {
        try {
            return Files.readString(Path.of(file.getAbsolutePath()));
        } catch (IOException e) {
            logger.error("Can't read the File content from " + file.getAbsolutePath());
            return null;
        }
    }

    public static @Nullable String getFileContent(@NotNull File path, String filename) {
        File file = new File(path.getAbsolutePath() + "/" + filename);
        try {
            return Files.readString(Path.of(file.getAbsolutePath()));
        } catch (IOException e) {
            logger.error(LoggingMarker.FILHANDLER,"Can't read the File content from " + file.getAbsolutePath());
            return null;
        }
    }

    public static @Nullable String getFileContent(String path) {
        File file = new File(path);
        try {
            return Files.readString(Path.of(file.getAbsolutePath()));
        } catch (IOException e) {
            logger.error(LoggingMarker.FILHANDLER,"Can't read the File content from " + file.getAbsolutePath());
            return null;
        }
    }

    public static @Nullable List<File> getFilesInDirectory(File path){
        if (null == path){
            logger.info(LoggingMarker.FILHANDLER,"FILEHANDLER: The path is null");
            return null;
        }

        if (path.isFile()){
            logger.info(LoggingMarker.FILHANDLER,"FILEHANDLER: The path is a file");
            return null;
        }
        if (null == path.listFiles()){
            logger.info(LoggingMarker.FILHANDLER,"FILEHANDLER: Error to listFiles");
            return null;
        }
        return Arrays.stream(Objects.requireNonNull(path.listFiles())).toList();
    }

    @Contract("_ -> new")
    private static @NotNull File getFile(String path) {
        return new File(path);
    }

    private static @Nullable FileWriter fileWriter(File path) {
        try {
            return new FileWriter(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(LoggingMarker.FILHANDLER,"Can't create a file-writer");
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
