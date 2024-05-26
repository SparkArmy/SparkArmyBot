package de.sparkarmy.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public class FileHandler {

    private static final File userDirectory = new File(System.getProperty("user.dir"));

    public static boolean createDirectory(String path, String directoryName) {
        File newDirectory = new File(path + "/" + directoryName);
        if (newDirectory.exists()) return true;
        return newDirectory.mkdirs();
    }

    public static boolean createDirectory(String directoryName) {
        return createDirectory(userDirectory.getAbsolutePath(),directoryName);
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

    @Contract("_ -> new")
    private static @NotNull File getFile(String path) {
        return new File(path);
    }





    /*
     * Exit-Codes
     * 10 - Can't create Directory
     * 11 - Can't read a Directory
     * 12 - Can't read a File
     * */


}
