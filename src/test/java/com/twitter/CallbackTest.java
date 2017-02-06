package com.twitter;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;
import com.twitter.model.Callback;
import org.mockito.Mockito;

public class CallbackTest extends Mockito {

    @Test
    public void testEquals() {
        Callback c1 = new Callback("id", "url");
        Callback c2 = new Callback("id", "url");
        assertTrue(c1.equals(c2));
        Callback c3 = new Callback("id", "url2");
        assertTrue(c1.equals(c3));
        Callback c4 = new Callback("id2", "url");
        assertFalse(c1.equals(c4));
    }

    @Test
    public void testInvalidUrlCheck() {
        Callback c1 = new Callback("id", "www.invalid.com");
        assertTrue(c1.check("key", "old", "new"));
    }

    @Test
    public void testCallbackCheck() throws HttpException, IOException {
        Callback c1 = new Callback("id", "www.validAlwaysTrue.com");
        HttpClient httpClient = mock(HttpClient.class);
        PostMethod post = mock(PostMethod.class);
        when(post.getResponseBodyAsString()).thenReturn("True");
        when(httpClient.executeMethod(post)).thenReturn(200);
        c1.setClient(httpClient);
        c1.setPost(post);
        assertTrue(c1.check("key", "oldKey", "newKey"));

        when(post.getResponseBodyAsString()).thenReturn("false");
        assertFalse(c1.check("key", "oldKey", "newKey"));

        when(httpClient.executeMethod(post)).thenReturn(400);
        assertTrue(c1.check("key", "oldKey", "newKey"));

        when(httpClient.executeMethod(post)).thenReturn(200);
        when(post.getResponseBodyAsString()).thenReturn("NOTBOOLEAN!!!");
        assertFalse(c1.check("key", "oldKey", "newKey"));

        when(httpClient.executeMethod(post)).thenThrow(new RuntimeException());
        assertTrue(c1.check("key", "oldKey", "newKey"));
    }
}
