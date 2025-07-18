package com.Shubham.projects.SkillSeeker.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto {
    private long total;
    private List<CourseDto> courses;
    private int page;
    private int size;
}
