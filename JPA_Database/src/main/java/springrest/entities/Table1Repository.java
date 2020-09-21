
package springrest.entities;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author stefan
 */

// This will be AUTO IMPLEMENTED by Spring into a Bean called table1Repository
// CRUD refers Create, Read, Update, Delete
public interface Table1Repository extends CrudRepository<TestDbTable1, Long> {
    
    @Query("SELECT t FROM TestDbTable1 t WHERE (t.id) <= (:idMax)")
    public List<TestDbTable1> query1(@Param("idMax") Long idMax);
}
