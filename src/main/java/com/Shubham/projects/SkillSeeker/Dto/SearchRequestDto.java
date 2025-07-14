package com.Shubham.projects.SkillSeeker.Dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestDto {

    private long total;
    private List<CourseDto> courses;
    private int page;
    private int size;
    private int totalPages;

}
