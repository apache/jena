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

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * Abstract implementation of a while file input format where each file is a
 * single split
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Value type
 */
public abstract class AbstractWholeFileInputFormat<TKey, TValue> extends FileInputFormat<TKey, TValue> {

    @Override
    protected final boolean isSplitable(JobContext context, Path filename) {
        return false;
    }
}
