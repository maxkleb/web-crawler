package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * This is the component, responsible of crawling single domain.
 */
public class Crawler implements Runnable {
    private ExecutorService executor;
    private List<Page> pages = new LinkedList<>();
    private LinkedBlockingQueue<Future<Page>> queue = new LinkedBlockingQueue();
    private LinkedBlockingQueue<Page> resultsQueue;
    private int maxPagesNumber;
    private ConcurrentMap<String, String> visitedWebsites;;
    private String domain;

    public Crawler(ConcurrentMap<String, String> visitedWebsites,
                   LinkedBlockingQueue<Page> resultsQueue,
                   String domain, int threadsNumber, int maxPagesNumber) {
        executor = Executors.newFixedThreadPool(threadsNumber);
        this.visitedWebsites = visitedWebsites;
        this.resultsQueue = resultsQueue;
        this.maxPagesNumber = maxPagesNumber;
        this.domain = domain;
    }

    @Override
    public void run() {
        try {
            List<Page> result = crawl(domain);
            result.forEach(page -> {
                try {
                    resultsQueue.put(page);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            System.out.println("Error: " + e);
        } catch (ExecutionException e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * Start crawling in given domain.
     * @param link website to crawl
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private List<Page> crawl(String link) throws InterruptedException, ExecutionException {
        visitIfNeeded(link);
        int currentPagesCounter = 0;
        while (!(queue.isEmpty() || currentPagesCounter >= this.maxPagesNumber)) {
            pages.add(queue.poll().get());
            currentPagesCounter++;
        }

        executor.shutdown();
        return pages;
    }

    /**
     * Visit given website if didn't visit yet.
     * @param link
     */
    private void visitIfNeeded(String link) {
        if (visitedWebsites.putIfAbsent(link, "") == null) {
            try {
                Future<Page> submit = executor.submit(() -> {
                    int retries = 0;
                    Page badResult = null;
                    while (retries < 3) {
                        try {
                            return visit(link);
                        } catch (IOException e) {
                        retries++;
                        Thread.sleep(5000);
                        badResult = new Page(link, e.getMessage());
                        }
                    }
                    return badResult;
                });
                queue.put(submit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Visit given website and trigger visiting all sub-links.
     * @param link
     * @return
     * @throws IOException
     */
    public Page visit(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        Elements links = doc.select("a[href]");
        Page page = new Page(link, links, doc.html());
        links.forEach(element -> visitIfNeeded(element.attr("abs:href")));

        return page;
    }
}
