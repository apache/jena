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
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;

/**
 * A registry which is used by various classes to dynamically select record
 * readers and writers based on a provided {@link Lang}
 * <p>
 * Readers and writers are dynamically discovered using the Java
 * {@link ServiceLoader} mechanism. This will look for files under
 * {@code META-INF/services} named
 * {@code org.apache.jena.hadoop.rdf.io.registry.ReaderFactory} and
 * {@code org.apache.jena.hadoop.rdf.io.registry.WriterFactory}. This follows
 * the standard {@linkplain ServiceLoader} format of provided one class name per
 * line which implements the relevant interface.
 * </p>
 * 
 */
public class HadoopRdfIORegistry {

    private static Map<Lang, ReaderFactory> readerFactories = new HashMap<>();
    private static Map<Lang, WriterFactory> writerFactories = new HashMap<>();
    private static boolean init = false;

    static {
        init();
    }

    private static synchronized void init() {
        if (init)
            return;

        // Dynamically load and register reader factories
        ServiceLoader<ReaderFactory> readerFactoryLoader = ServiceLoader.load(ReaderFactory.class);
        Iterator<ReaderFactory> readerFactoryIterator = readerFactoryLoader.iterator();
        while (readerFactoryIterator.hasNext()) {
            ReaderFactory f = readerFactoryIterator.next();
            addReaderFactory(f);
        }

        // Dynamically load and register writer factories
        ServiceLoader<WriterFactory> writerFactoryLoader = ServiceLoader.load(WriterFactory.class);
        Iterator<WriterFactory> writerFactoryIterator = writerFactoryLoader.iterator();
        while (writerFactoryIterator.hasNext()) {
            WriterFactory f = writerFactoryIterator.next();
            addWriterFactory(f);
        }

        init = true;
    }

    /**
     * Resets the registry to the default configuration
     */
    public static synchronized void reset() {
        if (!init)
            return;

        init = false;
        init();
    }

    /**
     * Registers the reader factory for all the languages it declares itself as
     * supporting
     * 
     * @param f
     *            Reader factory
     */
    public static void addReaderFactory(ReaderFactory f) {
        if (f == null)
            throw new NullPointerException("Factory cannot be null");

        readerFactories.put(f.getPrimaryLanguage(), f);
        for (Lang altLang : f.getAlternativeLanguages()) {
            readerFactories.put(altLang, f);
        }
    }

    /**
     * Registers the writer factory for all the languages it declares itself as
     * supporting
     * 
     * @param f
     *            Writer factory
     */
    public static void addWriterFactory(WriterFactory f) {
        if (f == null)
            throw new NullPointerException("Factory cannot be null");

        writerFactories.put(f.getPrimaryLanguage(), f);
        for (Lang altLang : f.getAlternativeLanguages()) {
            writerFactories.put(altLang, f);
        }
    }

    /**
     * Gets whether there is a quad reader available for the given language
     * 
     * @param lang
     *            Language
     * @return True if available, false otherwise
     */
    public static boolean hasQuadReader(Lang lang) {
        if (lang == null)
            return false;

        ReaderFactory f = readerFactories.get(lang);
        if (f == null)
            return false;
        return f.canReadQuads();
    }

    /**
     * Gets whether there is a triple reader available for the given language
     * 
     * @param lang
     *            Language
     * @return True if available, false otherwise
     */
    public static boolean hasTriplesReader(Lang lang) {
        if (lang == null)
            return false;

        ReaderFactory f = readerFactories.get(lang);
        if (f == null)
            return false;
        return f.canReadTriples();
    }

    /**
     * Tries to create a quad reader for the given language
     * 
     * @param lang
     *            Language
     * @return Quad reader if one is available
     * @throws IOException
     *             Thrown if a quad reader is not available or the given
     *             language does not support quads
     */
    public static RecordReader<LongWritable, QuadWritable> createQuadReader(Lang lang) throws IOException {
        if (lang == null)
            throw new IOException("Cannot create a quad reader for an undefined language");

        ReaderFactory f = readerFactories.get(lang);
        if (f == null)
            throw new IOException("No factory registered for language " + lang.getName());
        if (!f.canReadQuads())
            throw new IOException(lang.getName() + " does not support reading quads");

        RecordReader<LongWritable, QuadWritable> reader = f.createQuadReader();
        if (reader == null)
            throw new IOException("Registered factory for " + lang.getName() + " produced a null triples reader");
        return reader;
    }

    /**
     * Tries to create a triple reader for the given language
     * 
     * @param lang
     *            Language
     * @return Triple reader if one is available
     * @throws IOException
     *             Thrown if a triple reader is not available or the given
     *             language does not support triple
     */
    public static RecordReader<LongWritable, TripleWritable> createTripleReader(Lang lang) throws IOException {
        if (lang == null)
            throw new IOException("Cannot create a triple reader for an undefined language");

        ReaderFactory f = readerFactories.get(lang);
        if (f == null)
            throw new IOException("No factory registered for language " + lang.getName());
        if (!f.canReadTriples())
            throw new IOException(lang.getName() + " does not support reading triples");

        RecordReader<LongWritable, TripleWritable> reader = f.createTripleReader();
        if (reader == null)
            throw new IOException("Registered factory for " + lang.getName() + " produced a null triples reader");
        return reader;
    }

    /**
     * Gets whether there is a quad writer available for the given language
     * 
     * @param lang
     *            Language
     * @return True if available, false otherwise
     */
    public static boolean hasQuadWriter(Lang lang) {
        if (lang == null)
            return false;

        WriterFactory f = writerFactories.get(lang);
        if (f == null)
            return false;
        return f.canWriteQuads();
    }

    /**
     * Gets whether there is a triple writer available for the given language
     * 
     * @param lang
     *            Language
     * @return True if available, false otherwise
     */
    public static boolean hasTriplesWriter(Lang lang) {
        if (lang == null)
            return false;

        WriterFactory f = writerFactories.get(lang);
        if (f == null)
            return false;
        return f.canWriteTriples();
    }

    /**
     * Tries to create a quad writer for the given language
     * 
     * @param lang
     *            Language
     * @param writer
     *            Writer
     * @param config
     *            Configuration
     * 
     * @return Quad writer if one is available
     * @throws IOException
     *             Thrown if a quad writer is not available or the given
     *             language does not support quads
     */
    public static <TKey> RecordWriter<TKey, QuadWritable> createQuadWriter(Lang lang, Writer writer,
            Configuration config) throws IOException {
        if (lang == null)
            throw new IOException("Cannot create a quad writer for an undefined language");

        WriterFactory f = writerFactories.get(lang);
        if (f == null)
            throw new IOException("No factory registered for language " + lang.getName());
        if (!f.canWriteQuads())
            throw new IOException(lang.getName() + " does not support writeing quads");

        RecordWriter<TKey, QuadWritable> rwriter = f.<TKey> createQuadWriter(writer, config);
        if (rwriter == null)
            throw new IOException("Registered factory for " + lang.getName() + " produced a null triples writer");
        return rwriter;
    }

    /**
     * Tries to create a triple writer for the given language
     * 
     * @param lang
     *            Language
     * @param writer
     *            Writer
     * @param config
     *            Configuration
     * @return Triple writer if one is available
     * @throws IOException
     *             Thrown if a triple writer is not available or the given
     *             language does not support triple
     */
    public static <TKey> RecordWriter<TKey, TripleWritable> createTripleWriter(Lang lang, Writer writer,
            Configuration config) throws IOException {
        if (lang == null)
            throw new IOException("Cannot create a triple writer for an undefined language");

        WriterFactory f = writerFactories.get(lang);
        if (f == null)
            throw new IOException("No factory registered for language " + lang.getName());
        if (!f.canWriteTriples())
            throw new IOException(lang.getName() + " does not support writing triples");

        RecordWriter<TKey, TripleWritable> rwriter = f.<TKey> createTripleWriter(writer, config);
        if (rwriter == null)
            throw new IOException("Registered factory for " + lang.getName() + " produced a null triples writer");
        return rwriter;
    }
}
