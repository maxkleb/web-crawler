Basic web-crawler.

It consumes the input from resources/urls.csv and processes crawling for every link, limited by number of sub-links.

Archetecture:
UrlsReader -> Queue -> CrawlerManager -> WebsitePersister

UrlsReader and WebsitePersister are designed to be single threaded.
CrawlerManager creates Crawler (worker) for every domain and processes it in parallel.