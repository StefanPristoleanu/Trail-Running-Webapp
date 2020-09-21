package apiserver.mongodb_rest.entities;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/** @author */
public interface DbTable1Repository extends MongoRepository<DbTable1, String> {

  // https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/
  public List<DbTable1> findFirst10ByName(String name);

  public List<DbTable1> findTop10ByCounterUpdatesGreaterThan(long minCounterUpdates);

  public List<DbTable1> findByTrailCreatedBy(long trailCreatedBy);
}
