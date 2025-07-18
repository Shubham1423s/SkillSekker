package com.Shubham.projects.SkillSeeker.Service;

import com.Shubham.projects.SkillSeeker.Document.CourseDocument;
import com.Shubham.projects.SkillSeeker.Repository.CourseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
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

    @Autowired
    private CourseRepository courseRepository;

    @PostConstruct
    public void loadSampleData() {
        try {

            if (courseRepository.count() < 52) {
                System.out.println("Loading fresh data...");
                courseRepository.deleteAll(); // Clear existing data
            } else {
                System.out.println("All 52 courses already loaded.");
                return;
            }

            // Load JSON file
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("sample-courses.json");

            List<Map<String, Object>> coursesData = mapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {}
            );

            for (Map<String, Object> courseData : coursesData) {
                CourseDocument course = new CourseDocument();
                course.setId((String) courseData.get("id"));
                course.setTitle((String) courseData.get("title"));
                course.setDescription((String) courseData.get("description"));
                course.setCategory((String) courseData.get("category"));
                course.setType((String) courseData.get("type"));
                course.setGradeRange((String) courseData.get("gradeRange"));
                course.setMinAge((Integer) courseData.get("minAge"));
                course.setMaxAge((Integer) courseData.get("maxAge"));
                course.setPrice((Double) courseData.get("price"));


                String dateString = (String) courseData.get("nextSessionDate");
                LocalDateTime sessionDate = LocalDateTime.parse(
                        dateString,
                        DateTimeFormatter.ISO_DATE_TIME
                );
                course.setNextSessionDate(sessionDate.toLocalDate());


                course.setSuggestFromTitle();

                courseRepository.save(course);
            }

            System.out.println("Sample data loaded successfully!");
            System.out.println("Total courses loaded: " + courseRepository.count());

        } catch (IOException e) {
            System.err.println("Error loading sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}