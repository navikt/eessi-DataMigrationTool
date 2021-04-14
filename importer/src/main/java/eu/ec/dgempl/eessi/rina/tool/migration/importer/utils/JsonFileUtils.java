package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;

public class JsonFileUtils {

    /**
     * Returns a string content of the given document in a UTF-8 charset.
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String loadDocument(final String filePath, EElasticType eElasticType) throws Exception {

        Assert.isTrue(StringUtils.isNotBlank(filePath), "'filePath' cannot be blank!");
        Assert.notNull(eElasticType, "'elasticType' cannot be null!");

        return FileUtils.readFileToString(new File(filePath + getFileName(eElasticType)), StandardCharsets.UTF_8.name());

    }

    public static void writeToFile(String object, String path, EElasticType eElasticType) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(path + getFileName(eElasticType)))) {
            writer.write(object);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileName(EElasticType eElasticType) {
        return eElasticType.getIndex() + "_" + eElasticType.getType() + ".json";
    }
}
