package com.studygenie.lesson.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequestDTO(
        @NotBlank(message = "Course name is required") @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters") String name,

        @NotBlank(message = "Course acronym is required") @Size(min = 3, max = 4, message = "Course acronym must be between 3 and 4 characters") String acronym,

        @NotBlank(message = "Course color is required") @Size(min = 4, max = 10, message = "Course color must be between 4 and 10 characters") String color,

        String description) {
    public Course toCourse() {
        Course course = new Course();
        course.setName(this.name);
        course.setAcronym(this.acronym);
        course.setColor(this.color);
        course.setDescription(this.description);
        return course;
    }

    public void mapToCourse(Course course) {
        course.setName(this.name);
        course.setAcronym(this.acronym);
        course.setColor(this.color);
        course.setDescription(this.description);
    }
}
