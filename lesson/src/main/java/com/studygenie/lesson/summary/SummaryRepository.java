package com.studygenie.lesson.summary;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SummaryRepository extends JpaRepository<Summary, UUID> {

    @Query(
      value = """
        SELECT DISTINCT s.id, s.title, s.content, s.created_at, s.lesson_id,
               l.title AS lesson_title, l.course_id, c.name AS course_name,
               CASE WHEN :query IS NULL OR :query = ''
                    THEN NULL
                    ELSE ts_rank(s.search_vector, plainto_tsquery('portuguese', :query))
               END AS relevance
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        JOIN courses c ON c.id = l.course_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        ORDER BY s.created_at DESC
        """,
      countQuery = """
        SELECT COUNT(DISTINCT s.id)
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        """,
      nativeQuery = true
    )
    Page<Object[]> searchSummariesOrderByDateDesc(
        @Param("query") String query,
        @Param("courseId") UUID courseId,
        @Param("lessonId") UUID lessonId,
        @Param("tagIds") UUID[] tagIds,
        Pageable pageable
    );

    @Query(
      value = """
        SELECT DISTINCT s.id, s.title, s.content, s.created_at, s.lesson_id,
               l.title AS lesson_title, l.course_id, c.name AS course_name,
               CASE WHEN :query IS NULL OR :query = ''
                    THEN NULL
                    ELSE ts_rank(s.search_vector, plainto_tsquery('portuguese', :query))
               END AS relevance
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        JOIN courses c ON c.id = l.course_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        ORDER BY s.created_at ASC
        """,
      countQuery = """
        SELECT COUNT(DISTINCT s.id)
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        """,
      nativeQuery = true
    )
    Page<Object[]> searchSummariesOrderByDateAsc(
        @Param("query") String query,
        @Param("courseId") UUID courseId,
        @Param("lessonId") UUID lessonId,
        @Param("tagIds") UUID[] tagIds,
        Pageable pageable
    );

    @Query(
      value = """
        SELECT DISTINCT s.id, s.title, s.content, s.created_at, s.lesson_id,
               l.title AS lesson_title, l.course_id, c.name AS course_name,
               ts_rank(s.search_vector, plainto_tsquery('portuguese', :query)) AS relevance
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        JOIN courses c ON c.id = l.course_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        ORDER BY relevance DESC
        """,
      countQuery = """
        SELECT COUNT(DISTINCT s.id)
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        """,
      nativeQuery = true
    )
    Page<Object[]> searchSummariesOrderByRelevanceDesc(
        @Param("query") String query,
        @Param("courseId") UUID courseId,
        @Param("lessonId") UUID lessonId,
        @Param("tagIds") UUID[] tagIds,
        Pageable pageable
    );

    @Query(
      value = """
        SELECT DISTINCT s.id, s.title, s.content, s.created_at, s.lesson_id,
               l.title AS lesson_title, l.course_id, c.name AS course_name,
               ts_rank(s.search_vector, plainto_tsquery('portuguese', :query)) AS relevance
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        JOIN courses c ON c.id = l.course_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        ORDER BY relevance ASC
        """,
      countQuery = """
        SELECT COUNT(DISTINCT s.id)
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        """,
      nativeQuery = true
    )
    Page<Object[]> searchSummariesOrderByRelevanceAsc(
        @Param("query") String query,
        @Param("courseId") UUID courseId,
        @Param("lessonId") UUID lessonId,
        @Param("tagIds") UUID[] tagIds,
        Pageable pageable
    );

    @Query(
      value = """
        SELECT DISTINCT s.id, s.title, s.content, s.created_at, s.lesson_id,
               l.title AS lesson_title, l.course_id, c.name AS course_name,
               CASE WHEN :query IS NULL OR :query = ''
                    THEN NULL
                    ELSE ts_rank(s.search_vector, plainto_tsquery('portuguese', :query))
               END AS relevance,
               LENGTH(s.content) AS content_length
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        JOIN courses c ON c.id = l.course_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        ORDER BY content_length DESC
        """,
      countQuery = """
        SELECT COUNT(DISTINCT s.id)
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        """,
      nativeQuery = true
    )
    Page<Object[]> searchSummariesOrderBySizeDesc(
        @Param("query") String query,
        @Param("courseId") UUID courseId,
        @Param("lessonId") UUID lessonId,
        @Param("tagIds") UUID[] tagIds,
        Pageable pageable
    );

    @Query(
      value = """
        SELECT DISTINCT s.id, s.title, s.content, s.created_at, s.lesson_id,
               l.title AS lesson_title, l.course_id, c.name AS course_name,
               CASE WHEN :query IS NULL OR :query = ''
                    THEN NULL
                    ELSE ts_rank(s.search_vector, plainto_tsquery('portuguese', :query))
               END AS relevance,
               LENGTH(s.content) AS content_length
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        JOIN courses c ON c.id = l.course_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        ORDER BY content_length ASC
        """,
      countQuery = """
        SELECT COUNT(DISTINCT s.id)
        FROM summaries s
        JOIN lessons l ON l.id = s.lesson_id
        LEFT JOIN summary_tags st ON st.summary_id = s.id
        WHERE (:query IS NULL OR :query = '' OR s.search_vector @@ plainto_tsquery('portuguese', :query))
          AND (:courseId IS NULL OR l.course_id = :courseId)
          AND (:lessonId IS NULL OR s.lesson_id = :lessonId)
          AND (coalesce(:tagIds, NULL) IS NULL OR st.tag_id = ANY(CAST(:tagIds AS UUID[])))
        """,
      nativeQuery = true
    )
    Page<Object[]> searchSummariesOrderBySizeAsc(
        @Param("query") String query,
        @Param("courseId") UUID courseId,
        @Param("lessonId") UUID lessonId,
        @Param("tagIds") UUID[] tagIds,
        Pageable pageable
    );
}
