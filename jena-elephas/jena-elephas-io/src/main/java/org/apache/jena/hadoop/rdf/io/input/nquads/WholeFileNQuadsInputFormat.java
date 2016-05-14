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

package org.apache.jena.hadoop.rdf.io.input.nquads;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.hadoop.rdf.io.input.AbstractWholeFileInputFormat;
import org.apache.jena.hadoop.rdf.io.input.readers.nquads.WholeFileNQuadsReader;
import org.apache.jena.hadoop.rdf.types.QuadWritable;


/**
 * NQuads input format where files are processed as complete files rather than
 * in a line based manner as with the {@link NQuadsInputFormat}
 * <p>
 * This has the advantage of less parser setup overhead but the disadvantage
 * that the input cannot be split over multiple mappers.
 * </p>
 * 
 * 
 * 
 */
public class WholeFileNQuadsInputFormat extends AbstractWholeFileInputFormat<LongWritable, QuadWritable> {

    @Override
    public RecordReader<LongWritable, QuadWritable> createRecordReader(InputSplit split, TaskAttemptContext context) {
        return new WholeFileNQuadsReader();
    }

}
