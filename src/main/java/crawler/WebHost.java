package crawler;

/**
 * WebHost represents single input row.
 */
public class WebHost {
    private String websiteHost;
    private int numberOfPagesToDownload;


    public WebHost(String[] line) {
        String url = line[0];
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        int numOfPagesToDownload = Integer.valueOf(line[1]);

        this.websiteHost = url;
        this.numberOfPagesToDownload = numOfPagesToDownload;
    }

    public WebHost(String websiteHost, int numberOfPagesToDownload) {
        this.websiteHost = websiteHost;
        this.numberOfPagesToDownload = numberOfPagesToDownload;
    }

    public String getHost() {
        return this.websiteHost;
    }

    public int getNumberOfPagesToDownload() {
        return this.numberOfPagesToDownload;
    }
}
