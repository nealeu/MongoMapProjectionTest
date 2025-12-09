package org.example.mongoemptymaptest;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.count;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.mongodb.MongoDBContainer;

@SpringBootTest
class MongoMapProjectionTest {

    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.25");

    @BeforeAll
    static void beforeAll() {
        mongoDBContainer.start();
    }

    @AfterAll
    static void afterAll() {
        mongoDBContainer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    }

    @Autowired
    MongoOperations mongoOperations;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void contextLoads() {
        // This empty method will fail if the context fails to load.
        // This test method ensures that the Spring application context loads successfully.
    }


  @Document(collection = "items")
  public static class Item {
    private String id;
    private String name;

    public Item() {}

    public Item(String name) {
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @BeforeEach
  void setup() {
    mongoOperations.dropCollection(Item.class);
    mongoOperations.save(new Item("Item 1"));
    mongoOperations.save(new Item("Item 2"));
    mongoOperations.save(new Item("Item 3"));
  }

  @Test
  void testAggregationWithMapClass_ShowsRegression() {
    // This demonstrates the regression
    var aggregation = newAggregation(Item.class, count().as("count"));

    var result = mongoOperations.aggregate(aggregation, Map.class);
    Map<?, ?> uniqueResult = result.getUniqueMappedResult();

    // REGRESSION: This assertion may fail
    assert uniqueResult != null : "REGRESSION: getUniqueMappedResult() returned null";
    assert uniqueResult.containsKey("count") : "REGRESSION: Result missing 'count' key";

    Number count = (Number) uniqueResult.get("count");
    assert count.longValue() == 3L : "Expected 3, got " + count.longValue();
  }

  @Test
  void testAggregationWithDocumentClass_Workaround() {
    // This is the workaround that should work
    var aggregation = newAggregation(Item.class, count().as("count"));

    var result = mongoOperations.aggregate(aggregation, org.bson.Document.class);
    org.bson.Document uniqueResult = result.getUniqueMappedResult();

    assert uniqueResult != null : "getUniqueMappedResult() returned null";
    assert uniqueResult.containsKey("count") : "Result missing 'count' key";

    Number count = (Number) uniqueResult.get("count");
    assert count.longValue() == 3L : "Expected 3, got " + count.longValue();
  }
}
