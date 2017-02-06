package com.twitter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.model.Callback;
import com.twitter.util.CallbacksUtil;

/**
 * This is a main entry point for REST resources
 * 
 * @author zli
 *
 */
@Path("")
public class Root {
    final Logger logger = Logger.getLogger(Root.class);
    // From key to value map
    private static Map<String, String> map = new ConcurrentHashMap<>();

    public static final String VALUESTRING = "value=";
    public static final String URLSTRING = "url=";
    public static final String CALLBACKSTRING = "/callback";

    // JSON de/serializations
    private static final ObjectMapper mapper = new ObjectMapper();

    public Root() {
        logger.setLevel(Level.ALL);
    }

    /**
     * Get on root resource Assumption is to return the size of the map.
     * 
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getRoot() {
        logger.log(Level.INFO, String.format("Get on root \n"));
        return String.valueOf(map.size()) + "\n";
    }

    /**
     * Get method on keys, or callbacks
     * 
     * @param key
     * @return Rest response in terms of: A JSON representation of list of
     *         callbacks - if the resource is callback Or the value as in String
     *         for the key - if the resource is a key Or error code
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @GET
    @Path("{key : .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIt(@PathParam("key") String key)
            throws JsonGenerationException, JsonMappingException, IOException {
        logger.log(Level.INFO, String.format("Get on %s \n", key));

        // check if resource ends with callback, then it's a callback resource.
        if (key.endsWith(CALLBACKSTRING)) {
            key = key.substring(0, key.length() - CALLBACKSTRING.length());
            List<Callback> callbacks = CallbacksUtil.getCallbacks(key);
            logger.log(Level.INFO, String.format("Return on Get callbacks : %s \n", key));
            return Response.status(200).entity(mapper.writeValueAsString(callbacks) + "\n").build();
        }

        // Otherwise it's a normal key resource
        if (map.containsKey(key)) {
            String value = map.get(key);
            logger.log(Level.INFO, String.format("Return on Get %s : %s\n", key, value));
            return Response.status(200).entity(value + "\n").build();
        } else {
            logger.log(Level.WARN, String.format("Return 404 on Get %s", key));
            throw new WebApplicationException(404);
        }

    }

    /**
     * Post method on keys or callbacks
     * 
     * @return The rest response
     * @throws URISyntaxException
     */
    @POST
    @Path("{key : .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postIt(@PathParam(value = "key") String key, String payload) throws URISyntaxException {
        logger.log(Level.INFO, String.format("Post on %s with payload: %s \n", key, payload));
        if (key.isEmpty() || payload == null) {
            logger.log(Level.WARN, String.format("Return 400 from Post on %s with payload: %s \n", key, payload));
            throw new WebApplicationException(400);
        }

        if (key.endsWith(CALLBACKSTRING)) {
            if (!payload.startsWith(URLSTRING) || payload.length() < URLSTRING.length()) {
                throw new WebApplicationException(400);
            }
            key = key.substring(0, key.length() - CALLBACKSTRING.length());
            payload = payload.substring(URLSTRING.length());
            if (!CallbacksUtil.isValidCallback(payload)) {
                logger.log(Level.WARN, String.format("Return 400 from Post on %s with payload: %s \n", key, payload));
                throw new WebApplicationException(400);
            }
            String id = CallbacksUtil.setCallback(key, payload);
            return Response.status(201).entity(id + "\n").build();
        }

        else if (!payload.startsWith(VALUESTRING) || payload.length() < VALUESTRING.length()) {
            logger.log(Level.WARN, String.format("Return 400 from Post on %s with payload: %s \n", key, payload));
            throw new WebApplicationException(400);
        }

        String value = payload.substring(VALUESTRING.length());
        if (!CallbacksUtil.isValidValue(key, map.getOrDefault(key, null), value)) {
            logger.log(Level.WARN, String.format("Return 403 from Post on %s with payload: %s \n", key, payload));
            throw new WebApplicationException(403);
        }
        map.put(key, value);

        return Response.status(201).build();
    }

    /**
     * Delete method on callback resource only
     * 
     * @param key
     *            the key for the callback
     * @return Rest response
     */
    @DELETE
    @Path("{key : .+}") // Format should be something like:
                        // /my_key/callback/callback-1
    public Response deleteId(@PathParam(value = "key") String key) {
        String[] keyParts = key.split("/");
        // if this is not on callback
        if (keyParts.length < 3) {
            throw new WebApplicationException(404);
        }

        String callbackId = keyParts[keyParts.length - 1];
        StringBuilder keySb = new StringBuilder(keyParts[0]);

        // reconstruct the key
        for (int i = 1; i < keyParts.length - 2; i++) {
            keySb.append("/");
            keySb.append(keyParts[i]);
        }

        logger.log(Level.INFO, String.format("Trying to delete on key=%s with callbackId=%s\n", key, keySb.toString()));
        if (CallbacksUtil.delete(keySb.toString(), callbackId)) {
            return Response.noContent().build();
        } else {
            throw new WebApplicationException(404);
        }
    }

}
