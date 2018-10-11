package edu.vanderbilt.imagecrawler.utils;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.IMAGE;
import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;

/**
 * A pure data class for returning web page elements
 * from a WebPageCrawler implementation class.
 */
public class WebPageElement {
    public String mUrl;
    public Crawler.Type mType;

    private WebPageElement(String url, Crawler.Type type) {
        mUrl = url;
        mType = type;
    }

    public static WebPageElement newImageElement(String url) {
        return new WebPageElement(url, IMAGE);
    }

    public static WebPageElement newPageElement(String url) {
        return new WebPageElement(url, PAGE);
    }
}

