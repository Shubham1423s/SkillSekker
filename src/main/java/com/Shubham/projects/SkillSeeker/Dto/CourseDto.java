package com.Shubham.projects.SkillSeeker.Dto;

import lombok.*;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CourseDto {
    private String id;
    private String title;
    private String description;
    private String category;
    private String type;
    private String gradeRange;
    private Integer minAge;
    private Integer maxAge;
    private Double price;
    private LocalDateTime nextSessionDate;

}
