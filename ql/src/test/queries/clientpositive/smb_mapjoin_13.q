--! qt:dataset:src
set hive.optimize.bucketmapjoin = true;
set hive.optimize.bucketmapjoin.sortedmerge = true;
set hive.input.format = org.apache.hadoop.hive.ql.io.BucketizedHiveInputFormat;
set hive.cbo.enable=false;

set hive.exec.reducers.max = 1;
set hive.merge.mapfiles=false;
set hive.merge.mapredfiles=false; 

-- This test verifies that the sort merge join optimizer works when the tables are joined on columns with different names

-- Create bucketed and sorted tables
CREATE TABLE test_table1_n12 (key INT, value STRING) CLUSTERED BY (key) SORTED BY (key ASC) INTO 16 BUCKETS;
CREATE TABLE test_table2_n12 (value INT, key STRING) CLUSTERED BY (value) SORTED BY (value ASC) INTO 16 BUCKETS;
CREATE TABLE test_table3_n6 (key INT, value STRING) CLUSTERED BY (key, value) SORTED BY (key ASC, value ASC) INTO 16 BUCKETS;
CREATE TABLE test_table4_n0 (key INT, value STRING) CLUSTERED BY (key, value) SORTED BY (value ASC, key ASC) INTO 16 BUCKETS;

FROM src
INSERT OVERWRITE TABLE test_table1_n12 SELECT *
INSERT OVERWRITE TABLE test_table2_n12 SELECT *
INSERT OVERWRITE TABLE test_table3_n6 SELECT *
INSERT OVERWRITE TABLE test_table4_n0 SELECT *;

-- Join data from 2 tables on their respective sorted columns (one each, with different names) and
-- verify sort merge join is used
EXPLAIN EXTENDED
SELECT /*+ MAPJOIN(b) */ * FROM test_table1_n12 a JOIN test_table2_n12 b ON a.key = b.value ORDER BY a.key LIMIT 10;

SELECT /*+ MAPJOIN(b) */ * FROM test_table1_n12 a JOIN test_table2_n12 b ON a.key = b.value ORDER BY a.key LIMIT 10;

-- Join data from 2 tables on their respective columns (two each, with the same names but sorted
-- with different priorities) and verify sort merge join is not used
EXPLAIN EXTENDED
SELECT /*+ MAPJOIN(b) */ * FROM test_table3_n6 a JOIN test_table4_n0 b ON a.key = b.value ORDER BY a.key LIMIT 10;

SELECT /*+ MAPJOIN(b) */ * FROM test_table3_n6 a JOIN test_table4_n0 b ON a.key = b.value ORDER BY a.key LIMIT 10;
