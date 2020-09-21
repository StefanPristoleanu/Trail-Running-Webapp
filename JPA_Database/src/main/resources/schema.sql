CREATE TABLE IF NOT EXISTS TEST_DB_TABLE_1 (
    id BIGINT NOT NULL AUTO_INCREMENT,
    test_name varchar(100) NOT NULL,
    value NUMERIC,
    update_counter NUMERIC,
    last_update DATETIME NULL,
    info varchar(500) NULL,
    CONSTRAINT test_db_table_pk PRIMARY KEY (id),
    CONSTRAINT test_db_table_un UNIQUE (test_name));