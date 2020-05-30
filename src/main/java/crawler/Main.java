package crawler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    private static int MAX_THREADS = 2;
    private static int MAX_THREADS_PER_WEBSITE = 2;
    private static String SOURCE_PATH = "resources/urls.csv";
    private static String DESTINATION_PATH = "html/";
    public static String URLS_READER_DONE = "_DONE_";


    public static void main(String[] args) throws InterruptedException {

        ConcurrentMap<String, String> visitedWebsites = new ConcurrentHashMap<>();
        LinkedBlockingQueue<WebHost> urlQueue = new LinkedBlockingQueue();
        LinkedBlockingQueue<Page> resultsQueue = new LinkedBlockingQueue();

        Thread urlsReader = new Thread(new UrlsReader(urlQueue, SOURCE_PATH));
        Thread crawlerPool = new Thread(new WebsitePersister(resultsQueue, DESTINATION_PATH));
        Thread websitePersist = new Thread(new CrawlerManager(visitedWebsites, urlQueue,
                resultsQueue, MAX_THREADS, MAX_THREADS_PER_WEBSITE));

        urlsReader.start();
        crawlerPool.start();
        websitePersist.start();

        urlsReader.join();
        crawlerPool.join();
        websitePersist.join();

        System.out.println("About to stop the service...");
        System.exit(0);
    }
}
