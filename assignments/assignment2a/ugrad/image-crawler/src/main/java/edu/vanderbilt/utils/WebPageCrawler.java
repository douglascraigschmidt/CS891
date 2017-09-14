package edu.vanderbilt.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.function.Function;

import edu.vanderbilt.platform.Device;

/**
 * This helper class works around deficiencies in the jsoup library
 * (www.jsoup.org), which doesn't make web-based crawling and local
 * filesystem crawling transparent out-of-the-box..
 */
public class WebPageCrawler implements Crawler {
    /**
     * @return A container that wraps the HTML document associated with the @a pageUri.
     */
    public Container getContainer(String uri) {
        // This function (1) connects to a URL and gets its
        // contents and (2) converts checked exceptions to runtime
        // exceptions.
        Function<String, Document> connect =
            ExceptionUtils.rethrowFunction(url
                                           -> Jsoup.connect(url).get());

        Document apply = connect.apply(uri);
        return new DocumentContainer(apply, uri);
    }

    /**
     * Encapsulates/hides the JSoup Document object into a generic container.
     */
    protected class DocumentContainer implements Container {
        private String uri;
        private Document document;

        protected DocumentContainer(Document document, String uri) {
            if (Device.options().getDiagnosticsEnabled()) {
                System.out.println(">*********************************************");
                System.out.println("WebPageCrawler: constructor()");
                System.out.println("Constructed document: " + (uri == null ? "NULL" : uri));
                System.out.println("             baseURL: " + (document == null
                                                               ? "NULL"
                                                               : document.baseUri()));
                System.out.println("<*********************************************");
            }

            this.document = document;
            this.uri = uri;
        }

        @Override
        public Array<String> getObjectsAsStrings(Type type) {
            __printSearchResultsStarting(type, uri, document);

            Array<String> results;

            switch (type) {
            case CONTAINER: {
                results = document.select("a[href]").stream()
                    .map(element -> element.attr("abs:href"))
                    .collect(ArrayCollector.toArray());
                break;
            }
            case IMAGE:
                results = document.select("img").stream()
                    .map(element -> element.attr("abs:src"))
                    .collect(ArrayCollector.toArray());
                break;
            default:
                throw new IllegalArgumentException("Invalid WebPageCrawler object type.");
            }

            __printSearchResults(results, document);

            return results;
        }

        private void __printSearchResultsStarting(Type type, String uri, Document doc) {
            if (!Device.options().getDiagnosticsEnabled()) {
                return;
            }

            System.out.println(">*********************************************");
            System.out.println("WebPageCrawler: getObjectAsString()");
            System.out.println("Searching CONTAINER: " + (uri == null ? "NULL" : uri));
            System.out.println("            baseURL: " + doc.baseUri());
            System.out.println("      Searching For: " + type.name());
        }

        private void __printSearchResults(Array<String> results, Document doc) {
            if (!Device.options().getDiagnosticsEnabled()) {
                return;
            }

            System.out.println(
                               "       Result Count: " + (results == null ? "0" : results.size()));
            if (results != null && results.size() > 0) {
                System.out.print("            Results: ");
                for (int i = 0; i < results.size(); i++) {
                    if (i == 0) {
                        System.out.println(results.get(i));
                    } else {
                        System.out.println("                     " + results.get(i));
                    }
                }
            }
            System.out.println("<*********************************************");
        }

        @Override
        public Array<URL> getObjectsAsUrls(Type type) {
            return getObjectsAsStrings(type).stream()
                .map(ExceptionUtils.rethrowFunction(URL::new))
                .collect(ArrayCollector.toArray());
        }
    }
}
