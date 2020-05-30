package crawler;

import org.jsoup.select.Elements;

import java.util.Optional;

/**
 * Page represents website.
 */
public class Page {
    private String link;
    private Elements links;
    private String doc;
    private Optional<String> error = Optional.empty();

    public Page(String link, Elements links, String doc) {
        this.link = link;
        this.links = links;
        this.doc = doc;
    }

    public Page(String link, String error) {
        this.link = link;
        this.error = Optional.of(error);
    }

    public Optional<String> getError() {
        return error;
    }

    public String getLink() {
        return link;
    }

    public Elements getLinks() {
        return links;
    }

    public String getDoc() {
        return this.doc;
    }

    @Override
    public String toString() {
        return getError().isPresent() ?
                "Page{ link='" + link + ", error=" + error.get() + '}'
                :
                "Page{ link='" + link + '}';
    }

}
