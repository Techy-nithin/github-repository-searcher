package com.freightfox.githubsearcher.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.freightfox.githubsearcher.entity.GitHubRepository;

public interface GitHubRepositoryRepository
        extends JpaRepository<GitHubRepository, Long> {

    List<GitHubRepository> findByLanguageIgnoreCase(
            String language, Sort sort);

    List<GitHubRepository> findByStarsGreaterThanEqual(
            Integer minStars, Sort sort);

    List<GitHubRepository> findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
            String language, Integer minStars, Sort sort);
}