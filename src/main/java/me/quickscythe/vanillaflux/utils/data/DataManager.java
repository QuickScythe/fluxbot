package me.quickscythe.vanillaflux.utils.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DataManager {

    public static String getFileContents(File file) {
        try {
            if (!file.exists()) throw new RuntimeException("File does not exist (" + file.getName() + ")");

            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            return stringBuilder.toString();
        } catch (IOException ex) {
            return null;
        }
    }
}
