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

package org.apache.jena.hadoop.rdf.io.registry;

import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;

/**
 * Interface for reader factories
 * 
 */
public interface ReaderFactory {

    /**
     * Gets the primary language this factory produces readers for
     * 
     * @return Primary language
     */
    public abstract Lang getPrimaryLanguage();

    /**
     * Gets the alternative languages this factory can produce readers for
     * 
     * @return Alternative languages
     */
    public abstract Collection<Lang> getAlternativeLanguages();

    /**
     * Gets whether this factory can produce readers that are capable of reading
     * quads
     * 
     * @return True if quads can be read, false if not
     */
    public abstract boolean canReadQuads();

    /**
     * Gets whether this factory can produce readers that are capable of reading
     * triples
     * 
     * @return True if triples can be read, false if not
     */
    public abstract boolean canReadTriples();

    /**
     * Creates a quad reader
     * 
     * @return Quad reader
     * @throws IOException
     *             May be thrown if a quad reader cannot be created
     */
    public abstract RecordReader<LongWritable, QuadWritable> createQuadReader() throws IOException;

    /**
     * Creates a triples reader
     * 
     * @return Triples reader
     * @throws IOException
     *             May be thrown if a triple reader cannot be created
     */
    public abstract RecordReader<LongWritable, TripleWritable> createTripleReader() throws IOException;
}
