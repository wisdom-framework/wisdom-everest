package org.wisdom.everest.controller;

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.everest.core.Everest;
import org.ow2.chameleon.everest.impl.DefaultRequest;
import org.ow2.chameleon.everest.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

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
            Path path = Path.from(request().uri());
            // Remove the beginning Everest
            path = path.subtract(Path.from(EVEREST_URL));

            Resource resource = everest.process(translate());

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


    public DefaultRequest translate() {
        LOGGER.info("Everest request: " + request().method() + " " + request().uri());

        Path path = Path.from(request().uri());
        // Remove the beginning Everest
        path = path.subtract(Path.from(EVEREST_URL));
        Action action = getAction();
        Map<String, List<String>> params = context().parameters();
        return new DefaultRequest(action, path, params); // TODO Detect JSON.
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
            case DELETE:
                action = Action.DELETE;
            default:
                throw new IllegalArgumentException("The HTTP Method " + request().method() + " is not supported by " +
                        "Everest");
        }
        return action;
    }

//    public static Map<String, ?> flat(Map<String, String[]> params) {
//        if (params == null) {
//            return Collections.emptyMap();
//        }
//        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
//        for (Map.Entry<String, String[]> entry : params.entrySet()) {
//            if (entry.getValue() == null) {
//                // No value.
//                map.put(entry.getKey(), null);
//            } else if (entry.getValue().length == 0) {
//                map.put(entry.getKey(), Boolean.TRUE.toString());
//            } else if (entry.getValue().length == 1) {
//                // Scalar parameter.
//                map.put(entry.getKey(), entry.getValue()[0]);
//            } else if (entry.getValue().length > 1) {
//                // Translate to list
//                map.put(entry.getKey(), Arrays.asList(entry.getValue()));
//            }
//        }
//        return map;
//    }
}
