package com.Shubham.projects.SkillSeeker.Document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Document(indexName = "courses")
public class CourseDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String gradeRange;

    @Field(type = FieldType.Integer)
    private Integer minAge;

    @Field(type = FieldType.Integer)
    private Integer maxAge;

    @Field(type = FieldType.Double)
    private Double price;

    // Change to LocalDate since your stored data is date-only
    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate nextSessionDate;

    // Make suggest field optional - it will be null if not present
    @CompletionField
    private Completion suggest;

    public void setSuggestFromTitle() {
        if (this.title != null) {
            this.suggest = new Completion();
            this.suggest.setInput(new String[]{this.title});
        }
    }
}