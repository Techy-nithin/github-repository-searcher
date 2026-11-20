
package com.freightfox.githubsearcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SearchRequest {

    @NotBlank(message = "Search query cannot be empty")
    private String query;

    private String language;

    @Pattern(
        regexp = "(?i)stars|forks|updated",
        message = "Sort must be stars, forks or updated"
    )
    private String sort = "stars";

    public SearchRequest() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
