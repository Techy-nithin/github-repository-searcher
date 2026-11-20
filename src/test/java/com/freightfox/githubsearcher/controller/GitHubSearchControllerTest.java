
package com.freightfox.githubsearcher.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.freightfox.githubsearcher.entity.GitHubRepository;
import com.freightfox.githubsearcher.exception.GlobalExceptionHandler;
import com.freightfox.githubsearcher.service.GitHubSearchService;

@ExtendWith(MockitoExtension.class)
class GitHubSearchControllerTest {

    @Mock
    private GitHubSearchService service;

    @InjectMocks
    private GitHubSearchController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldSearchRepositoriesSuccessfully() throws Exception {

        GitHubRepository repo = new GitHubRepository();
        repo.setId(12345L);
        repo.setName("spring-boot-demo");
        repo.setLanguage("Java");
        repo.setStars(100);

        when(service.searchAndSave(any()))
                .thenReturn(List.of(repo));

        mockMvc.perform(post("/api/github/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "query": "spring boot",
                            "language": "Java",
                            "sort": "stars"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Repositories fetched and saved successfully"))
                .andExpect(jsonPath("$.repositories[0].name")
                        .value("spring-boot-demo"))
                .andExpect(jsonPath("$.repositories[0].stars")
                        .value(100));
    }
    
    @Test
    void shouldReturnBadRequestForEmptyQuery() throws Exception {

        mockMvc.perform(post("/api/github/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "query": "",
                            "language": "Java",
                            "sort": "stars"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Search query cannot be empty"));

        verifyNoInteractions(service);
    }
    
    @Test
    void shouldReturnNoRepositoriesFoundMessage() throws Exception {

        when(service.searchAndSave(any()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/github/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "query": "unknown-project-123",
                            "language": "Java",
                            "sort": "stars"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("No repositories found"))
                .andExpect(jsonPath("$.repositories").isEmpty());
    }
    
    @Test
    void shouldReturnBadRequestForInvalidSort() throws Exception {

        mockMvc.perform(post("/api/github/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "query": "spring boot",
                            "language": "Java",
                            "sort": "invalid"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .exists());

        verifyNoInteractions(service);
    }
    
    @Test
    void shouldGetAllRepositories() throws Exception {

        GitHubRepository repo = new GitHubRepository();
        repo.setId(12345L);
        repo.setName("spring-boot-demo");
        repo.setLanguage("Java");
        repo.setStars(100);

        when(service.getRepositories(null, null, "stars"))
                .thenReturn(List.of(repo));

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders.get("/api/github/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories[0].name")
                        .value("spring-boot-demo"))
                .andExpect(jsonPath("$.repositories[0].stars")
                        .value(100));
    }
    
    @Test
    void shouldGetRepositoriesWithFilters() throws Exception {

        GitHubRepository repo = new GitHubRepository();
        repo.setId(12345L);
        repo.setName("spring-boot-demo");
        repo.setLanguage("Java");
        repo.setStars(500);

        when(service.getRepositories("Java", 100, "stars"))
                .thenReturn(List.of(repo));

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .get("/api/github/repositories")
                        .param("language", "Java")
                        .param("minStars", "100")
                        .param("sort", "stars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories[0].language")
                        .value("Java"))
                .andExpect(jsonPath("$.repositories[0].stars")
                        .value(500));
    }
    
    @Test
    void shouldReturnBadRequestForInvalidGetSort() throws Exception {

        when(service.getRepositories(null, null, "invalid"))
                .thenThrow(new IllegalArgumentException(
                        "Sort must be stars, forks or updated"));

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .get("/api/github/repositories")
                        .param("sort", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Sort must be stars, forks or updated"));
    }
}
