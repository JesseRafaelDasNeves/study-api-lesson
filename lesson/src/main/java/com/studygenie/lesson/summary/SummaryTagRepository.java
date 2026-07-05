package com.studygenie.lesson.summary;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryTagRepository extends JpaRepository<SummaryTag, UUID> {
    void deleteBySummary(Summary summary);
}
