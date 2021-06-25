package de.fraunhofer.isst.configmanager.util.camel;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * Component for deploying and deleting Camel routes in the file system.
 */
@Slf4j
@Component
@NoArgsConstructor
public class RouteFileHelper {
    /**
     * Path for the Camel route files.
     */
    @Value("${camel.xml-routes.directory}")
    private String filePath;

    /**
     * Writes a given string to a file in the directory specified in application.properties. Creates
     * the file if it does not exist.
     *
     * @param fileName the filename
     * @param content the content to write to the file
     * @throws IOException if the file cannot be created or written
     */
    public void writeToFile(final String fileName, final String content) throws IOException {
        final var file = new File(filePath + File.separator +  fileName);

        try (var fileWriter = new FileWriter(file);
             var bufferedWriter = new BufferedWriter(fileWriter)) {

            if (log.isErrorEnabled() && !file.exists() && !file.createNewFile()) {
                log.error("Could not create file '{}{}{}'", filePath, File.separator, fileName);
            }

            bufferedWriter.write(content);

            if (log.isInfoEnabled()) {
                log.info("Successfully created file '{}{}{}'.", filePath, File.separator, fileName);
            }

        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Cannot write to file '{}{}{}' because an IO error occurred: {}",
                        filePath, File.separator, fileName, e.toString());
            }

            throw e;
        }
    }

    /**
     * Deletes a file with a given name in the directory specified in application.properties.
     *
     * @param name the filename
     * @throws IOException if the file cannot be deleted
     */
    public void deleteFile(final String name) throws IOException {
        final var file = Paths.get(filePath + name);
        if (Files.exists(file)) {
            try {
                Files.delete(file);

                if (log.isInfoEnabled()) {
                    log.info("Successfully deleted file '{}{}{}'.", filePath, File.separator, name);
                }
            } catch (NoSuchFileException e) {
                if (log.isErrorEnabled()) {
                    log.error("Cannot delete file '{}{}{}' because file does not exist.", filePath,
                            File.separator, name, e);
                }
                throw e;
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Cannot delete file '{}{}{}' because an IO error occurred.", filePath,
                            File.separator, name, e);
                }
                throw e;
            }
        }
    }
}
