package scraper.app.model;

import lombok.Getter;

@Getter
public class FilterDate {
    private String fromDate;
    private String toDate;
    private String issueDate;

    public FilterDate(String fromDate, String toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public FilterDate(String issueDate) {
        this.issueDate = issueDate;
    }
}
