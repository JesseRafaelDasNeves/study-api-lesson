package com.studygenie.lesson.summary;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface SummaryTagRepository extends JpaRepository<SummaryTag, UUID> {
    @Modifying
    @Transactional
    void deleteBySummary(Summary summary);

    List<SummaryTag> findBySummaryIdIn(List<UUID> summaryIds);
}
