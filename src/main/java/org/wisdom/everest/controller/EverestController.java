package org.wisdom.everest.controller;

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.everest.impl.DefaultRequest;
import org.ow2.chameleon.everest.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.content.Json;
import org.wisdom.api.content.ParameterConverters;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The controller giving access to Everest.
 */
@Controller
public class EverestController extends DefaultController {

    public static final String EVEREST_URL = "/everest";
    @Requires
    private EverestService everest;

    @Requires
    private Json json;

    @Requires
    private ParameterConverters converters;

    private static final Logger LOGGER = LoggerFactory.getLogger(EverestController.class);

    @Override
    public List<Route> routes() {
        return ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on(EVEREST_URL + "*").to(this, "serve"),
                new RouteBuilder().route(HttpMethod.POST).on(EVEREST_URL + "*").to(this, "serve"),
                new RouteBuilder().route(HttpMethod.DELETE).on(EVEREST_URL + "*").to(this, "serve"),
                new RouteBuilder().route(HttpMethod.PUT).on(EVEREST_URL + "*").to(this, "serve"),
                new RouteBuilder().route(HttpMethod.OPTIONS).on(EVEREST_URL + "*").to(this, "serve"),
                new RouteBuilder().route(HttpMethod.PATCH).on(EVEREST_URL + "*").to(this, "serve")
        );
    }

    public Result serve() {
        try {
            Path path = Path.from(context().request().path());
            // Remove the beginning Everest
            path = path.subtract(Path.from(EVEREST_URL));

            Resource resource = everest.process(translate(path));

            LOGGER.info("=> " + json.toJson(resource));

            switch (HttpMethod.from(request().method())) {
                case HEAD:
                    return ok().with(LOCATION, toUrl(path));
                case PUT:
                    return status(200).render(resource).json().with(LOCATION, toUrl(path));
                default:
                    return ok().render(resource).json().with(LOCATION, toUrl(path));
            }
        } catch (IllegalActionOnResourceException e) {
            return status(405).render(e.getMessage()).json();
        } catch (ResourceNotFoundException e) {
            return notFound();
        }
    }

    private String toUrl(Path path) {
        //TODO Detect https.
        return "http://" + request().host() + EVEREST_URL + path.toString();
    }


    public DefaultRequest translate(Path path) {
        LOGGER.info("Everest request: " + request().method() + " " + request().path());
        Action action = getAction();

        Map<String, List<String>> params = context().parameters();
        if (params == null || params.isEmpty()) {
            // Fall-back for forms
            params = context().attributes();
        }

        Map<String, Object> everestParameters = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            if (entry.getKey().startsWith("$")) {
                // It's a type, ignore it.
                continue;
            }

            String name = entry.getKey();
            Class type = String.class;
            // Do we have a type ?
            if (params.containsKey("$" + name)) {
                String classname = params.get("$" + name).get(0);
                type = load(classname);
            }
            Object value = converters.convertValues(entry.getValue(), type, null, null);
            everestParameters.put(name, value);
        }

        return new DefaultRequest(action, path, everestParameters);
    }

    private Class load(String classname) {
        try {
            return this.getClass().getClassLoader().loadClass(classname);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot load " + classname, e);
        }
    }

    private Action getAction() {
        Action action;
        switch (HttpMethod.valueOf(request().method())) {
            case GET:
            case HEAD:
                action = Action.READ;
                break;
            case PUT:
                action = Action.CREATE;
                break;
            case POST:
            case PATCH:
                action = Action.UPDATE;
                break;
            case DELETE:
                action = Action.DELETE;
                break;
            default:
                throw new IllegalArgumentException("The HTTP Method " + request().method() + " is not supported by " +
                        "Everest");
        }
        return action;
    }
}
