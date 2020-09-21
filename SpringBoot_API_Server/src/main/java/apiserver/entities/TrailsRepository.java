package apiserver.entities;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/** @author stefan */
public interface TrailsRepository extends CrudRepository<TrailEntity, Long> {
  // Important Note: for nativeQuery use table and fields names as in the table create SQL (DDL) and
  // not the java
  // entity class name!
  // select * from db_table_1 where data_json->>'name'='thread_1' order by
  // data_json->>'lastUpdatedAt' desc limit 10;
  /*@Query(
  value = "SELECT id, data_json FROM DB_TABLE_1 t WHERE t.data_json->>'name' = ?1 order by data_json->>'lastUpdatedAt' desc limit 10",
  nativeQuery = true) */
  @Query(value = "SELECT * FROM TRAILS t WHERE t.TRAIL_CREATED_BY = ?1 ", nativeQuery = true)
  public List<TrailEntity> findByUserId(Long userId);
}
