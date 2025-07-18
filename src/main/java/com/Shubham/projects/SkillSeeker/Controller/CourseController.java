package com.Shubham.projects.SkillSeeker.Controller;

import com.Shubham.projects.SkillSeeker.Dto.SearchRequestDto;
import com.Shubham.projects.SkillSeeker.Dto.SearchResponseDto;
import com.Shubham.projects.SkillSeeker.Service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponseDto> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(defaultValue = "upcoming") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            SearchRequestDto request = new SearchRequestDto(q, minAge, maxAge, category, type,
                    minPrice, maxPrice, startDate, sort, page, size);
            SearchResponseDto response = courseService.searchCourses(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/suggest")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String q) {
        try {
            List<String> suggestions = courseService.getSuggestions(q);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
