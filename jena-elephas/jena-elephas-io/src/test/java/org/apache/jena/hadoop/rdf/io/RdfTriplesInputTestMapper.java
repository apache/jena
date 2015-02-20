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

package org.apache.jena.hadoop.rdf.io;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.log4j.Logger;


/**
 * A test mapper which takes in line based RDF triple input and just produces triples
 * 
 *
 */
public class RdfTriplesInputTestMapper extends Mapper<LongWritable, TripleWritable, NullWritable, TripleWritable> {
    
    private static final Logger LOG = Logger.getLogger(RdfTriplesInputTestMapper.class);

    @Override
    protected void map(LongWritable key, TripleWritable value, Context context)
            throws IOException, InterruptedException {
        LOG.info("Line " + key.toString() + " => " + value.toString());
        context.write(NullWritable.get(), value);
    }

    
}
