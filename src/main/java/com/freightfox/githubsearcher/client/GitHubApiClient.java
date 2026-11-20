package com.freightfox.githubsearcher.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.freightfox.githubsearcher.dto.GitHubSearchResponse;

@Component
public class GitHubApiClient {

    private final RestClient restClient;

    public GitHubApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "freightfox-github-searcher")
                .build();
    }

    public GitHubSearchResponse searchRepositories(
            String query,
            String language,
            String sort) {

        String searchQuery = query.trim();

        if (language != null && !language.isBlank()) {
            searchQuery += " language:" + language.trim();
        }
        
        final String finalSearchQuery = searchQuery;

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/repositories")
                        .queryParam("q", finalSearchQuery)
                        .queryParam("sort", sort)
                        .build())
                .retrieve()
                .body(GitHubSearchResponse.class);
    }
}