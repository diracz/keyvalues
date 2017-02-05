package com.twitter.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.twitter.model.Callback;

public class CallbacksUtil {

    private static final String CALLBACKPREFIX = "callback-";
    // <key : <url : callback> map used to find, and create new callbacks
    public static ConcurrentHashMap<String, Map<String, Callback>> callbackMap = new ConcurrentHashMap<>();
    // next available id for a key. This just keeps incrementing.
    // To improve, we can use another queue and put released ids back in use again.
    public static ConcurrentHashMap<String, AtomicInteger> nextAvailableIds = new ConcurrentHashMap<>();

    public static String setCallback(String key, String url) {
        String id;
        synchronized (callbackMap) {
            Map<String, Callback> map;
            if (!callbackMap.contains(key)) {
                map = new HashMap<>();
            } else {
                map = callbackMap.get(key);
            }
            if (map.containsKey(url))
                return map.get(url).getName();
            AtomicInteger nextId = nextAvailableIds.getOrDefault(key, new AtomicInteger(1));
            id = CALLBACKPREFIX + nextId.toString();
            map.put(url, new Callback(id, url));
            nextId.incrementAndGet();
            callbackMap.put(key, map);
        }
        return id;
    }

    public static List<Callback> getCallbacks(String key) {
        List<Callback> result = new ArrayList<>();
        if (!callbackMap.containsKey(key))
            return result;
        Map<String, Callback> map = callbackMap.get(key);
        for (String url : map.keySet()) {
            result.add(map.get(url));
        }
        return result;
    }

    public static boolean delete(String key, String id) {
        if (!callbackMap.containsKey(key))
            return false;

        Map<String, Callback> map = callbackMap.get(key);
        if (!map.containsValue(id))
            return false;

        map.remove(id);
        return true;
    }
    
    public static boolean isValidValue(String key, String value) {
        Map<String, Callback> map = callbackMap.getOrDefault(key, new HashMap<String, Callback>());
        for (String id : map.keySet()) {
            if (!map.get(id).check(value)) return false;
        }
        return true;
    }

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
    
    public static void clearAll () {
        callbackMap.clear();
        nextAvailableIds.clear();
    }
}
