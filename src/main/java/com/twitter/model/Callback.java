package com.twitter.model;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Callback {
    static final Logger logger = Logger.getLogger(Callback.class);
    private String id;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Callback(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean check(String key, String oldValue, String newValue) {
        PostMethod post = new PostMethod(this.url);
        HttpClient client = new HttpClient();
        NameValuePair[] pairs = new NameValuePair[3];
        pairs[0] = new NameValuePair("key", key);
        pairs[1] = new NameValuePair("current", oldValue);
        pairs[2] = new NameValuePair("requested", newValue);
        post.setRequestBody(pairs);
        
        try {
            int statusCode = client.executeMethod(post);
            if (statusCode >= 300) {
                logger.log(Level.ERROR,
                        String.format("url=%s did not return valid response code! code=%d", url, statusCode));
                return true;
            }
            String responseString = post.getResponseBodyAsString();
            return "true".equalsIgnoreCase(responseString);
        } catch (Exception e) {
            logger.log(Level.ERROR, String.format("Calling url=%s results in error! %s", url, e.getMessage()));
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Callback other = (Callback) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
