# Course Search Application - Spring Boot + Elasticsearch

A Spring Boot application that provides course search functionality with Elasticsearch backend, including full-text search, filtering, sorting, pagination, and autocomplete features.

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Docker and Docker Compose
- Git

## Project Structure

```
course-search/
├── src/
│   ├── main/
│   │   ├── java/com/example/coursesearch/
│   │   │   ├── config/
│   │   │   │   └── ElasticConfig.java
│   │   │   ├── controller/
│   │   │   │   └── CourseController.java
│   │   │   ├── document/
│   │   │   │   └── CourseDocument.java
│   │   │   ├── dto/
│   │   │   │   ├── SearchRequest.java
│   │   │   │   └── SearchResponse.java
│   │   │   ├── repository/
│   │   │   │   └── CourseRepository.java
│   │   │   ├── service/
│   │   │   │   ├── CourseSearchService.java
│   │   │   │   └── DataLoadingService.java
│   │   │   └── CourseSearchApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── sample-courses.json
│   └── test/
│       └── java/com/example/coursesearch/
│           └── CourseSearchIntegrationTest.java
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd course-search
```

### 2. Start Elasticsearch

```bash
docker-compose up -d
```

Verify Elasticsearch is running:
```bash
curl http://localhost:9200
```

You should see a JSON response with cluster information.

### 3. Build and Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Verify Data Loading

The application automatically loads sample data on startup. Check the console for:
```
Loaded X courses into Elasticsearch
```

## API Endpoints

### Search Courses - `/api/search`

**GET** `/api/search`

**Query Parameters:**
- `q` (optional): Search keyword for title and description
- `minAge` (optional): Minimum age filter
- `maxAge` (optional): Maximum age filter
- `category` (optional): Course category filter
- `type` (optional): Course type filter (ONE_TIME, COURSE, CLUB)
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter
- `startDate` (optional): Start date filter (ISO-8601 format)
- `sort` (optional): Sort order (upcoming, priceAsc, priceDesc) - default: upcoming
- `page` (optional): Page number (0-based) - default: 0
- `size` (optional): Page size - default: 10

**Example Requests:**

1. **Basic search:**
```bash
curl "http://localhost:8080/api/search?q=physics"
```

2. **Search with filters:**
```bash
curl "http://localhost:8080/api/search?q=science&minAge=10&maxAge=15&category=Science"
```

3. **Search with price range:**
```bash
curl "http://localhost:8080/api/search?minPrice=50&maxPrice=150"
```

4. **Search with sorting:**
```bash
curl "http://localhost:8080/api/search?sort=priceAsc&page=0&size=5"
```

5**Complex search:**
```bash
curl "http://localhost:8080/api/search?q=math&category=Math&type=COURSE&minPrice=100&sort=priceDesc"
```

**Response Format:**
```json
{
  "total": 25,
  "courses": [
    {
      "id": "1",
      "title": "Introduction to Physics",
      "description": "Basic physics concepts for beginners",
      "category": "Science",
      "type": "COURSE",
      "gradeRange": "6th-8th",
      "minAge": 11,
      "maxAge": 14,
      "price": 150.00,
      "nextSessionDate": "2025-08-15T10:00:00"
    }
  ]
}
```

### Autocomplete Suggestions - `/api/search/suggest`

**GET** `/api/search/suggest`

**Query Parameters:**
- `q` (required): Partial course title for autocomplete

**Example Requests:**

1. **Get suggestions:**
```bash
curl "http://localhost:8080/api/search/suggest?q=phy"
```

2. **Get suggestions for partial word:**
```bash
curl "http://localhost:8080/api/search/suggest?q=math"
```

**Response Format:**
```json
[
  "Introduction to Physics",
  "Advanced Physics Lab",
  "Physics for Beginners"
]
```

## Features

### Assignment A (Required Features)
- ✅ Full-text search on title and description
- ✅ Age range filtering (minAge/maxAge)
- ✅ Price range filtering (minPrice/maxPrice)
- ✅ Category and type exact filtering
- ✅ Date filtering for upcoming sessions
- ✅ Multiple sorting options (date, price ascending/descending)
- ✅ Pagination support
- ✅ Proper Elasticsearch mapping and indexing

### Assignment B (Bonus Features)
- ✅ Autocomplete suggestions using Elasticsearch completion suggester
- ✅ Fuzzy search for handling typos in search queries
- ✅ Enhanced search experience


## Testing

### Manual Testing

1. **Test basic search:**
```bash
curl "http://localhost:8080/api/search?q=physics"
```

2. **Test fuzzy search (typos):**
```bash
curl "http://localhost:8080/api/search?q=phisics"
```

3. **Test filters:**
```bash
curl "http://localhost:8080/api/search?category=Science&minAge=10&maxAge=15"
```

4. **Test sorting:**
```bash
curl "http://localhost:8080/api/search?sort=priceAsc"
```

5. **Test pagination:**
```bash
curl "http://localhost:8080/api/search?page=1&size=3"
```

6. **Test autocomplete:**
```bash
curl "http://localhost:8080/api/search/suggest?q=intro"
```



## Configuration

### Application Configuration

```yaml
  spring:
  data:
     elasticsearch:
        repositories:
           enabled: true
  elasticsearch:
     uris: http://localhost:9200
     connection-timeout: 10s
     socket-timeout: 60s

server:
   port: 8080

logging:
   level:
      org.springframework.data.elasticsearch: DEBUG
      org.elasticsearch: DEBUG
```

### Docker Compose

The `docker-compose.yml` file sets up:
- Elasticsearch 8.5.0 (single-node cluster)
- Disabled security for development
- Exposed ports: 9200, 9300
- Persistent data volume

## Troubleshooting

### Common Issues

1. **Elasticsearch not starting:**
    - Check Docker is running
    - Verify ports 9200/9300 are available
    - Check logs: `docker-compose logs elasticsearch`

2. **No data loaded:**
    - Ensure `sample-courses.json` is in `src/main/resources`
    - Check application startup logs
    - Verify Elasticsearch is accessible
    - Check application.yml configuration

3. **Search not working:**
    - Check if data is indexed: `curl "http://localhost:9200/courses/_search"`
    - Verify application.properties configuration
    - Check application logs for errors

4. **Connection refused:**
    - Ensure Elasticsearch is running on localhost:9200
    - Check firewall settings
    - Verify network connectivity

### Health Checks

1. **Check Elasticsearch health:**
```bash
curl http://localhost:9200/_cluster/health
```

2. **Check indexed data:**
```bash
curl "http://localhost:9200/courses/_search?size=5"
```

3. **Check application health:**
```bash
curl http://localhost:8080/actuator/health
```

## Development Notes

- Uses Spring Boot 3.0+ with Spring Data Elasticsearch
- Lombok for reducing boilerplate code
- Jackson for JSON processing
- Basic exception handling implemented
- Elasticsearch completion suggester for autocomplete
- Fuzzy matching enabled for search queries

## Performance Considerations

- Elasticsearch queries use filters for better performance
- Pagination prevents loading large result sets
- Proper indexing with appropriate field types
- Connection pooling and timeout configuration

## Security Notes

- Development setup with security disabled
- For production, enable Elasticsearch security
- Add authentication and authorization
- Use HTTPS for API endpoints
- Validate and sanitize user inputs

## Future Enhancements

- Add more sophisticated search relevance scoring
- Implement faceted search
- Add real-time data updates
- Implement caching layer
- Add comprehensive monitoring and logging
- Add API rate limiting
- Implement user authentication and personalization