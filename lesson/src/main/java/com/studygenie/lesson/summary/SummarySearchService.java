package com.studygenie.lesson.summary;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SummarySearchService {

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private SummaryTagRepository summaryTagRepository;

    @Transactional(readOnly = true)
    public Page<SummarySearchResultItem> search(SummarySearchCriteria criteria, Pageable pageable) {
        // Regra de negócio importante sobre ordenação:
        // Ordenação por relevância só é válida quando existe um termo de busca (query) preenchido.
        if (criteria.sortBy() == SummarySortBy.RELEVANCE && (criteria.query() == null || criteria.query().trim().isEmpty())) {
            throw new IllegalArgumentException("A ordenação por relevância exige um termo de busca preenchido.");
        }

        UUID[] tagIdsArray = null;
        if (criteria.tagIds() != null && !criteria.tagIds().isEmpty()) {
            tagIdsArray = criteria.tagIds().toArray(new UUID[0]);
        }

        SummarySortBy sortBy = criteria.sortBy() != null ? criteria.sortBy() : SummarySortBy.DATE;
        SummarySortDirection direction = criteria.sortDirection() != null ? criteria.sortDirection() : SummarySortDirection.DESC;

        Page<Object[]> rawResults;

        if (sortBy == SummarySortBy.DATE) {
            if (direction == SummarySortDirection.DESC) {
                rawResults = summaryRepository.searchSummariesOrderByDateDesc(criteria.query(), criteria.courseId(), criteria.lessonId(), tagIdsArray, pageable);
            } else {
                rawResults = summaryRepository.searchSummariesOrderByDateAsc(criteria.query(), criteria.courseId(), criteria.lessonId(), tagIdsArray, pageable);
            }
        } else if (sortBy == SummarySortBy.RELEVANCE) {
            if (direction == SummarySortDirection.DESC) {
                rawResults = summaryRepository.searchSummariesOrderByRelevanceDesc(criteria.query(), criteria.courseId(), criteria.lessonId(), tagIdsArray, pageable);
            } else {
                rawResults = summaryRepository.searchSummariesOrderByRelevanceAsc(criteria.query(), criteria.courseId(), criteria.lessonId(), tagIdsArray, pageable);
            }
        } else { // SIZE
            if (direction == SummarySortDirection.DESC) {
                rawResults = summaryRepository.searchSummariesOrderBySizeDesc(criteria.query(), criteria.courseId(), criteria.lessonId(), tagIdsArray, pageable);
            } else {
                rawResults = summaryRepository.searchSummariesOrderBySizeAsc(criteria.query(), criteria.courseId(), criteria.lessonId(), tagIdsArray, pageable);
            }
        }

        List<Object[]> contentList = rawResults.getContent();
        if (contentList.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, rawResults.getTotalElements());
        }

        // Buscar as tags em lote por IDs de resumos
        List<UUID> summaryIds = contentList.stream()
                .map(row -> (UUID) row[0])
                .distinct()
                .collect(Collectors.toList());

        List<SummaryTag> summaryTags = summaryTagRepository.findBySummaryIdIn(summaryIds);
        Map<UUID, List<String>> tagsMap = new HashMap<>();
        for (SummaryTag st : summaryTags) {
            tagsMap.computeIfAbsent(st.getSummary().getId(), k -> new ArrayList<>())
                    .add(st.getTag().getName());
        }

        List<SummarySearchResultItem> items = contentList.stream().map(row -> {
            UUID id = (UUID) row[0];
            String title = (String) row[1];
            String content = (String) row[2];
            Object createdAtTimestamp = row[3];
            UUID lessonId = (UUID) row[4];
            String lessonTitle = (String) row[5];
            UUID courseId = (UUID) row[6];
            String courseName = (String) row[7];
            
            Double relevance = null;
            if (row[8] != null) {
                if (row[8] instanceof Float) {
                    relevance = ((Float) row[8]).doubleValue();
                } else if (row[8] instanceof Double) {
                    relevance = (Double) row[8];
                } else if (row[8] instanceof BigDecimal) {
                    relevance = ((BigDecimal) row[8]).doubleValue();
                }
            }

            // Criar contentSnippet limitando a 200 caracteres
            String snippet = "";
            if (content != null) {
                if (content.length() <= 200) {
                    snippet = content;
                } else {
                    snippet = content.substring(0, 200) + "...";
                }
            }

            List<String> tags = tagsMap.getOrDefault(id, List.of());
            
            Instant createdAt = null;
            if (createdAtTimestamp != null) {
                if (createdAtTimestamp instanceof java.sql.Timestamp) {
                    createdAt = ((java.sql.Timestamp) createdAtTimestamp).toInstant();
                } else if (createdAtTimestamp instanceof java.time.LocalDateTime) {
                    createdAt = ((java.time.LocalDateTime) createdAtTimestamp).atZone(java.time.ZoneId.systemDefault()).toInstant();
                } else if (createdAtTimestamp instanceof java.time.Instant) {
                    createdAt = (java.time.Instant) createdAtTimestamp;
                }
            }

            return new SummarySearchResultItem(
                    id,
                    title,
                    snippet,
                    lessonId,
                    lessonTitle,
                    courseId,
                    courseName,
                    tags,
                    createdAt,
                    relevance
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(items, pageable, rawResults.getTotalElements());
    }
}
