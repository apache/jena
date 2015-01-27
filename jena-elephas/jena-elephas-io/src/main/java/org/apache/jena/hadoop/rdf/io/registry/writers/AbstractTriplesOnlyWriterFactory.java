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
package org.apache.jena.hadoop.rdf.io.registry.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.hadoop.rdf.io.registry.WriterFactory;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;

/**
 * Abstract writer factory for languages that only support triples
 */
public abstract class AbstractTriplesOnlyWriterFactory implements WriterFactory {

    private Lang lang;
    private Collection<Lang> alternateLangs = Collections.unmodifiableList(Collections.<Lang> emptyList());

    public AbstractTriplesOnlyWriterFactory(Lang lang) {
        this(lang, (Collection<Lang>) null);
    }

    public AbstractTriplesOnlyWriterFactory(Lang lang, Lang... altLangs) {
        this(lang, Arrays.asList(altLangs));
    }

    public AbstractTriplesOnlyWriterFactory(Lang lang, Collection<Lang> altLangs) {
        this.lang = lang;
        if (altLangs != null)
            this.alternateLangs = Collections.unmodifiableCollection(altLangs);
    }

    @Override
    public final Lang getPrimaryLanguage() {
        return this.lang;
    }

    @Override
    public final Collection<Lang> getAlternativeLanguages() {
        return this.alternateLangs;
    }

    @Override
    public final boolean canWriteQuads() {
        return false;
    }

    @Override
    public final boolean canWriteTriples() {
        return true;
    }

    @Override
    public final <TKey> RecordWriter<TKey, QuadWritable> createQuadWriter(Writer writer, Configuration config)
            throws IOException {
        throw new IOException(this.lang.getName() + " does not support writing quads");
    }

    @Override
    public abstract <TKey> RecordWriter<TKey, TripleWritable> createTripleWriter(Writer writer, Configuration config)
            throws IOException;

}
