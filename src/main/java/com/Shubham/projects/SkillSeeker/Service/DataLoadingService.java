package com.Shubham.projects.SkillSeeker.Service;

import com.Shubham.projects.SkillSeeker.Document.CourseDocument;
import com.Shubham.projects.SkillSeeker.Repository.CourseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class DataLoadingService {

    private static final Logger log = LoggerFactory.getLogger(DataLoadingService.class);

    @Autowired
    private CourseRepository courseRepository;

    private final ObjectMapper mapper;

    public DataLoadingService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void loadSampleData() {
        try {
            long existingCount = courseRepository.count();
            if (existingCount > 0) {
                log.info("Data already exists. Total courses: {}. Skipping data loading.", existingCount);
                return;
            }

            log.info("Loading sample data from JSON file...");

            ClassPathResource resource = new ClassPathResource("sample-courses.json");

            if (!resource.exists()) {
                log.error("Sample data file not found: sample-courses.json");
                return;
            }

            List<Map<String, Object>> coursesData = mapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            List<CourseDocument> courses = coursesData.stream()
                    .map(this::mapToCourseDocument)
                    .toList();

            courseRepository.saveAll(courses);

            log.info("Sample data loaded successfully! Total courses loaded: {}", courses.size());

        } catch (IOException e) {
            log.error("Error loading sample data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load sample data", e);
        } catch (Exception e) {
            log.error("Unexpected error during data loading: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load sample data", e);
        }
    }

    private CourseDocument mapToCourseDocument(Map<String, Object> courseData) {
        try {
            CourseDocument course = new CourseDocument();

            course.setId((String) courseData.get("id"));
            course.setTitle((String) courseData.get("title"));
            course.setDescription((String) courseData.get("description"));
            course.setCategory((String) courseData.get("category"));
            course.setType((String) courseData.get("type"));
            course.setGradeRange((String) courseData.get("gradeRange"));

            course.setMinAge(convertToInteger(courseData.get("minAge")));
            course.setMaxAge(convertToInteger(courseData.get("maxAge")));
            course.setPrice(convertToDouble(courseData.get("price")));

            String dateString = (String) courseData.get("nextSessionDate");
            if (dateString != null) {
                LocalDateTime sessionDate = LocalDateTime.parse(
                        dateString.replace("Z", ""),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
                course.setNextSessionDate(sessionDate);
            }

            return course;

        } catch (Exception e) {
            log.error("Error mapping course data: {}", courseData, e);
            throw new RuntimeException("Failed to map course data", e);
        }
    }

    private Integer convertToInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    private Double convertToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}