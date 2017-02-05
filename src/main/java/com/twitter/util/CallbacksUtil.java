package com.twitter.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.twitter.model.Callback;

/**
 * The utility class storing all the callback related information and callback operations
 * @author zli
 *
 */
public class CallbacksUtil {

    public static final String CALLBACKPREFIX = "callback-";

    // <key : <url : callback> map used to find, and create new callbacks
    private static ConcurrentHashMap<String, Map<String, Callback>> callbackMap = new ConcurrentHashMap<>();
    // next available id for a key. This just keeps incrementing.
    // To improve, we can use another queue and put released ids back in use again.
    private static ConcurrentHashMap<String, AtomicInteger> nextAvailableIds = new ConcurrentHashMap<>();

    /**
     * Setting the callback, including replacing and creating
     * @return id of the callback
     */
    public static String setCallback(String key, String url) {
        String id;
        synchronized (callbackMap) {
            Map<String, Callback> map;
            if (!callbackMap.containsKey(key)) {
                map = new HashMap<>();
            } else {
                map = callbackMap.get(key);
            }
            
            //short circuit if the url is already existed.
            if (map.containsKey(url))
                return map.get(url).getId();
            
            AtomicInteger nextId = nextAvailableIds.getOrDefault(key, new AtomicInteger(1));
            id = CALLBACKPREFIX + nextId.toString();
            map.put(url, new Callback(id, url));

            //increase the id for the next use
            nextId.incrementAndGet();
            nextAvailableIds.put(key, nextId);
            callbackMap.put(key, map);
        }
        return id;
    }

    /**
     * Get callbacks according to the key
     * @return  A list of all callbacks for the key, or an empty list
     */
    public static List<Callback> getCallbacks(String key) {
        List<Callback> result = new ArrayList<>();
        synchronized (callbackMap) {
            if (!callbackMap.containsKey(key))
                return result;
            Map<String, Callback> map = callbackMap.get(key);
            for (String url : map.keySet()) {
                result.add(map.get(url));
            }
        }
        return result;
    }

    /**
     * Delete a key's callback according to the id
     * @return boolean   if the remove happened 
     */
    public static boolean delete(String key, String id) {
        synchronized (callbackMap) {
            if (!callbackMap.containsKey(key))
                return false;

            Map<String, Callback> map = callbackMap.get(key);

            for (String url : map.keySet()) {
                if (map.get(url).getId().equals(id)) {
                    map.remove(url);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if a value for a key is valid, after calling all the callbacks for the key
     * @return boolean  if it's valid 
     */
    public static boolean isValidValue(String key, String oldValue, String newValue) {
        Map<String, Callback> map = callbackMap.getOrDefault(key, new HashMap<String, Callback>());
        for (String id : map.keySet()) {
            if (!map.get(id).check(key, oldValue, newValue))
                return false;
        }
        return true;
    }

    /**
     * check the format of the url to see if it's valid
     */
    public static boolean isValidCallback(String url) {
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }
        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }

    /**
     * For testing and resetting
     */
    public static void clearAll() {
        callbackMap.clear();
        nextAvailableIds.clear();
    }
}
