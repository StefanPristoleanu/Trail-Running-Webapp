package apiserver.postgresql_rest.entities;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/** @author */
// for PostgreSQL JSONB operators see:
// https://www.postgresql.org/docs/11/functions-json.html
public interface MyTableRepository extends CrudRepository<MyTableModel, Long> {

  // Important Note: for nativeQuery use table and fields names as in the table create SQL (DDL) and
  // not the java
  // entity class name!
  // select * from db_table_1 where data_json->>'name'='thread_1' order by
  // data_json->>'lastUpdatedAt' desc limit 10;
  /*@Query(
  value = "SELECT id, data_json FROM DB_TABLE_1 t WHERE t.data_json->>'name' = ?1 order by data_json->>'lastUpdatedAt' desc limit 10",
  nativeQuery = true) */
  @Query(
      value =
          "SELECT * FROM DB_TABLE_2 t WHERE t.name = ?1 order by t.last_updated_at desc limit 10",
      nativeQuery = true)
  public List<MyTableModel> findFirst10ByName(String name);

  @Query(
      value =
          "SELECT id, data_json FROM DB_TABLE_2 t WHERE CAST (t.data_json->>'counterUpdates' AS INTEGER) >= ?1 limit 10",
      nativeQuery = true)
  public List<MyTableModel> findTop10ByCounterUpdatesGreaterThan(long minCounterUpdates);

  @Query(value = "SELECT * FROM DB_TABLE_2 t WHERE t.TRAIL_CREATED_BY = ?1 ", nativeQuery = true)
  public List<MyTableModel> findByUserId(Long userId);
}
