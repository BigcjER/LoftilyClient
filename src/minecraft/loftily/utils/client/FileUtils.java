package loftily.utils.client;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Objects;

public class FileUtils {
    public static String readInputStream(final InputStream inputStream) {
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    
    public static void unpackFile(File file, String name) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(Objects.requireNonNull(FileUtils.class.getClassLoader().getResourceAsStream(name)), fos);
        fos.close();
    }
}
