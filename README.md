# Simple web scrapper
```bash
A Simple Java application written using SpringBoot and Selenium library, enabling scraping of textual data
from a website with filter-based search. It also opens additional tabs to extract detailed information by executing 
JavaScript commands within the code.
```

## Used technologies and libraries:
| Technology           | Version   |
|:---------------------|:----------|
| `JDK`                | `17`      |
| `Maven`              | `4.0.0`   |
| `Lombok`             | `1.18.34` |
| `Spring Boot`        | `2.5.4`   |
| `Selenium Java`      | `4.25.0`  |
| `Web driver manager` | `5.6.1`   |
| `Checkstyle plugin`  | `3.1.1`   |

# How does it work
1. The scraper opens this [link](https://hendersonnv-energovweb.tylerhost.net/apps/selfservice#/search) and clicks the "Search" button.
2. The next step is to extract the first record and open the link inside it.
3. After opening this link in a new tab, the scraper extracts additional data.
4. Then, the scraper clicks the "Contacts" button and reads the data table.
5. The tab is closed, and we return to the main page to continue scraping all subsequent records.
6. When we reach the last record, we switch to the next page.
7. The data is saved to a file output.csv

# How to run this program
1. Download the code from Github by this [link](https://github.com/mrmax24/scraper-app)
2. Open it in IntelliJ IDEA.
3. Build the project using the command ```mvn clean install```
4. Run the program ```scraper.app.Main```










