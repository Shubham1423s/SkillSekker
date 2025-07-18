package com.Shubham.projects.SkillSeeker.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.json.JsonData;
import com.Shubham.projects.SkillSeeker.Document.CourseDocument;
import com.Shubham.projects.SkillSeeker.Dto.CourseDto;
import com.Shubham.projects.SkillSeeker.Dto.SearchRequestDto;
import com.Shubham.projects.SkillSeeker.Dto.SearchResponseDto;
import com.Shubham.projects.SkillSeeker.Repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public SearchResponseDto searchCourses(SearchRequestDto request) {
        try {
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            // Full-text fuzzy search with field boosting
            if (request.getQ() != null && !request.getQ().trim().isEmpty()) {
                // Title search with boost
                boolQueryBuilder.should(s -> s
                        .multiMatch(mm -> mm
                                .fields("title")
                                .query(request.getQ())
                                .fuzziness("AUTO")
                                .boost(2.0f)
                        )
                );

                // Description search
                boolQueryBuilder.should(s -> s
                        .multiMatch(mm -> mm
                                .fields("description")
                                .query(request.getQ())
                                .fuzziness("AUTO")
                        )
                );

                // Set minimum should match to ensure at least one clause matches
                boolQueryBuilder.minimumShouldMatch("1");
            }

            // Category filter
            if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
                boolQueryBuilder.filter(f -> f
                        .term(t -> t
                                .field("category")
                                .value(request.getCategory())
                        )
                );
            }

            // Type filter
            if (request.getType() != null && !request.getType().trim().isEmpty()) {
                boolQueryBuilder.filter(f -> f
                        .term(t -> t
                                .field("type")
                                .value(request.getType())
                        )
                );
            }

            // Age filters
            if (request.getMinAge() != null) {
                boolQueryBuilder.filter(f -> f
                        .range(r -> r
                                .field("maxAge")
                                .gte(JsonData.of(request.getMinAge()))
                        )
                );
            }
            if (request.getMaxAge() != null) {
                boolQueryBuilder.filter(f -> f
                        .range(r -> r
                                .field("minAge")
                                .lte(JsonData.of(request.getMaxAge()))
                        )
                );
            }

            // Price filters
            if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                RangeQuery.Builder rangeQueryBuilder = new RangeQuery.Builder().field("price");
                if (request.getMinPrice() != null) {
                    rangeQueryBuilder.gte(JsonData.of(request.getMinPrice()));
                }
                if (request.getMaxPrice() != null) {
                    rangeQueryBuilder.lte(JsonData.of(request.getMaxPrice()));
                }
                boolQueryBuilder.filter(f -> f.range(rangeQueryBuilder.build()));
            }

            // Start date filter
            if (request.getStartDate() != null) {
                String formattedStartDate;

                // Handle common Java time types
                if (request.getStartDate() instanceof java.time.temporal.TemporalAccessor) {
                    if (request.getStartDate() instanceof java.time.LocalDateTime) {
                        formattedStartDate = ((java.time.LocalDateTime) request.getStartDate()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//                    } else if (request.getStartDate() instanceof java.time.LocalDate) {
//                        formattedStartDate = ((java.time.LocalDate) request.getStartDate()).format(DateTimeFormatter.ISO_LOCAL_DATE);
                    } else {
                        formattedStartDate = request.getStartDate().toString();
                    }
                } else {
                    formattedStartDate = request.getStartDate().toString();
                    log.warn("Unhandled date type for startDate: {}. Using toString().", request.getStartDate().getClass().getName());
                }

                boolQueryBuilder.filter(f -> f
                        .range(r -> r
                                .field("nextSessionDate")
                                .gte(JsonData.of(formattedStartDate))
                        )
                );
            }

            Query finalQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

            // Sorting & Pagination
            Sort sort = getSortOrder(request.getSort());
            int page = Math.max(request.getPage(), 0);
            int size = Math.min(Math.max(request.getSize(), 1), 100);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Search query using NativeQuery
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(finalQuery)
                    .withPageable(pageable)
                    .build();

            SearchHits<CourseDocument> searchHits = elasticsearchOperations.search(searchQuery, CourseDocument.class);

            List<CourseDto> courses = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return new SearchResponseDto(searchHits.getTotalHits(), courses, page, size);

        } catch (Exception e) {
            log.error("Search failed: {}", e.getMessage(), e);
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    public List<String> getSuggestions(String query) {
        try {

            Suggester suggester = Suggester.of(s -> s
                    .suggesters("course_suggest", FieldSuggester.of(fs -> fs
                            .completion(c -> c
                                    .field("suggest")
//                                    .prefix(query)
                                    .skipDuplicates(true)
                                    .size(10)
                            )
                    ))
            );

            NativeQuery searchQuery = NativeQuery.builder()
                    .withSuggester(suggester)
                    .build();

            SearchHits<CourseDocument> searchHits = elasticsearchOperations.search(searchQuery, CourseDocument.class);

            List<String> suggestionsList = new ArrayList<>();


            if (searchHits.getSuggest() != null) {
                org.springframework.data.elasticsearch.core.suggest.response.Suggest.Suggestion<? extends org.springframework.data.elasticsearch.core.suggest.response.Suggest.Suggestion.Entry<? extends org.springframework.data.elasticsearch.core.suggest.response.Suggest.Suggestion.Entry.Option>> suggestion =
                        searchHits.getSuggest().getSuggestion("course_suggest");

                if (suggestion != null) {
                    for (org.springframework.data.elasticsearch.core.suggest.response.Suggest.Suggestion.Entry<? extends org.springframework.data.elasticsearch.core.suggest.response.Suggest.Suggestion.Entry.Option> entry : suggestion.getEntries()) {
                        for (org.springframework.data.elasticsearch.core.suggest.response.Suggest.Suggestion.Entry.Option option : entry.getOptions()) {
//                            suggestionsList.add(option.getText().string());
                        }
                    }
                }
            }

            return suggestionsList;

        } catch (Exception e) {
            log.error("Suggestion fetch failed: {}", e.getMessage(), e);
            throw new RuntimeException("Suggestion fetch failed: " + e.getMessage(), e);
        }
    }

    public long getTotalCourses() {
        return courseRepository.count();
    }

    public List<CourseDto> getAllCourses() {
        List<CourseDocument> documents = new ArrayList<>();
        courseRepository.findAll().forEach(documents::add);
        return documents.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private Sort getSortOrder(String sortParam) {
        if (sortParam == null || sortParam.trim().isEmpty()) {
            sortParam = "upcoming";
        }

        return switch (sortParam.toLowerCase()) {
            case "priceasc" -> Sort.by(Sort.Direction.ASC, "price");
            case "pricedesc" -> Sort.by(Sort.Direction.DESC, "price");
            case "upcoming" -> Sort.by(Sort.Direction.ASC, "nextSessionDate");
            default -> Sort.by(Sort.Direction.ASC, "nextSessionDate");
        };
    }

    private CourseDto convertToDto(CourseDocument doc) {
        return new CourseDto(
                doc.getId(),
                doc.getTitle(),
                doc.getDescription(),
                doc.getCategory(),
                doc.getType(),
                doc.getGradeRange(),
                doc.getMinAge(),
                doc.getMaxAge(),
                doc.getPrice(),
                doc.getNextSessionDate().atStartOfDay()
        );
    }
}