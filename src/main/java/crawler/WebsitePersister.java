package crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class consumes crawled websites and persists it to local storage.
 */
public class WebsitePersister implements Runnable {
    protected LinkedBlockingQueue<Page> resultsQueue;
    private String destinationPath;

    WebsitePersister(LinkedBlockingQueue<Page> resultsQueue, String destinationPath) {
        this.resultsQueue = resultsQueue;
        this.destinationPath = destinationPath;
    }

    @Override
    public void run() {
        try {
            System.out.println("Starting WebsitePersister...");
            while (true) {
                Page page = resultsQueue.take();
                if (page.getLink().equals(Main.URLS_READER_DONE)) break;
                persistToLocal(page);
            }
            System.out.println("Finishing WebsitePersister's work...");
        } catch (InterruptedException e) {
            System.out.println("Terminating WebsitePersister...");
        }
    }

    private void persistToLocal(Page page) {
        if (!page.getError().isPresent()) {
            String doc = page.getDoc();
            String link = page.getLink();
            String fileName = link.replace("/", "_")
                    .replace(":", "_");

            System.out.println("Saving " + fileName);
            File input = new File(destinationPath + fileName + ".html");
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(input, "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            writer.write(doc);
            writer.flush();
            writer.close();
        }
    }
}
