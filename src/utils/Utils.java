package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Utils {
    /**
     * Delete local file if exists
     * @params filename
     */
    public static void deleteLocalFile(String filename){
        File file = new File(filename);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
