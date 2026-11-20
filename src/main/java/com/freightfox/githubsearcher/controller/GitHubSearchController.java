package com.freightfox.githubsearcher.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.freightfox.githubsearcher.dto.SearchRequest;
import com.freightfox.githubsearcher.entity.GitHubRepository;
import com.freightfox.githubsearcher.service.GitHubSearchService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/github")
@Tag(
        name = "GitHub Repository Search",
        description = "APIs for searching and retrieving GitHub repositories"
    )
public class GitHubSearchController {

    private final GitHubSearchService service;

    public GitHubSearchController(GitHubSearchService service) {
        this.service = service;
    }
    
    @PostMapping("/search")
    @Operation(
            summary = "Search GitHub repositories",
            description = "Searches GitHub repositories by query and optional language, "
                        + "then saves or updates the results in PostgreSQL."
        )
    public Map<String, Object> searchRepositories(
            @Valid @RequestBody SearchRequest request) {

        List<GitHubRepository> repositories =
                service.searchAndSave(request);

        return Map.of(
                "message", repositories.isEmpty()
                ? "No repositories found"
                : "Repositories fetched and saved successfully",
                "repositories", repositories
        );
    }
    
    @GetMapping("/repositories")
    @Operation(
            summary = "Get saved repositories",
            description = "Retrieves saved repositories with optional language and "
                        + "minimum star filters, sorted by stars, forks, or last updated."
        )
    public Map<String, Object> getRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(defaultValue = "stars") String sort) {

        List<GitHubRepository> repositories =
                service.getRepositories(language, minStars, sort);

        return Map.of("repositories", repositories);
    }
}