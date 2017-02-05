package com.twitter;

import static org.junit.Assert.*;

import org.junit.Test;
import com.twitter.util.CallbacksUtil;

public class CallbacksUtilTest {

    @Test
    public void testCallbackSets() {
        CallbacksUtil.clearAll();
        String id1 = CallbacksUtil.setCallback("key1", "url1");
        assertEquals(CallbacksUtil.CALLBACKPREFIX + "1", id1);
        String id2 = CallbacksUtil.setCallback("key2", "url2");
        assertEquals(CallbacksUtil.CALLBACKPREFIX + "1", id2);
        String id3 = CallbacksUtil.setCallback("key1", "url2");
        assertEquals(CallbacksUtil.CALLBACKPREFIX + "2", id3);
        String id4 = CallbacksUtil.setCallback("key1", "url2");
        assertEquals(CallbacksUtil.CALLBACKPREFIX + "2", id4);
    }

    @Test
    public void testIsValidCallback() {
        assertTrue (CallbacksUtil.isValidCallback("http://example.com"));
        assertTrue (CallbacksUtil.isValidCallback("http://example.com/checkers"));
        assertTrue (CallbacksUtil.isValidCallback("http://example.co"));
        assertTrue (CallbacksUtil.isValidCallback("http://example"));
        assertTrue (CallbacksUtil.isValidCallback("https://example"));
        assertFalse (CallbacksUtil.isValidCallback("example"));
        assertFalse (CallbacksUtil.isValidCallback("www.example"));
        assertFalse (CallbacksUtil.isValidCallback("www.example.com"));
        assertFalse (CallbacksUtil.isValidCallback("htt:/www.example.com"));
    }
    
    
}
