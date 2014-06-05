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

package org.apache.jena.hadoop.rdf.io.input.compressed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.jena.hadoop.rdf.io.HadoopIOConstants;
import org.apache.jena.hadoop.rdf.io.input.AbstractNodeTupleInputFormatTests;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Abstract tests for compressed whole file triple formats
 * 
 * 
 */
public abstract class AbstractCompressedWholeFileTripleInputFormatTests extends
        AbstractNodeTupleInputFormatTests<Triple, TripleWritable> {

    @Override
    protected Configuration prepareConfiguration() {
        Configuration config = super.prepareConfiguration();
        config.set(HadoopIOConstants.IO_COMPRESSION_CODECS, this.getCompressionCodec().getClass().getCanonicalName());
        return config;
    }

    @Override
    protected Writer getWriter(File f) throws IOException {
        CompressionCodec codec = this.getCompressionCodec();
        if (codec instanceof Configurable) {
            ((Configurable) codec).setConf(this.prepareConfiguration());
        }
        FileOutputStream fileOutput = new FileOutputStream(f, false);
        OutputStream output = codec.createOutputStream(fileOutput);
        return new OutputStreamWriter(output);
    }

    /**
     * Gets the compression codec to use
     * 
     * @return Compression codec
     */
    protected abstract CompressionCodec getCompressionCodec();

    /**
     * Indicates whether inputs can be split, defaults to false for compressed
     * input tests
     */
    @Override
    protected boolean canSplitInputs() {
        return false;
    }

    @SuppressWarnings("deprecation")
    private void writeTuples(Model m, Writer writer) {
        RDFDataMgr.write(writer, m, this.getRdfLanguage());
    }

    /**
     * Gets the RDF language to write out generated tuples in
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();

    @Override
    protected final void generateTuples(Writer writer, int num) throws IOException {
        Model m = ModelFactory.createDefaultModel();
        Resource currSubj = m.createResource("http://example.org/subjects/0");
        Property predicate = m.createProperty("http://example.org/predicate");
        for (int i = 0; i < num; i++) {
            if (i % 10 == 0) {
                currSubj = m.createResource("http://example.org/subjects/" + (i / 10));
            }
            m.add(currSubj, predicate, m.createTypedLiteral(i));
        }
        this.writeTuples(m, writer);
        writer.close();
    }

    @Override
    protected final void generateMixedTuples(Writer writer, int num) throws IOException {
        // Write good data
        Model m = ModelFactory.createDefaultModel();
        Resource currSubj = m.createResource("http://example.org/subjects/0");
        Property predicate = m.createProperty("http://example.org/predicate");
        for (int i = 0; i < num / 2; i++) {
            if (i % 10 == 0) {
                currSubj = m.createResource("http://example.org/subjects/" + (i / 10));
            }
            m.add(currSubj, predicate, m.createTypedLiteral(i));
        }
        this.writeTuples(m, writer);

        // Write junk data
        for (int i = 0; i < num / 2; i++) {
            writer.write("junk data\n");
        }

        writer.flush();
        writer.close();
    }

    @Override
    protected final void generateBadTuples(Writer writer, int num) throws IOException {
        for (int i = 0; i < num; i++) {
            writer.write("junk data\n");
        }
        writer.flush();
        writer.close();
    }
}
