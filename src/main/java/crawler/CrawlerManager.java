package crawler;

import java.util.concurrent.*;

/**
 * This is the main worker of the system.
 * It consumes the input from UrlsReader and creates Crawler
 * for each domain.
 */
public class CrawlerManager implements Runnable {
    private ThreadPoolExecutor executor;
    private ConcurrentMap<String, String> visitedWebsites;
    private LinkedBlockingQueue<Page> resultsQueue;
    private LinkedBlockingQueue<WebHost> urlQueue;
    private int threadsNumber;
    private int threadsNumberPerWebsite;


    public CrawlerManager(ConcurrentMap<String, String> visitedWebsites,
                          LinkedBlockingQueue<WebHost> urlQueue,
                          LinkedBlockingQueue<Page> resultsQueue,
                          int threadsNumber,
                          int threadsNumberPerWebsite) {
        this.visitedWebsites = visitedWebsites;
        this.urlQueue = urlQueue;
        this.threadsNumber = threadsNumber;
        this.threadsNumberPerWebsite = threadsNumberPerWebsite;
        this.resultsQueue = resultsQueue;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadsNumber);
    }


    private void execute(String link, int numberOfPagesToDownload) {
        Crawler crawler = new Crawler(visitedWebsites,
                resultsQueue, link, threadsNumberPerWebsite, numberOfPagesToDownload);
        executor.submit(crawler);
    }


    @Override
    public void run() {
        try {
            while (true) {
                if (executor.getActiveCount() < threadsNumber) {
                    WebHost webSite = urlQueue.take();
                    processWebsiteOrFinish(webSite);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Terminating CrawlerPool...");
        }
    }

    /**
     * Process given website of finish if UrlsReader.URLS_READER_DONE
     * has been passed
     * <p>
     * In this case the function will wait until all executors will finish
     * their work and than WesitePersister will be stopped.
     * After that CrawlerPool will be gracefully shutdown.
     *
     * @param webSite website to process
     * @throws InterruptedException
     */
    private void processWebsiteOrFinish(WebHost webSite) throws InterruptedException {
        if (webSite.getHost().equals(Main.URLS_READER_DONE)) {
            while (executor.getActiveCount() > 0) {
                Thread.sleep(500);
            }
            System.out.println("Finishing CrawlerManager's work... ");

            // Notify the consumer that all links have been processed
            resultsQueue.put(new Page(Main.URLS_READER_DONE, null, null));
            Thread.currentThread().interrupt();
        } else {
            execute(webSite.getHost(), webSite.getNumberOfPagesToDownload());
        }
    }
}
