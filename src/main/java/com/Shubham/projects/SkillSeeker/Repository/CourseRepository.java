package com.Shubham.projects.SkillSeeker.Repository;

import com.Shubham.projects.SkillSeeker.Document.CourseDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {
}
