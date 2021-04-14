package eu.ec.dgempl.eessi.rina.tool.migration.common.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Wrapper of class {@link Gson}
 */
public class GsonWrapper {
    private static final Logger logger = LoggerFactory.getLogger(GsonWrapper.class);

    private GsonWrapper() {
    }

    /**
     * Method for reading the contents of a json file in the classpath
     * 
     * @param path
     *            relative path to the json (must be visible to the classloader)
     * @param type
     *            the type of object into which the json will be mapped
     * @param <T>
     * @return the mapped object
     * @throws FileNotFoundException
     */
    public static <T> T loadFromClasspathResource(String path, Class<T> type) throws FileNotFoundException {
        PreconditionsHelper.notEmpty(path, "path");

        // load resource
        InputStream is = GsonWrapper.class.getClassLoader().getResourceAsStream(path);

        // check if resource exists
        if (is == null) {
            logger.info("Could not find the mapping in the specified path [path={}]", path);
            return null;
        }

        // init gson
        Gson gson = new Gson();

        // return the contents of the json
        return gson.fromJson(new InputStreamReader(is), type);
    }

    /**
     * Method for reading the contents of a json file on the disk
     *
     * @param path
     *            path to the json
     * @param type
     *            the type of object into which the json will be mapped
     * @param <T>
     * @return the mapped object
     * @throws FileNotFoundException
     */
    public static <T> T loadFromDisk(String path, Class<T> type) throws IOException {
        PreconditionsHelper.notEmpty(path, "path");

        try (InputStream is = Files.newInputStream(Paths.get(path))) {
            // init gson
            Gson gson = new Gson();

            // return the contents of the json
            return gson.fromJson(new InputStreamReader(is), type);
        } catch (NoSuchFileException ex) {
            logger.info("Could not find the mapping in the specified path [path={}]", path);
            return null;
        }
    }

    /**
     * Method for mapping an object to a json then write it on the disk
     * 
     * @param object
     *            the object to be converted into json
     * @param path
     *            the path of the file where the json will be written
     * @throws IOException
     */
    public static void writeToFile(Object object, String path) throws IOException {
        PreconditionsHelper.notNull(object, "object");
        PreconditionsHelper.notEmpty(path, "path");

        // create Gson instance; disable escaping, otherwise '=' will be encoded as '\u003d'
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        Path filePath = Paths.get(path);

        File directory = filePath.getParent().toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // create a writer
        Writer writer = Files.newBufferedWriter(filePath);

        // convert report to JSON file
        gson.toJson(object, writer);

        // close writer
        writer.close();
    }

    /**
     * Method for stringifying json object
     *
     * @param object
     *            the object to be converted into json
     * @return
     */
    public static String stringify(Object object) {
        if (object == null) {
            return null;
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        return gson.toJson(object);
    }
}
