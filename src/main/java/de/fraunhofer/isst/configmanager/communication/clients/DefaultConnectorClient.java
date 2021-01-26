package de.fraunhofer.isst.configmanager.communication.clients;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.ResourceRepresentation;
import de.fraunhofer.isst.configmanager.configmanagement.service.listeners.ConfigModelListener;

import java.io.IOException;
import java.net.URI;

/**
 * The interface DefaultConnectorClient defines methods that are implemented to make
 * configurations for the dataspace connector.
 * The implementations of the ConfigManager are oriented according to the structure of the dataspace connectors.
 */
public interface DefaultConnectorClient extends ConfigModelListener {

    /**
     * The method helps to update connector in the broker. For this only the id of the corresponding broker
     * is necessary.
     *
     * @param brokerURI URI of the broker to update/register
     * @return Response of the update/register request of the connector
     * @throws IOException when sending the request fails
     */
    String updateAtBroker(String brokerURI) throws IOException;

    /**
     * The method removes the connector from the corresponding broker. For this only the id of the broker is necessary.
     *
     * @param brokerURI URI of the broker to unregister
     * @return Response of the unregister request of the connector
     * @throws IOException when sending the request fails
     */
    String unregisterAtBroker(String brokerURI) throws IOException;

    /**
     * The boolean method helps to send the current configuration model to the target connector.
     *
     * @param configurationModel current configuration model that is sent to the target Connector
     * @return true if connector accepted configuration
     * @throws IOException when request cannot be sent
     */
    boolean sendConfiguration(String configurationModel) throws IOException;

    /**
     * The method returns the current configuration model.
     *
     * @return the current configuration model
     * @throws IOException if request fails
     */
    ConfigurationModel getConfiguration() throws IOException;

    /**
     * Send a Resource update Request to a target Connector
     *
     * @param resourceID ID of the Resource that will be created
     * @param resource   Resource to create
     * @return Response of the target Connector
     * @throws IOException when serializing of the Resource, or sending of the request fails
     */
    String updateResource(URI resourceID, Resource resource) throws IOException;

    /**
     * Send a Resource update request to a target broker.
     *
     * @param resourceID ID of the Resource that will be created
     * @param resource   Resource to create
     * @param brokerUri  URI of the Broker
     * @return Response of the target Connector
     * @throws IOException when serializing of the Resource, or sending of the request fails
     */
    String updateResourceAtBroker(URI resourceID, Resource resource, String brokerUri) throws IOException;

    /**
     * Send a resource creation request to a target connector.
     *
     * @param resource Resource that will be created
     * @return Response of the target Connector
     * @throws IOException when serializing of the Resource, or sending of the request fails
     */
    String registerResource(Resource resource) throws IOException;

    /**
     * Send a resource deletion request to a target connector.
     *
     * @param resourceID ID of the Resource to delete
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String deleteResource(URI resourceID) throws IOException;

    /**
     * Send a resource deletion request to a target broker.
     *
     * @param resourceID ID of the Resource to delete
     * @param brokerUri  URI of the Broker
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String deleteResourceAtBroker(URI resourceID, String brokerUri) throws IOException;

    /**
     * Send a resource representation deletion request to a connector.
     *
     * @param resourceID       ID of the Resource for which the representation is deleted
     * @param representationID ID of the Representation to delete
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String deleteResourceRepresentation(String resourceID, String representationID) throws IOException;

    /**
     * Send a resource representation creation request to the connector.
     *
     * @param resourceID     ID of the Resource for which the representation is registered
     * @param representation representation to be registered
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String registerResourceRepresentation(String resourceID, Representation representation) throws IOException;

    /**
     * Send a resource representation update request to a connector.
     *
     * @param resourceID       ID of the Resource for which the representation is updated
     * @param representationID ID of the representation to be updated
     * @param representation   representation to be updated
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String updateResourceRepresentation(String resourceID, String representationID, Representation representation)
            throws IOException;

    /**
     * Updates a custom {@link ResourceRepresentation} at a connector.
     *
     * @param resourceID             ID of the Resource for which the representation is updated
     * @param representationID       ID of the representation to be updated
     * @param resourceRepresentation representation to be updated
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String updateCustomResourceRepresentation(String resourceID, String representationID,
                                              ResourceRepresentation resourceRepresentation) throws IOException;

    /**
     * Updates a resource contract at a connector.
     *
     * @param resourceID ID of the Resource for which the contract is updated
     * @param contract   contract to be created
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String updateResourceContract(String resourceID, Contract contract) throws IOException;
}