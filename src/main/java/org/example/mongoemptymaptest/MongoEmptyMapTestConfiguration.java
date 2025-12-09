package org.example.mongoemptymaptest;

import com.mongodb.MongoClientSettings;
import org.bson.UuidRepresentation;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ComponentScan
@EnableMongoRepositories
public class MongoEmptyMapTestConfiguration extends AbstractMongoClientConfiguration {

  @Override
  protected String getDatabaseName() {
    return "test";
  }

  @Override
  protected void configureClientSettings(MongoClientSettings.Builder builder) {
    builder.uuidRepresentation(UuidRepresentation.STANDARD);
  }

}
