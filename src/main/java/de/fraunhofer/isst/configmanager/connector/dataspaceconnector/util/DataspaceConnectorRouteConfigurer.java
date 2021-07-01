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
package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.ConnectorEndpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Utility class for configuring Camel routes for the Dataspace Connector.
 */
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataspaceConnectorRouteConfigurer {

    /**
     * Username for the Dataspace Connector.
     */
    @Setter
    @Value("${dataspace.connector.api.username}")
    String dataSpaceConnectorApiUsername;

    /**
     * Password for the Dataspace Connector.
     */
    @Setter
    @Value("${dataspace.connector.api.password}")
    String dataSpaceConnectorApiPassword;

    /**
     * ResourceLoader for loading Camel route templates from the classpath.
     */
    final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

    /**
     * Adds basic authentication information for the Dataspace Connector to the Velocity context
     * for creating a Camel XML route to be used with the Dataspace Connector.
     *
     * @param velocityContext the context containing the values to insert into the route template
     */
    public void addBasicAuthToContext(final VelocityContext velocityContext) {
        final var auth = dataSpaceConnectorApiUsername + ":" + dataSpaceConnectorApiPassword;
        final var encodedAuth = Base64.encodeBase64(auth.getBytes());
        final var authHeader = "Basic " + new String(encodedAuth);
        velocityContext.put("connectorAuthHeader", authHeader);
    }

    /**
     * Chooses and returns the route template for the Dataspace Connector based on the app route.
     *
     * @param appRoute the app route
     * @return the route template
     */
    public Resource getRouteTemplate(final AppRoute appRoute) {
        final var routeStart = appRoute.getAppRouteStart();

        Resource resource;
        if (routeStart.get(0) instanceof GenericEndpoint) {
            resource = RESOURCE_LOADER.getResource("classpath:camel-templates/dataspaceconnector/http_to_connector_template.vm");
        } else if (routeStart.get(0) instanceof ConnectorEndpoint) {
            resource = RESOURCE_LOADER.getResource("classpath:camel-templates/dataspaceconnector/connector_to_http_template.vm");
        } else {
            resource = null;
        }

        return resource;
    }
}
