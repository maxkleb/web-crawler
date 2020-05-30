package crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This process is responsible of reading given CSV and feed the system
 * with the input (links and number of websites to download).
 */
public class UrlsReader implements Runnable {
    private static String COMMA_DELIMITER = ",";
    private LinkedBlockingQueue<WebHost> urlQueue;
    private String path;

    public UrlsReader(LinkedBlockingQueue<WebHost> urlQueue, String path) {
        this.path = path;
        this.urlQueue = urlQueue;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                urlQueue.put(new WebHost(values));
            }
            System.out.println("Finishing UrlsReader's work...");
            // Notify the consumer that all URL's have been read
            urlQueue.put(new WebHost(Main.URLS_READER_DONE, 0));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Terminating UrlsReader...");
        }
    }
}
