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

package org.apache.jena.hadoop.rdf.io.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;

/**
 * Abstract line based input format that reuses the machinery from
 * {@link NLineInputFormat} to calculate the splits
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Value type
 */
public abstract class AbstractNLineFileInputFormat<TKey, TValue> extends FileInputFormat<TKey, TValue> {

    /**
     * Logically splits the set of input files for the job, splits N lines of
     * the input as one split.
     * 
     * @see FileInputFormat#getSplits(JobContext)
     */
    public final List<InputSplit> getSplits(JobContext job) throws IOException {
        List<InputSplit> splits = new ArrayList<InputSplit>();
        int numLinesPerSplit = NLineInputFormat.getNumLinesPerSplit(job);
        for (FileStatus status : listStatus(job)) {
            splits.addAll(NLineInputFormat.getSplitsForFile(status, job.getConfiguration(), numLinesPerSplit));
        }
        return splits;
    }
}
