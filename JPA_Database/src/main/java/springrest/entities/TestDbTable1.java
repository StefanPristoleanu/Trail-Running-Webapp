package springrest.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author stefan - 2018.10.12
 */
/*
CREATE TABLE TEST_DB_TABLE_1 (
    id BIGINT NOT NULL AUTO_INCREMENT,
    test_name varchar(100) NOT NULL,
    value NUMERIC,
    update_counter NUMERIC,
    last_update DATETIME NULL,
    info varchar(500) NULL,
    CONSTRAINT test_db_table_pk PRIMARY KEY (id),
    CONSTRAINT test_db_table_un UNIQUE (test_name));
 */
@Entity
@Table(name = "TEST_DB_TABLE_1")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TestDbTable1.findAll", query = "SELECT t FROM TestDbTable1 t"),
    @NamedQuery(name = "TestDbTable1.findById", query = "SELECT t FROM TestDbTable1 t WHERE t.id = :id"),
    @NamedQuery(name = "TestDbTable1.findByTestName", query = "SELECT t FROM TestDbTable1 t WHERE t.testName = :testName"),
    @NamedQuery(name = "TestDbTable1.findByValue", query = "SELECT t FROM TestDbTable1 t WHERE t.value = :value"),
    @NamedQuery(name = "TestDbTable1.findByUpdateCounter", query = "SELECT t FROM TestDbTable1 t WHERE t.updateCounter = :updateCounter"),
    @NamedQuery(name = "TestDbTable1.findByLastUpdate", query = "SELECT t FROM TestDbTable1 t WHERE t.lastUpdate = :lastUpdate"),
    @NamedQuery(name = "TestDbTable1.findByInfo", query = "SELECT t FROM TestDbTable1 t WHERE t.info = :info")})
public class TestDbTable1 implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "TEST_NAME")
    private String testName;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "VALUE")
    private long value;
    @Column(name = "UPDATE_COUNTER")
    private long updateCounter;
    @Column(name = "LAST_UPDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    @Size(max = 500)
    @Column(name = "INFO")
    private String info;

    public TestDbTable1() {
    }

    public TestDbTable1(Long id) {
        this.id = id;
    }

    public TestDbTable1(Long id, String testName) {
        this.id = id;
        this.testName = testName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getUpdateCounter() {
        return updateCounter;
    }

    public void setUpdateCounter(long updateCounter) {
        this.updateCounter = updateCounter;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TestDbTable1)) {
            return false;
        }
        TestDbTable1 other = (TestDbTable1) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "isanet.springrest.entities.TestDbTable1[ id=" + id + " ]";
    }

}
