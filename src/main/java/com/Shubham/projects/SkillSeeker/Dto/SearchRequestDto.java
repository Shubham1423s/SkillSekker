package com.Shubham.projects.SkillSeeker.Dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestDto {

    private String q;
    private Integer minAge;
    private Integer maxAge;
    private String category;
    private String type;
    private Double minPrice;
    private Double maxPrice;
    private LocalDateTime startDate;
    private String sort = "upcoming";
    private int page = 0;
    private int size = 10;

}
