# Simple web scrapper
```bash
A Simple Java application written using SpringBoot and Selenium library, enabling scraping
of textual data from a website with filter-based search. This works within a multithreaded
environment, opening each page with records within a separate thread, scraping multiple tabs
simultaneously, and processing each website within its own thread.
```

## Used technologies and libraries:
| Technology          | Version   |
| `JDK`               | `17`      |
| `Maven`             | `4.0.0`   |
| `Lombok`            | `1.18.34` |
| `Spring Boot`       | `3.3.4`   |
| `Selenium Java`     | `4.27.0`  |
| `Slf4j api`         | `2.0.0`   |
| `Logback`           | `1.4.12`   |
| `logback`           | `1.4.12`   |
| `Webdriver manager` | `5.9.2`   |
| `Checkstyle plugin`  | `3.1.1`   |

# How does it work
1. The scraper opens two websites in separate threads: [Henderson](https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search)
and [Calabasas](https://ci-calabasas-ca.smartgovcommunity.com/ApplicationPublic/ApplicationSearchAdvanced/Search)
2. Then, the scraper processes each page of pagination in separate threads.
3. The next step is filtering and searching for records.
4. Once a page with records is opened, the scraper reads basic information and opens each record in a new tab to extract additional data.
5. After opening a link in a new tab, the scraper extracts the additional data.
6. The extracted data is saved to output_1.csv and output_2.csv.
7. Logs are saved in logs/scraper.log.

# How to run this program
1. Download the code from Github by this [link](https://github.com/mrmax24/scraper-app)
2. Open it in IntelliJ IDEA.
3. Build the project using the command ```mvn clean install```
4. Run the program ```scraper.app.Main```










