/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.ql.parse.repl.load.metric;

import org.apache.hadoop.hive.ql.parse.repl.metric.ReplicationMetricCollector;
import org.apache.hadoop.hive.ql.parse.repl.metric.event.Metadata;

/**
 * BootstrapLoadMetricCollector.
 * Bootstrap Load Metric Collector
 */
public class BootstrapLoadMetricCollector extends ReplicationMetricCollector {
  public BootstrapLoadMetricCollector(String dbName, String stagingDir, String policy,
                                      long executionId, long dumpExecutionId, long maxCacheSize) {
    super(dbName, Metadata.ReplicationType.BOOTSTRAP, stagingDir, policy, executionId, dumpExecutionId, maxCacheSize);
  }
}
