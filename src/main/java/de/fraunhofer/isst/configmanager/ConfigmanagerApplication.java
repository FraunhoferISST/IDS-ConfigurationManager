/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Main class for starting the configuration manager application.
 */

@Slf4j
@EnableScheduling
@SpringBootApplication
public class ConfigmanagerApplication {
    public static final String CURRENT_VERSION = "7.1.0";

    public static void main(final String[] args) {
        if (log.isInfoEnabled()) {
            log.info("---- [ConfigmanagerApplication] Starting ConfigManager " + CURRENT_VERSION);
            log.info("---- [ConfigmanagerApplication] Used JVM charset (should be UTF-8): " + Charset.defaultCharset());
        }

        SpringApplication.run(ConfigmanagerApplication.class, args);
    }

    /**
     * This method creates for the open api a custom description, which fits with the
     * configuration manager.
     *
     * @return OpenAPi
     */
    @Bean
    public OpenAPI customOpenAPI() throws IOException {
        final var properties = new Properties();

        try (var inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            // This function may crash (e.g. ill-formatted file). Let it bubble up.
            properties.load(inputStream);
        }

        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title(properties.getProperty("title"))
                        .description(properties.getProperty("project_desc"))
                        .version(properties.getProperty("version"))
                        .contact(new Contact()
                                .name(properties.getProperty("organization_name"))
                        )
                        .license(new License()
                                .name(properties.getProperty("licence"))
                                .url(properties.getProperty("licence_url")))
                );
    }


    /**
     * This method creates a serializer bean to autowire the serializer in other places.
     *
     * @return infomodel serializer as bean for autowiring
     */
    @Bean
    public Serializer getSerializer() {
        return new Serializer();
    }

    /**
     * This method creates a object mapper bean to autowire it in other places.
     *
     * @return Object mapper as bean for autowiring
     */
    @Bean
    public ObjectMapper getObjectMapper() {
        final var objectMapper = new ObjectMapper();
        final var ptv = BasicPolymorphicTypeValidator.builder().build();

        objectMapper.activateDefaultTyping(ptv);

        return objectMapper;
    }

    @Scheduled(fixedRate = 60_000, initialDelay = 30_000)
    public void logInfoStillAlive() {
        final var mb = 1024L * 1024L;
        final var currentHeapSize = Runtime.getRuntime().totalMemory() / mb;
        final var maxHeapSize = Runtime.getRuntime().maxMemory() / mb;
        final var freeHeapSize = Runtime.getRuntime().freeMemory() / mb;
        final var threadCount = Thread.activeCount();

        System.gc(); //Called manually as a precaution, so that the GC is eventually executed

        if (log.isInfoEnabled()) {
            log.info("[ConfigManager " + CURRENT_VERSION + "] Heap Size Stats: Used " + Math.toIntExact(currentHeapSize) + " MB - Free " + Math.toIntExact(freeHeapSize) + " MB - Max " + Math.toIntExact(maxHeapSize) + " MB - Running Threads: " + threadCount);
            log.info("[ConfigManager " + CURRENT_VERSION + "] Waiting for API call...");
        }
    }
}
