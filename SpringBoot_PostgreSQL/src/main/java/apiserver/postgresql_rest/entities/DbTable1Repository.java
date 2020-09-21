package apiserver.postgresql_rest.entities;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/** @author Stefan */
// for PostgreSQL JSONB operators see:
// https://www.postgresql.org/docs/11/functions-json.html
public interface DbTable1Repository extends CrudRepository<DbTable1, Long> {

  // for nativeQuery use table and fields names as in the table create SQL (DDL) and not as in java
  // entity class!
  @Query(
      value = "SELECT id, data_json FROM DB_TABLE_1 t WHERE t.data_json->>'name' = ?1 limit 10",
      nativeQuery = true)
  public List<DbTable1> findFirst10ByName(String name);

  @Query(
      value =
          "SELECT id, data_json FROM DB_TABLE_1 t WHERE CAST (t.data_json->>'counterUpdates' AS INTEGER) >= ?1 limit 10",
      nativeQuery = true)
  public List<DbTable1> findTop10ByCounterUpdatesGreaterThan(long minCounterUpdates);
}
