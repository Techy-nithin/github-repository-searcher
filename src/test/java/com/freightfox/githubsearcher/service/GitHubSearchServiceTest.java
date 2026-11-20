
package com.freightfox.githubsearcher.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import com.freightfox.githubsearcher.dto.GitHubRepositoryItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.freightfox.githubsearcher.client.GitHubApiClient;
import com.freightfox.githubsearcher.dto.GitHubSearchResponse;
import com.freightfox.githubsearcher.dto.SearchRequest;
import com.freightfox.githubsearcher.entity.GitHubRepository;
import com.freightfox.githubsearcher.repository.GitHubRepositoryRepository;

@ExtendWith(MockitoExtension.class)
class GitHubSearchServiceTest {

    @Mock
    private GitHubApiClient gitHubApiClient;

    @Mock
    private GitHubRepositoryRepository repository;

    @InjectMocks
    private GitHubSearchService service;

    @Test
    void shouldReturnEmptyListWhenNoRepositoriesFound() {

        SearchRequest request = new SearchRequest();
        request.setQuery("spring boot");
        request.setLanguage("Java");
        request.setSort("stars");

        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setItems(new ArrayList<>());

        when(gitHubApiClient.searchRepositories(
                "spring boot", "Java", "stars"))
                .thenReturn(response);

        List<GitHubRepository> result =
                service.searchAndSave(request);

        assertTrue(result.isEmpty());

        verify(repository, never()).save(any(GitHubRepository.class));
    }
    
    @Test
    void shouldSaveRepositoryWhenGitHubReturnsResults() {

        SearchRequest request = new SearchRequest();
        request.setQuery("spring boot");
        request.setLanguage("Java");
        request.setSort("stars");

        GitHubRepositoryItem item = new GitHubRepositoryItem();
        item.setId(12345L);
        item.setName("spring-boot-demo");
        item.setDescription("Spring Boot project");
        item.setLanguage("Java");
        item.setStars(100);
        item.setForks(20);

        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setItems(List.of(item));

        when(gitHubApiClient.searchRepositories(
                "spring boot", "Java", "stars"))
                .thenReturn(response);

        when(repository.findById(12345L))
                .thenReturn(java.util.Optional.empty());

        List<GitHubRepository> result =
                service.searchAndSave(request);

        assertEquals(1, result.size());
        assertEquals("spring-boot-demo", result.get(0).getName());

        ArgumentCaptor<GitHubRepository> captor =
                ArgumentCaptor.forClass(GitHubRepository.class);

        verify(repository).save(captor.capture());

        assertEquals(12345L, captor.getValue().getId());
        assertEquals("spring-boot-demo", captor.getValue().getName());
        assertEquals(100, captor.getValue().getStars());
    }
    
    @Test
    void shouldUpdateExistingRepositoryInsteadOfCreatingDuplicate() {

        SearchRequest request = new SearchRequest();
        request.setQuery("spring boot");
        request.setLanguage("Java");
        request.setSort("stars");

        GitHubRepositoryItem item = new GitHubRepositoryItem();
        item.setId(12345L);
        item.setName("spring-boot-demo");
        item.setDescription("Updated Spring Boot project");
        item.setLanguage("Java");
        item.setStars(200);
        item.setForks(50);

        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setItems(List.of(item));

        GitHubRepository existing = new GitHubRepository();
        existing.setId(12345L);
        existing.setName("spring-boot-demo");
        existing.setStars(100);

        when(gitHubApiClient.searchRepositories(
                "spring boot", "Java", "stars"))
                .thenReturn(response);

        when(repository.findById(12345L))
                .thenReturn(java.util.Optional.of(existing));

        service.searchAndSave(request);

        verify(repository).save(existing);

        verify(repository, never())
                .save(argThat(repo -> repo != existing));

        assertEquals(200, existing.getStars());
        assertEquals(50, existing.getForks());
        assertEquals("Updated Spring Boot project",
                existing.getDescription());
    }
    
    @Test
    void shouldFilterRepositoriesByLanguageAndMinimumStars() {

        List<GitHubRepository> expected = new ArrayList<>();

        GitHubRepository repo = new GitHubRepository();
        repo.setId(12345L);
        repo.setName("spring-boot-demo");
        repo.setLanguage("Java");
        repo.setStars(500);

        expected.add(repo);

        when(repository.findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
                eq("Java"),
                eq(100),
                any(org.springframework.data.domain.Sort.class)))
                .thenReturn(expected);

        List<GitHubRepository> result =
                service.getRepositories("Java", 100, "stars");

        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getLanguage());
        assertEquals(500, result.get(0).getStars());

        verify(repository).findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
                eq("Java"),
                eq(100),
                any(org.springframework.data.domain.Sort.class));
    }
    
    @Test
    void shouldThrowExceptionForInvalidSort() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getRepositories("Java", 100, "invalid")
        );
    }
    
    @Test
    void shouldUseStarsAsDefaultSort() {

        List<GitHubRepository> expected = new ArrayList<>();

        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(expected);

        List<GitHubRepository> result =
                service.getRepositories(null, null, null);

        assertTrue(result.isEmpty());

        ArgumentCaptor<org.springframework.data.domain.Sort> captor =
                ArgumentCaptor.forClass(
                        org.springframework.data.domain.Sort.class);

        verify(repository).findAll(captor.capture());

        org.springframework.data.domain.Sort sorting = captor.getValue();

        assertEquals("stars",
                sorting.iterator().next().getProperty());

        assertEquals(
                org.springframework.data.domain.Sort.Direction.DESC,
                sorting.iterator().next().getDirection());
    }
    
    @Test
    void shouldFilterRepositoriesByLanguageOnly() {

        List<GitHubRepository> expected = new ArrayList<>();

        when(repository.findByLanguageIgnoreCase(
                eq("Java"),
                any(org.springframework.data.domain.Sort.class)))
                .thenReturn(expected);

        List<GitHubRepository> result =
                service.getRepositories("Java", null, "stars");

        assertTrue(result.isEmpty());

        verify(repository).findByLanguageIgnoreCase(
                eq("Java"),
                any(org.springframework.data.domain.Sort.class));
    }
    
    @Test
    void shouldFilterRepositoriesByMinimumStarsOnly() {

        List<GitHubRepository> expected = new ArrayList<>();

        when(repository.findByStarsGreaterThanEqual(
                eq(100),
                any(org.springframework.data.domain.Sort.class)))
                .thenReturn(expected);

        List<GitHubRepository> result =
                service.getRepositories(null, 100, "stars");

        assertTrue(result.isEmpty());

        verify(repository).findByStarsGreaterThanEqual(
                eq(100),
                any(org.springframework.data.domain.Sort.class));
    }
    
    @Test
    void shouldSortRepositoriesByForks() {

        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(new ArrayList<>());

        service.getRepositories(null, null, "forks");

        ArgumentCaptor<org.springframework.data.domain.Sort> captor =
                ArgumentCaptor.forClass(
                        org.springframework.data.domain.Sort.class);

        verify(repository).findAll(captor.capture());

        assertEquals("forks",
                captor.getValue().iterator().next().getProperty());

        assertEquals(
                org.springframework.data.domain.Sort.Direction.DESC,
                captor.getValue().iterator().next().getDirection());
    }
    
    @Test
    void shouldSortRepositoriesByUpdatedDate() {

        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(new ArrayList<>());

        service.getRepositories(null, null, "updated");

        ArgumentCaptor<org.springframework.data.domain.Sort> captor =
                ArgumentCaptor.forClass(
                        org.springframework.data.domain.Sort.class);

        verify(repository).findAll(captor.capture());

        assertEquals("lastUpdated",
                captor.getValue().iterator().next().getProperty());

        assertEquals(
                org.springframework.data.domain.Sort.Direction.DESC,
                captor.getValue().iterator().next().getDirection());
    }
    
    @Test
    void shouldReturnEmptyListWhenGitHubResponseIsNull() {

        SearchRequest request = new SearchRequest();
        request.setQuery("spring boot");
        request.setLanguage("Java");
        request.setSort("stars");

        when(gitHubApiClient.searchRepositories(
                "spring boot", "Java", "stars"))
                .thenReturn(null);

        List<GitHubRepository> result =
                service.searchAndSave(request);

        assertTrue(result.isEmpty());

        verify(repository, never()).save(any(GitHubRepository.class));
    }
    
    @Test
    void shouldReturnEmptyListWhenGitHubItemsAreNull() {

        SearchRequest request = new SearchRequest();
        request.setQuery("spring boot");
        request.setLanguage("Java");
        request.setSort("stars");

        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setItems(null);

        when(gitHubApiClient.searchRepositories(
                "spring boot", "Java", "stars"))
                .thenReturn(response);

        List<GitHubRepository> result =
                service.searchAndSave(request);

        assertTrue(result.isEmpty());

        verify(repository, never()).save(any(GitHubRepository.class));
    }
    
    @Test
    void shouldUseStarsWhenSearchSortIsBlank() {

        SearchRequest request = new SearchRequest();
        request.setQuery("spring boot");
        request.setLanguage("Java");
        request.setSort(" ");

        GitHubSearchResponse response = new GitHubSearchResponse();
        response.setItems(new ArrayList<>());

        when(gitHubApiClient.searchRepositories(
                "spring boot", "Java", "stars"))
                .thenReturn(response);

        List<GitHubRepository> result =
                service.searchAndSave(request);

        assertTrue(result.isEmpty());

        verify(gitHubApiClient).searchRepositories(
                "spring boot", "Java", "stars");
    }
}
