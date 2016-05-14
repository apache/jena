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

package org.apache.jena.hadoop.rdf.io.input.readers;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.io.registry.HadoopRdfIORegistry;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

/**
 * A record reader that reads triples from any RDF triples format
 */
public class TriplesReader extends AbstractRdfReader<Triple, TripleWritable> {

    @Override
    protected RecordReader<LongWritable, TripleWritable> selectRecordReader(Lang lang) throws IOException {
        if (!RDFLanguages.isTriples(lang))
            throw new IOException(
                    lang.getLabel()
                            + " is not a RDF triples format, perhaps you wanted QuadsInputFormat or TriplesOrQuadsInputFormat instead?");

        // This will throw an appropriate error if the language does not support
        // triples
        return HadoopRdfIORegistry.createTripleReader(lang);
    }

}
