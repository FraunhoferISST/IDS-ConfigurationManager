package de.fraunhofer.isst.configmanager.util.camel;

import de.fraunhofer.iais.eis.AppEndpoint;
import de.fraunhofer.iais.eis.AppEndpointType;
import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConnectorEndpoint;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util.DataspaceConnectorRouteConfigurer;
import de.fraunhofer.isst.configmanager.connector.trustedconnector.TrustedConnectorRouteConfigurer;
import de.fraunhofer.isst.configmanager.util.camel.dto.RouteStepEndpoint;
import de.fraunhofer.isst.configmanager.util.camel.exceptions.NoSuitableTemplateException;
import de.fraunhofer.isst.configmanager.util.camel.exceptions.RouteCreationException;
import de.fraunhofer.isst.configmanager.util.camel.exceptions.RouteDeletionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Component for creating Camel routes from AppRoutes.
 */
@Slf4j
@Component
public class RouteManager {
    /**
     * Indicates if a Dataspace Connector or a Trusted Connector is being managed.
     */
    @Value("${dataspace.connector.enabled}")
    private boolean dataspaceConnectorEnabled;

    /**
     * Helper for deploying and deleting Camel routes via HTTP.
     */
    private final RouteHttpHelper routeHttpHelper;

    /**
     * Helper for deploying and deleting Camel routes in the file system.
     */
    private final RouteFileHelper routeFileHelper;

    /**
     * Constructs a RouteManager.
     *
     * @param routeHttpHelper the RouteHttpHelper instance
     * @param routeFileHelper the RouteFileHelper instance
     */
    @Autowired
    public RouteManager(final RouteHttpHelper routeHttpHelper,
                        final RouteFileHelper routeFileHelper) {
        this.routeHttpHelper = routeHttpHelper;
        this.routeFileHelper = routeFileHelper;
    }

    /**
     * Creates a Camel XML route from a given app route for either the Dataspace Connector or the
     * Trusted Connector. If the Configuration Manager is currently managing a Dataspace Connector,
     * the generated XML route will be sent to the Camel application. If the Configuration
     * Manager is currently managing a Trusted Connector, the generated XML route will be written
     * to a file in the designated directory. Both the Camel application and the directory are
     * specified in application.properties.
     *
     * @param configurationModel config model the app route belongs to; contains key- and truststore
     *                           information
     * @param appRoute the app route to create a Camel route for
     * @throws RouteCreationException if the Camel route cannot be created or deployed
     */
    public void createAndDeployXMLRoute(final ConfigurationModel configurationModel,
                                        final AppRoute appRoute) throws RouteCreationException {
        final var velocityContext = new VelocityContext();

        //create ID for Camel route
        final var camelRouteId = getCamelRouteId(appRoute);
        velocityContext.put("routeId", camelRouteId);

        //get route start and end (will either be connector, app or generic endpoint)
        addRouteStartToContext(velocityContext, appRoute.getAppRouteStart());
        addRouteEndToContext(velocityContext, appRoute.getAppRouteEnd());

        //get route steps (if any)
        addRouteStepsToContext(velocityContext, appRoute.getHasSubRoute());

        try {
            if (dataspaceConnectorEnabled) {
                createDataspaceConnectorRoute(appRoute, velocityContext);
            } else {
                createTrustedConnectorRoute(appRoute, velocityContext, configurationModel,
                        camelRouteId);
            }
        } catch (Exception e) {
            throw new RouteCreationException("Error creating Camel route for AppRoute with ID '"
                    + appRoute.getId() + "'", e);
        }
    }

    /**
     * Extracts the URL of the {@link AppRoute}'s start and adds it to the Velocity context.
     *
     * @param velocityContext the Velocity context
     * @param routeStart start of the AppRoute
     */
    private void addRouteStartToContext(final VelocityContext velocityContext,
                                        final ArrayList<? extends Endpoint> routeStart) {
        if (routeStart.get(0) instanceof ConnectorEndpoint) {
            final var connectorEndpoint = (ConnectorEndpoint) routeStart.get(0);
            velocityContext.put("startUrl", connectorEndpoint.getAccessURL().toString());
        } else if (routeStart.get(0) instanceof GenericEndpoint) {
            final var genericEndpoint = (GenericEndpoint) routeStart.get(0);
            velocityContext.put("startUrl", genericEndpoint.getAccessURL().toString());
            addBasicAuthHeaderForGenericEndpoint(velocityContext, genericEndpoint);
        } else {
            //app is route start
        }
    }

    /**
     * Extracts the URL of the {@link AppRoute}'s end and adds it to the Velocity context.
     *
     * @param velocityContext the Velocity context
     * @param routeEnd end of the AppRoute
     */
    private void addRouteEndToContext(final VelocityContext velocityContext,
                                      final ArrayList<? extends Endpoint> routeEnd) {
        if (routeEnd.get(0) instanceof ConnectorEndpoint) {
            final var connectorEndpoint = (ConnectorEndpoint) routeEnd.get(0);
            velocityContext.put("endUrl", connectorEndpoint.getAccessURL().toString());
        } else if (routeEnd.get(0) instanceof GenericEndpoint) {
            final var genericEndpoint = (GenericEndpoint) routeEnd.get(0);
            velocityContext.put("endUrl", genericEndpoint.getAccessURL().toString());
            addBasicAuthHeaderForGenericEndpoint(velocityContext, genericEndpoint);
        } else {
            //app is route end
        }
    }

    /**
     * Creates and adds the basic authentication header for calling a generic endpoint to a Velocity
     * context, if basic authentication is defined for the given endpoint.
     * @param velocityContext the Velocity context
     * @param genericEndpoint the generic endpoint
     */
    private void addBasicAuthHeaderForGenericEndpoint(final VelocityContext velocityContext,
                                                      final GenericEndpoint genericEndpoint) {
        if (genericEndpoint.getGenericEndpointAuthentication() != null) {
            final var username = genericEndpoint.getGenericEndpointAuthentication()
                    .getAuthUsername();
            final var password = genericEndpoint.getGenericEndpointAuthentication()
                    .getAuthPassword();
            final var auth = username + ":" + password;
            final var encodedAuth = Base64.encodeBase64(auth.getBytes());
            final var authHeader = "Basic " + new String(encodedAuth);
            velocityContext.put("genericEndpointAuthHeader", authHeader);
        }
    }

    /**
     * Extracts the start and end URLs of the {@link AppRoute}'s steps and adds them to the
     * Velocity context.
     *
     * @param velocityContext the Velocity context
     * @param routeSteps steps of the AppRoute
     */
    private void addRouteStepsToContext(final VelocityContext velocityContext,
                                        final ArrayList<? extends RouteStep> routeSteps) {
        final var routeStepEndpoints = new ArrayList<RouteStepEndpoint>();

        if (routeSteps != null) {
            Endpoint lastStepEnd = null;

            for (int i = 0; i < routeSteps.size(); i++) {
                final var routeStep = routeSteps.get(i);

                final var stepStart = routeStep.getAppRouteStart().get(0);

                //if end of last step is same as start of current step only call endpoint once
                if (i > 0 && !stepStart.equals(lastStepEnd)) {
                    addRouteStepEndpoint(stepStart, routeStepEndpoints);
                }

                if (i < routeSteps.size() - 1) {
                    final var stepEnd = routeStep.getAppRouteEnd().get(0);
                    addRouteStepEndpoint(stepEnd, routeStepEndpoints);

                    lastStepEnd = stepEnd;
                }
            }
        }

        velocityContext.put("routeStepEndpoints", routeStepEndpoints);
    }

    /**
     * Adds a {@link RouteStepEndpoint} representation of an {@link Endpoint} to the given list.
     *
     * @param endpoint the endpoint.
     * @param list the list.
     */
    private void addRouteStepEndpoint(final Endpoint endpoint, final List<RouteStepEndpoint> list) {
        if (AppEndpoint.class.isAssignableFrom(endpoint.getClass())) {
            final var appEndpoint = (AppEndpoint) endpoint;
            if (appEndpoint.getAppEndpointType() == AppEndpointType.OUTPUT_ENDPOINT) {
                list.add(new RouteStepEndpoint(appEndpoint.getAccessURL(),
                        HttpMethod.GET));
            } else {
                list.add(new RouteStepEndpoint(appEndpoint.getAccessURL(),
                        HttpMethod.POST));
            }
        } else {
            list.add(new RouteStepEndpoint(endpoint.getAccessURL(),
                    HttpMethod.POST));
        }
    }

    /**
     * Creates and deploys a Camel route for the Dataspace Connector. First, Dataspace Connector
     * specific configuration is added to the Velocity Context, which should already contain
     * general route information. Then, the correct route template for the given AppRoute object
     * is chosen from the Dataspace Connector templates. Last, the generated XML route is sent to
     * the Camel application defined in application.properties.
     *
     * @param appRoute the AppRoute object
     * @param velocityContext the Velocity context
     * @throws Exception if the route file cannot be created or deployed
     */
    private void createDataspaceConnectorRoute(final AppRoute appRoute,
                                               final VelocityContext velocityContext)
            throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("---- [RouteManager createDataspaceConnectorRoute]Creating route for Dataspace Connector...");
        }

        //add basic auth header for connector endpoint
        DataspaceConnectorRouteConfigurer.addBasicAuthToContext(velocityContext);

        //choose correct XML template based on route
        final var template = DataspaceConnectorRouteConfigurer.getRouteTemplate(appRoute);

        if (template != null) {
            final var velocityEngine = new VelocityEngine();
            velocityEngine.init();

            //populate route template with properties from velocity context to create route
            final var writer = populateTemplate(template, velocityEngine, velocityContext);

            //send the generated route (XML) to Camel via HTTP
            routeHttpHelper.sendRouteFileToCamelApplication(writer.toString());
        } else {
            if (log.isWarnEnabled()) {
                log.warn("---- [RouteManager createDataspaceConnectorRoute] Template is null. Unable to create XML route file for AppRoute"
                        + " with ID '{}'", appRoute.getId());
            }

            throw new NoSuitableTemplateException("No suitable Camel route template found for "
                    + "AppRoute with ID '" + appRoute.getId() + "'");
        }
    }

    /**
     * Creates and deploys a Camel route for the Trusted Connector. First, Trusted Connector
     * specific configuration is added to the Velocity Context, which should already contain
     * general route information. Then, the correct route template for the given AppRoute
     * object is chosen from the Trusted Connector templates. Last, the generated XML route is
     * written to the directory defined in application.properties.
     *
     * @param appRoute the AppRoute object
     * @param velocityContext the Velocity context
     * @param configurationModel the Configuration Model containing key- and truststore passwords
     *                           required for the Trusted Connector's SSL configuration
     * @param camelRouteId ID of the Camel route, which is used as the file name
     * @throws Exception if the route file cannot be created or deployed
     */
    private void createTrustedConnectorRoute(final AppRoute appRoute,
                                             final VelocityContext velocityContext,
                                             final ConfigurationModel configurationModel,
                                             final String camelRouteId) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("---- [RouteManager createTrustedConnectorRoute] Creating route for Trusted Connector...");
        }

        //add SSL configuration for connector endpoint
        TrustedConnectorRouteConfigurer.addSslConfig(velocityContext, configurationModel);

        //choose correct XML template based on route
        final var template = TrustedConnectorRouteConfigurer.getRouteTemplate(appRoute);

        if (template != null) {
            final var velocityEngine = new VelocityEngine();
            velocityEngine.init();

            //populate route template with properties from velocity context to create route
            final var writer = populateTemplate(template, velocityEngine, velocityContext);

            //write the generated route (XML) to a file in the designated directory
            routeFileHelper.writeToFile(camelRouteId + ".xml", writer.toString());
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Template is null. Unable to create XML route file for AppRoute"
                        + " with ID '{}'", appRoute.getId());
            }

            throw new NoSuitableTemplateException("No suitable Camel route template found for "
                    + "AppRoute with ID '" + appRoute.getId() + "'");
        }
    }

    /**
     * Populates a given Velocity template using the values from a given Velocity context.
     *
     * @param resource the template
     * @param velocityEngine the Velocity engine required for populating the template
     * @param velocityContext the context containing the values to insert into the template
     * @return the populated template as a string
     * @throws Exception if an error occurs while filling out the route template
     */
    private StringWriter populateTemplate(final Resource resource,
                                          final VelocityEngine velocityEngine,
                                          final VelocityContext velocityContext) throws Exception {
        final var stringWriter = new StringWriter();
        InputStreamReader inputStreamReader;

        try {
            inputStreamReader = new InputStreamReader(resource.getInputStream());
            velocityEngine.evaluate(velocityContext, stringWriter, "", inputStreamReader);
        } catch (Exception e) {
            final var camelRouteId = (String) velocityContext.get("routeId");

            if (log.isErrorEnabled()) {
                log.error("An error occurred while populating template. Please check all respective "
                                + "files for connection with ID '{}' for correctness! (Error message: {})",
                        camelRouteId, e.toString());
            }

            throw e;
        }

        return stringWriter;
    }

    /**
     * Deletes all Camel routes associated with app routes from a given config model by calling
     * {@link RouteManager#deleteRoute(AppRoute)}.
     *
     * @param configurationModel the config model
     * @throws RouteDeletionException if any of the Camel routes cannot be deleted
     */
    public void deleteRouteFiles(final ConfigurationModel configurationModel)
            throws RouteDeletionException {
        for (final var appRoute: configurationModel.getAppRoute()) {
            deleteRoute(appRoute);
        }
    }

    /**
     * Deletes the Camel route for a given {@link AppRoute}. If the Configuration Manager is
     * currently managing a Dataspace Connector, the route is deleted at the Camel application. If
     * the Configuration Manager is currently managing a Trusted Connector, the route file is
     * removed from the designated directory.
     *
     * @param appRoute the AppRoute
     * @throws RouteDeletionException if the Camel route cannot be deleted
     */
    public void deleteRoute(final AppRoute appRoute) throws RouteDeletionException {
        final var camelRouteId = getCamelRouteId(appRoute);

        try {
            if (dataspaceConnectorEnabled) {
                routeHttpHelper.deleteRouteAtCamelApplication(camelRouteId);
            } else {
                routeFileHelper.deleteFile(camelRouteId + ".xml");
            }
        } catch (Exception e) {
            throw new RouteDeletionException("Error deleting Camel route for AppRoute with ID '"
                    + appRoute.getId() + "'", e);
        }

    }

    /**
     * Generated the ID of the Camel route for a given {@link AppRoute}. The Camel route ID consists
     * of the String 'app-route_' followed by the UUID from the AppRoute's ID.
     *
     * @param appRoute the AppRoute
     * @return the Camel route ID
     */
    private String getCamelRouteId(final AppRoute appRoute) {
        final var appRouteId = appRoute.getId().toString()
                .split("/")[appRoute.getId().toString().split("/").length - 1];
        return "app-route_" + appRouteId;
    }

}
