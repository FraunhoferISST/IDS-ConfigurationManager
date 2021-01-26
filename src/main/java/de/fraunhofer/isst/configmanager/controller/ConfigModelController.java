package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;

/**
 * The controller class implements the ConfigModelApi and offers the possibilities to manage
 * the configuration model in the configurationmanager.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "ConfigModel Management", description = "Endpoints for managing the configuration model")
public class ConfigModelController implements ConfigModelApi {

    private final static Logger logger = LoggerFactory.getLogger(ResourceUIController.class);

    private final Serializer serializer;
    private final ConfigModelService configModelService;

    @Autowired
    public ConfigModelController(Serializer serializer, ConfigModelService configModelService) {
        this.serializer = serializer;
        this.configModelService = configModelService;
    }

    /**
     * This method creates a configuration model with the given parameters.
     *
     * @param loglevel            loglevel of the configuration model
     * @param connectorStatus     connector status of the configuration model
     * @param connectorDeployMode connector deploy mode of the configuration model
     * @param trustStore          trustStore of the configuration model
     * @param keyStore            keyStore of the configuration model
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createConfigModel(String loglevel, String connectorStatus, String connectorDeployMode,
                                                    String trustStore, String keyStore) {

        ConfigurationModel configurationModel = configModelService.createConfigModel(loglevel, connectorStatus,
                connectorDeployMode, trustStore, keyStore);
        if (configurationModel != null) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Successfully created a new configuration model with the id: " +
                    configurationModel.getId()));
        } else {
            return ResponseEntity.badRequest().body("Could not create configuration model");
        }
    }

    /**
     * This method updates the configuration model with the given parameters.
     *
     * @param loglevel            loglevel of the configuration model
     * @param connectorStatus     connector status of the configuration model
     * @param connectorDeployMode connector deploy mode of the configuration model
     * @param trustStore          trustStore of the configuration model
     * @param keyStore            keyStore of the configuration model
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateConfigModel(String loglevel, String connectorStatus, String connectorDeployMode,
                                                    String trustStore, String keyStore) {

        var result = configModelService.updateConfigurationModel(loglevel, connectorStatus,
                connectorDeployMode, trustStore, keyStore);
        if (result) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Successfully updated the configuration model with the id: "
                    + configModelService.getConfigModel().getId().toString()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Connector did not accent the new Configuration!");
        }
    }

    /**
     * This method returns the current configuration model.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConfigModel() {
        try {
            return ResponseEntity.ok(serializer.serialize(configModelService.getConfigModel()));
        } catch (IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("Could not determine the configuration model");
        }
    }

    /**
     * This method returns the current configuration model as JSON string
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConfigModelJson() {

        ConfigurationModel configurationModel = configModelService.getConfigModel();

        JSONObject configModelJson = new JSONObject();
        configModelJson.put("loglevel", configurationModel.getConfigurationModelLogLevel());
        configModelJson.put("connectorStatus", configurationModel.getConnectorStatus());
        configModelJson.put("connectorDeployMode", configurationModel.getConnectorDeployMode());
        configModelJson.put("trustStore", configurationModel.getTrustStore().toString());
        configModelJson.put("keyStore", configurationModel.getKeyStore().toString());

        return ResponseEntity.ok(configModelJson.toJSONString());
    }

    /**
     * This method deletes a configuration model with the given id.
     *
     * @param configmodelId id of the configuration model
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteConfigModel(URI configmodelId) {

        if (configModelService.deleteConfigModel(configmodelId)) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "ConfigModel with ID: " + configmodelId + " is deleted"));
        } else {
            return ResponseEntity.badRequest().body("Could not delete the configuration model with ID: " + configmodelId);
        }
    }
}