package com.twitter;

import static org.junit.Assert.*;

import org.junit.Test;
import com.twitter.model.Callback;

public class CallbackTest {
    
    @Test
    public void testEquals () {
        Callback c1 = new Callback("id", "url");
        Callback c2 = new Callback("id", "url");
        assertTrue (c1.equals(c2));
        Callback c3 = new Callback("id", "url2");
        assertTrue (c1.equals(c3));
        Callback c4 = new Callback("id2", "url");
        assertFalse (c1.equals(c4));
    }
    
    @Test
    public void testInvalidUrlCheck () {
        Callback c1 = new Callback("id", "www.invalid.com");
        assertTrue (c1.check("key", "old", "new"));
    }
}
