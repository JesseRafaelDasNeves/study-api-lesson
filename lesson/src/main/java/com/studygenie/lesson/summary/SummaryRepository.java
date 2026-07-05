package com.studygenie.lesson.summary;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, UUID> {
}
