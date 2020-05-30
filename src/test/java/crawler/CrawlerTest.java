package crawler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CrawlerTest {

    public static final int PORT = 8089;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Before
    public void setup() throws IOException {
        givenPageExists("page-with-two-links.html");
        givenPageExists("page-with-no-links.html");
        givenPageExists("page-with-multiple-links.html");
        givenPageExists("cyclic.pages/start-page.html");
        givenPageExists("cyclic.pages/cyclic-page1.html");
        givenPageExists("cyclic.pages/cyclic-page2.html");
        givenPageExists("cyclic.pages/cyclic-page3.html");
        givenPageExists("cyclic.pages/cyclic-page4.html");
        givenPageExists("cyclic.pages/cyclic-page5.html");
    }

    private void givenPageExists(String page) throws IOException {
        givenThat(get(urlMatching("/" + page)).willReturn(aResponse()
                .withStatus(200)
                .withBody(Resources.toString(Resources.getResource(page), defaultCharset()))
                .withHeader("Content-Type", "text/html")));
    }

    @Test
    public void testVisitSinglePageWithLinks() throws IOException {
        ConcurrentMap<String, String> visitedWebsites = new ConcurrentHashMap<>();
        LinkedBlockingQueue<Page> resultsQueue = new LinkedBlockingQueue();

        Page page = new Crawler(visitedWebsites, resultsQueue,
                "http://localhost:" + PORT, 1, 2)
                .visit("http://localhost:" + PORT + "/page-with-two-links.html");

        assertThat(page.getLink(), is("http://localhost:" + PORT + "/page-with-two-links.html"));
        assertThat(page.getLinks().size(), is(2));
        assertThat(page.getError().isPresent(), is(false));
    }

    @Test
    public void testVisitSinglePageWithNoLinks() throws IOException {
        ConcurrentMap<String, String> visitedWebsites = new ConcurrentHashMap<>();
        LinkedBlockingQueue<Page> resultsQueue = new LinkedBlockingQueue();

        Page page = new Crawler(visitedWebsites, resultsQueue,
                "http://localhost:" + PORT, 1, 1)
                .visit("http://localhost:" + PORT + "/page-with-no-links.html");

        assertThat(page.getLink(), is("http://localhost:" + PORT + "/page-with-no-links.html"));
        assertThat(page.getLinks().size(), is(0));
        assertThat(page.getError().isPresent(), is(false));
    }

    @Test
    public void testCrawlerPagesLimitSingleThread() throws InterruptedException {
        ConcurrentMap<String, String> visitedWebsites = new ConcurrentHashMap<>();
        LinkedBlockingQueue<Page> resultsQueue = new LinkedBlockingQueue();


        Crawler crawler = new Crawler(visitedWebsites, resultsQueue,
                "http://localhost:" + PORT + "/page-with-multiple-links.html", 1, 3);

        Thread crawlerThread = new Thread(crawler);
        crawlerThread.start();

        Thread.sleep(10000);
        List<Page> testResults = new LinkedList<>();

        while (!resultsQueue.isEmpty()) {
            Page page = resultsQueue.take();
            testResults.add(page);
        }
        System.out.println("Proceeded pages:");
        testResults.forEach(page -> System.out.println(page.getLink()));
        assertThat(testResults.size(), is(3));
    }

    @Test
    public void testCrawlerPagesLimitMultipleThreads() throws InterruptedException {
        ConcurrentMap<String, String> visitedWebsites = new ConcurrentHashMap<>();
        LinkedBlockingQueue<Page> resultsQueue = new LinkedBlockingQueue();


        Crawler crawler = new Crawler(visitedWebsites, resultsQueue,
                "http://localhost:" + PORT + "/page-with-multiple-links.html", 2, 3);

        Thread crawlerThread = new Thread(crawler);
        crawlerThread.start();

        Thread.sleep(10000);
        List<Page> testResults = new LinkedList<>();

        while (!resultsQueue.isEmpty()) {
            Page page = resultsQueue.take();
            testResults.add(page);
        }
        System.out.println("Proceeded pages:");
        testResults.forEach(page -> System.out.println(page.getLink()));
        assertThat(testResults.size(), is(3));
    }

    @Test
    public void testCrawlerCyclePagesSingleThread() throws InterruptedException {
        ConcurrentMap<String, String> visitedWebsites = new ConcurrentHashMap<>();
        LinkedBlockingQueue<Page> resultsQueue = new LinkedBlockingQueue();


        Crawler crawler = new Crawler(visitedWebsites, resultsQueue,
                "http://localhost:" + PORT + "/cyclic.pages/start-page.html", 1, 10);

        Thread crawlerThread = new Thread(crawler);
        crawlerThread.start();

        Thread.sleep(10000);
        List<Page> testResults = new LinkedList<>();

        while (!resultsQueue.isEmpty()) {
            Page page = resultsQueue.take();
            testResults.add(page);
        }
        System.out.println("Proceeded pages:");
        testResults.forEach(page -> System.out.println(page.getLink()));
        assertThat(testResults.size(), is(6));
    }

    @Test
    public void testCrawlerCyclePagesMultipleThreads() throws InterruptedException {
        ConcurrentMap<String, String> visitedWebsites = new ConcurrentHashMap<>();
        LinkedBlockingQueue<Page> resultsQueue = new LinkedBlockingQueue();


        Crawler crawler = new Crawler(visitedWebsites, resultsQueue,
                "http://localhost:" + PORT + "/cyclic.pages/start-page.html", 2, 10);

        Thread crawlerThread = new Thread(crawler);
        crawlerThread.start();

        Thread.sleep(10000);
        List<Page> testResults = new LinkedList<>();

        while (!resultsQueue.isEmpty()) {
            Page page = resultsQueue.take();
            testResults.add(page);
        }

        System.out.println("Proceeded pages:");
        testResults.forEach(page -> System.out.println(page.getLink()));
        assertThat(testResults.size(), is(6));
    }

    @Test
    public void testCrawlerHighPagesNumberMultipleThreads() throws InterruptedException {
        ConcurrentMap<String, String> visitedWebsites = new ConcurrentHashMap<>();
        LinkedBlockingQueue<Page> resultsQueue = new LinkedBlockingQueue();


        Crawler crawler = new Crawler(visitedWebsites, resultsQueue,
                "http://localhost:" + PORT + "/page-with-multiple-links.html", 2, 100);

        Thread crawlerThread = new Thread(crawler);
        crawlerThread.start();

        Thread.sleep(60000);
        List<Page> testResults = new LinkedList<>();

        while (!resultsQueue.isEmpty()) {
            Page page = resultsQueue.take();
            testResults.add(page);
        }
        System.out.println("Proceeded pages:");
        testResults.forEach(page -> System.out.println(page.getLink()));
        assertThat(testResults.size(), is(100));
    }
}