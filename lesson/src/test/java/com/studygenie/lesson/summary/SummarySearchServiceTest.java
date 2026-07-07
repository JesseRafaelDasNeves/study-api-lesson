package com.studygenie.lesson.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.studygenie.lesson.course.Course;
import com.studygenie.lesson.course.CourseRepository;
import com.studygenie.lesson.lesson.Lesson;
import com.studygenie.lesson.lesson.LessonRepository;
import com.studygenie.lesson.tag.Tag;
import com.studygenie.lesson.tag.TagRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SummarySearchServiceTest {

    @Autowired
    private SummarySearchService summarySearchService;

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private SummaryTagRepository summaryTagRepository;

    private Course course1;
    private Course course2;
    private Lesson lesson1;
    private Lesson lesson2;
    private Tag tag1;
    private Tag tag2;
    private Summary summary1;
    private Summary summary2;

    @BeforeEach
    public void setUp() {
        summaryTagRepository.deleteAll();
        summaryRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        tagRepository.deleteAll();

        course1 = new Course();
        course1.setName("Curso de Java");
        course1.setAcronym("JAVA");
        course1.setColor("#FF0000");
        course1.setDescription("Curso de Java Básico");
        course1 = courseRepository.save(course1);

        course2 = new Course();
        course2.setName("Curso de Angular");
        course2.setAcronym("ANG");
        course2.setColor("#00FF00");
        course2.setDescription("Curso de Angular");
        course2 = courseRepository.save(course2);

        lesson1 = new Lesson();
        lesson1.setTitle("Aula de Spring Boot");
        lesson1.setDescription("Introdução ao Spring Boot");
        lesson1.setCourse(course1);
        lesson1.setDate(LocalDateTime.now().minusDays(2));
        lesson1 = lessonRepository.save(lesson1);

        lesson2 = new Lesson();
        lesson2.setTitle("Aula de Angular Router");
        lesson2.setDescription("Rotas no Angular");
        lesson2.setCourse(course2);
        lesson2.setDate(LocalDateTime.now().minusDays(1));
        lesson2 = lessonRepository.save(lesson2);

        tag1 = new Tag();
        tag1.setName("Backend");
        tag1 = tagRepository.save(tag1);

        tag2 = new Tag();
        tag2.setName("Frontend");
        tag2 = tagRepository.save(tag2);

        summary1 = new Summary();
        summary1.setTitle("Resumo de Spring Boot API");
        summary1.setContent("O Spring Boot facilita a criação de APIs REST robustas e performáticas em Java.");
        summary1.setSource(SummarySource.MANUAL);
        summary1.setLesson(lesson1);
        summary1.setCreatedAt(LocalDateTime.now().minusDays(2));
        summary1.setUpdatedAt(LocalDateTime.now().minusDays(2));
        summary1 = summaryRepository.save(summary1);

        SummaryTag st1 = new SummaryTag();
        st1.setSummary(summary1);
        st1.setTag(tag1);
        st1.setCreatedAt(LocalDateTime.now());
        st1.setUpdatedAt(LocalDateTime.now());
        summaryTagRepository.save(st1);

        summary2 = new Summary();
        summary2.setTitle("Resumo de Rotas Angular");
        summary2.setContent("O Router permite navegação flexível de páginas SPA.");
        summary2.setSource(SummarySource.MANUAL);
        summary2.setLesson(lesson2);
        summary2.setCreatedAt(LocalDateTime.now().minusDays(1));
        summary2.setUpdatedAt(LocalDateTime.now().minusDays(1));
        summary2 = summaryRepository.save(summary2);

        SummaryTag st2 = new SummaryTag();
        st2.setSummary(summary2);
        st2.setTag(tag2);
        st2.setCreatedAt(LocalDateTime.now());
        st2.setUpdatedAt(LocalDateTime.now());
        summaryTagRepository.save(st2);
    }

    @Test
    public void shouldSearchByQuery() {
        SummarySearchCriteria criteria = new SummarySearchCriteria(
                "Spring Boot", null, null, null, SummarySortBy.DATE, SummarySortDirection.DESC
        );

        Page<SummarySearchResultItem> page = summarySearchService.search(criteria, PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).title()).contains("Spring Boot");
    }

    @Test
    public void shouldFilterByCourse() {
        SummarySearchCriteria criteria = new SummarySearchCriteria(
                null, course1.getId(), null, null, SummarySortBy.DATE, SummarySortDirection.DESC
        );

        Page<SummarySearchResultItem> page = summarySearchService.search(criteria, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).courseId()).isEqualTo(course1.getId());
    }

    @Test
    public void shouldFilterByLesson() {
        SummarySearchCriteria criteria = new SummarySearchCriteria(
                null, null, lesson2.getId(), null, SummarySortBy.DATE, SummarySortDirection.DESC
        );

        Page<SummarySearchResultItem> page = summarySearchService.search(criteria, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).lessonId()).isEqualTo(lesson2.getId());
    }

    @Test
    public void shouldFilterByTagsOr() {
        SummarySearchCriteria criteria = new SummarySearchCriteria(
                null, null, null, List.of(tag1.getId(), tag2.getId()), SummarySortBy.DATE, SummarySortDirection.DESC
        );

        Page<SummarySearchResultItem> page = summarySearchService.search(criteria, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    public void shouldSortBySize() {
        SummarySearchCriteria criteria = new SummarySearchCriteria(
                null, null, null, null, SummarySortBy.SIZE, SummarySortDirection.DESC
        );

        Page<SummarySearchResultItem> page = summarySearchService.search(criteria, PageRequest.of(0, 10));
        // summary1 has longer content (77 chars) than summary2 (51 chars)
        // Por isso, summary1 deve vir primeiro na ordenação decrescente (DESC).
        // Se a ordenação está correta no SQL (ORDER BY content_length DESC), listará summary1 primeiro.
        // Vamos verificar se o primeiro item é o de maior tamanho (summary1).
        assertThat(page.getContent().get(0).title()).isEqualTo(summary1.getTitle());
    }

    @Test
    public void shouldThrowWhenSortingByRelevanceWithoutQuery() {
        SummarySearchCriteria criteria = new SummarySearchCriteria(
                null, null, null, null, SummarySortBy.RELEVANCE, SummarySortDirection.DESC
        );

        assertThatThrownBy(() -> summarySearchService.search(criteria, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A ordenação por relevância exige um termo de busca preenchido.");
    }
}
