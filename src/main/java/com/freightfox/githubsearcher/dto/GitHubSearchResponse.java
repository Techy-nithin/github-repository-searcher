
package com.freightfox.githubsearcher.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubSearchResponse {

    private int total_count;

    private boolean incomplete_results;

    private List<GitHubRepositoryItem> items;

    public int getTotal_count() {
        return total_count;
    }

    public void setTotal_count(int total_count) {
        this.total_count = total_count;
    }

    public boolean isIncomplete_results() {
        return incomplete_results;
    }

    public void setIncomplete_results(boolean incomplete_results) {
        this.incomplete_results = incomplete_results;
    }

    public List<GitHubRepositoryItem> getItems() {
        return items;
    }

    public void setItems(List<GitHubRepositoryItem> items) {
        this.items = items;
    }
}
