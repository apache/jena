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

import java.io.IOException;
import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.hadoop.rdf.io.registry.HadoopRdfIORegistry;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.Quad ;

/**
 * An output format for RDF quads that dynamically selects the appropriate quad
 * writer to use based on the file extension of the output file.
 * <p>
 * For example this is useful when the output format may be controlled by a user
 * supplied filename i.e. the desired RDF output format is not precisely known
 * in advance
 * </p>
 * 
 * @param <TKey>
 *            Key type
 */
public abstract class QuadsOutputFormat<TKey> extends AbstractNodeTupleOutputFormat<TKey, Quad, QuadWritable> {

    @Override
    protected RecordWriter<TKey, QuadWritable> getRecordWriter(Writer writer, Configuration config, Path outputPath)
            throws IOException {
        Lang lang = RDFLanguages.filenameToLang(outputPath.getName());
        if (lang == null)
            throw new IOException("There is no registered RDF language for the output file " + outputPath.toString());

        if (!RDFLanguages.isQuads(lang))
            throw new IOException(
                    lang.getName()
                            + " is not a RDF quads format, perhaps you wanted TriplesOutputFormat or TriplesOrQuadsOutputFormat instead?");

        // This will throw an appropriate error if the language does not support
        // writing quads
        return HadoopRdfIORegistry.<TKey> createQuadWriter(lang, writer, config);
    }

}
