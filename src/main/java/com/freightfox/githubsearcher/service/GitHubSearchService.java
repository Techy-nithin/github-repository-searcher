package com.freightfox.githubsearcher.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.freightfox.githubsearcher.client.GitHubApiClient;
import com.freightfox.githubsearcher.dto.GitHubRepositoryItem;
import com.freightfox.githubsearcher.dto.GitHubSearchResponse;
import com.freightfox.githubsearcher.dto.SearchRequest;
import com.freightfox.githubsearcher.entity.GitHubRepository;
import com.freightfox.githubsearcher.repository.GitHubRepositoryRepository;

@Service
public class GitHubSearchService {

    private final GitHubApiClient gitHubApiClient;
    private final GitHubRepositoryRepository repository;

    public GitHubSearchService(
            GitHubApiClient gitHubApiClient,
            GitHubRepositoryRepository repository) {
        this.gitHubApiClient = gitHubApiClient;
        this.repository = repository;
    }
    
    public List<GitHubRepository> searchAndSave(SearchRequest request) {

        String sort = request.getSort();

        if (sort == null || sort.isBlank()) {
            sort = "stars";
        }

        GitHubSearchResponse response =
                gitHubApiClient.searchRepositories(
                        request.getQuery(),
                        request.getLanguage(),
                        sort
                );

        List<GitHubRepository> savedRepositories = new ArrayList<>();

        if (response != null && response.getItems() != null) {

            for (GitHubRepositoryItem item : response.getItems()) {

                GitHubRepository entity = mapToEntity(item);

                saveOrUpdate(entity);

                savedRepositories.add(entity);
            }
        }

        return savedRepositories;
    }
    
    private GitHubRepository mapToEntity(GitHubRepositoryItem item) {

        GitHubRepository entity = new GitHubRepository();

        entity.setId(item.getId());
        entity.setName(item.getName());
        entity.setDescription(item.getDescription());
        entity.setOwner(
                item.getOwner() != null
                        ? item.getOwner().getLogin()
                        : null
        );
        entity.setLanguage(item.getLanguage());
        entity.setStars(item.getStars());
        entity.setForks(item.getForks());
        entity.setLastUpdated(item.getLastUpdated());

        return entity;
    }
    
    private void saveOrUpdate(GitHubRepository entity) {

        GitHubRepository existing =
                repository.findById(entity.getId()).orElse(null);

        if (existing != null) {
            existing.setName(entity.getName());
            existing.setDescription(entity.getDescription());
            existing.setOwner(entity.getOwner());
            existing.setLanguage(entity.getLanguage());
            existing.setStars(entity.getStars());
            existing.setForks(entity.getForks());
            existing.setLastUpdated(entity.getLastUpdated());

            repository.save(existing);
        } else {
            repository.save(entity);
        }
    }
    
    public List<GitHubRepository> getRepositories(
            String language,
            Integer minStars,
            String sort) {

        if (sort == null || sort.isBlank()) {
            sort = "stars";
        }

        String sortField;

        switch (sort.toLowerCase()) {
            case "forks":
                sortField = "forks";
                break;

            case "updated":
                sortField = "lastUpdated";
                break;

            case "stars":
                sortField = "stars";
                break;

            default:
                throw new IllegalArgumentException(
                        "Sort must be stars, forks or updated"
                );
        }

        org.springframework.data.domain.Sort sorting =
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC,
                        sortField
                );

        boolean hasLanguage =
                language != null && !language.isBlank();

        boolean hasMinStars = minStars != null;

        if (hasLanguage && hasMinStars) {
            return repository.findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
                    language.trim(), minStars, sorting);
        }

        if (hasLanguage) {
            return repository.findByLanguageIgnoreCase(
                    language.trim(), sorting);
        }

        if (hasMinStars) {
            return repository.findByStarsGreaterThanEqual(
                    minStars, sorting);
        }

        return repository.findAll(sorting);
    }
}