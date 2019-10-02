package edu.vanderbilt.imagecrawler.utils;

import java.net.MalformedURLException;
import java.net.URL;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * A pure data class for returning web page elements
 * from a WebPageCrawler implementation class.
 */
public class WebPageElement {
    private String mUrl;
    private Crawler.Type mType;

    public WebPageElement(String url, Crawler.Type type) {
        mUrl = url;
        mType = type;
    }

    public URL getURL() {
        try {
            return new URL(mUrl);
        } catch (MalformedURLException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public Crawler.Type getType() {
        return mType;
    }

    public static WebPageElement newImageElement(String url) {
        return new WebPageElement(url, IMAGE);
    }

    public static WebPageElement newPageElement(String url) {
        return new WebPageElement(url, PAGE);
    }
}

