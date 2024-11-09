package scraper.app.service;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.WebElement;

public class DataExtractor {
    public List<String> extractData(List<WebElement> elements) {
        return elements.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }
}
