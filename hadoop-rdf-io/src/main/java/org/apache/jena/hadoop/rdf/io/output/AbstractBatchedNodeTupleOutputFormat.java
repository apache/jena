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

package org.apache.jena.hadoop.rdf.io.output;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.hadoop.rdf.io.RdfIOConstants;
import org.apache.jena.hadoop.rdf.io.output.writers.AbstractBatchedNodeTupleWriter;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;


/**
 * Abstract output format for formats that use a
 * {@link AbstractBatchedNodeTupleWriter} as their writer
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractBatchedNodeTupleOutputFormat<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractNodeTupleOutputFormat<TKey, TValue, T> {

    @Override
    protected RecordWriter<TKey, T> getRecordWriter(Writer writer, Configuration config) {
        long batchSize = config.getLong(RdfIOConstants.OUTPUT_BATCH_SIZE, RdfIOConstants.DEFAULT_OUTPUT_BATCH_SIZE);
        return this.getRecordWriter(writer, batchSize);
    }
    
    protected abstract RecordWriter<TKey, T> getRecordWriter(Writer writer, long batchSize);

}
