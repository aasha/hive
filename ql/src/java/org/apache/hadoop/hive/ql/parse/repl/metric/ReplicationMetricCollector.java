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
package org.apache.hadoop.hive.ql.parse.repl.metric;

import org.apache.hadoop.hive.metastore.utils.StringUtils;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.parse.repl.metric.event.ReplicationMetric;
import org.apache.hadoop.hive.ql.parse.repl.metric.event.Metadata;
import org.apache.hadoop.hive.ql.parse.repl.metric.event.Progress;
import org.apache.hadoop.hive.ql.parse.repl.metric.event.Stage;
import org.apache.hadoop.hive.ql.parse.repl.metric.event.Status;
import org.apache.hadoop.hive.ql.parse.repl.metric.event.Metric;

import java.util.Map;

/**
 * Abstract class for Replication Metric Collection.
 */
public abstract class ReplicationMetricCollector {
  private ReplicationMetric replicationMetric;
  private MetricCollector metricCollector;
  private boolean isEnabled;

  public ReplicationMetricCollector(String dbName, Metadata.ReplicationType replicationType,
                             String stagingDir, String policy, long executionId,
                                    long dumpExecutionId, long maxCacheSize) {
    if (!StringUtils.isEmpty(policy) && executionId > 0) {
      isEnabled = true;
      metricCollector = MetricCollector.getInstance().init(maxCacheSize);
      Metadata metadata = new Metadata(dbName, replicationType, stagingDir);
      replicationMetric = new ReplicationMetric(executionId, policy, dumpExecutionId, metadata);
    }
  }

  public void reportStageStart(String stageName, Map<String, Long> metricMap) throws SemanticException {
    if (isEnabled) {
      Progress progress = replicationMetric.getProgress();
      Stage stage = new Stage(stageName, Status.IN_PROGRESS, System.currentTimeMillis());
      for (Map.Entry<String, Long> metric : metricMap.entrySet()) {
        stage.addMetric(new Metric(metric.getKey(), metric.getValue()));
      }
      progress.addStage(stage);
      replicationMetric.setProgress(progress);
      metricCollector.addMetric(replicationMetric);
    }
  }


  public void reportStageEnd(String stageName, Status status, long lastReplId) throws SemanticException {
    if (isEnabled) {
      Progress progress = replicationMetric.getProgress();
      Stage stage = progress.getStageByName(stageName);
      stage.setStatus(status);
      stage.setEndTime(System.currentTimeMillis());
      progress.addStage(stage);
      replicationMetric.setProgress(progress);
      Metadata metadata = replicationMetric.getMetadata();
      metadata.setLastReplId(lastReplId);
      replicationMetric.setMetadata(metadata);
      metricCollector.addMetric(replicationMetric);
    }
  }

  public void reportStageEnd(String stageName, Status status) throws SemanticException {
    if (isEnabled) {
      Progress progress = replicationMetric.getProgress();
      Stage stage = progress.getStageByName(stageName);
      stage.setStatus(status);
      stage.setEndTime(System.currentTimeMillis());
      progress.addStage(stage);
      replicationMetric.setProgress(progress);
      metricCollector.addMetric(replicationMetric);
    }
  }

  public void reportStageProgress(String stageName, String metricName, long count) throws SemanticException {
    if (isEnabled) {
      Progress progress = replicationMetric.getProgress();
      Stage stage = progress.getStageByName(stageName);
      Metric metric = stage.getMetricByName(metricName);
      metric.setCurrentCount(metric.getCurrentCount() + count);
      if (metric.getCurrentCount() > metric.getTotalCount()) {
        metric.setTotalCount(metric.getCurrentCount());
      }
      stage.addMetric(metric);
      progress.addStage(stage);
      replicationMetric.setProgress(progress);
      metricCollector.addMetric(replicationMetric);
    }
  }

  public void reportEnd(Status status) throws SemanticException {
    if (isEnabled) {
      Progress progress = replicationMetric.getProgress();
      progress.setStatus(status);
      replicationMetric.setProgress(progress);
      metricCollector.addMetric(replicationMetric);
    }
  }
}
