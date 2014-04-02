/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.compressed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFWriterRegistry;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.io.HadoopIOConstants;
import com.yarcdata.urika.hadoop.rdf.io.input.AbstractNodeTupleInputFormatTests;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Abstract tests for compressed whole file quad formats
 * 
 * @author rvesse
 */
public abstract class AbstractCompressedWholeFileQuadInputFormatTests extends
        AbstractNodeTupleInputFormatTests<Quad, QuadWritable> {

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
    private void writeTuples(Dataset ds, Writer writer) {
        RDFDataMgr.write(writer, ds, RDFWriterRegistry.defaultSerialization(this.getRdfLanguage()));
    }

    /**
     * Gets the RDF language to write out generated tuples in
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();

    private void writeGoodTuples(Writer writer, int num) throws IOException {
        Dataset ds = DatasetFactory.createMem();
        Model m = ModelFactory.createDefaultModel();
        Resource currSubj = m.createResource("http://example.org/subjects/0");
        Property predicate = m.createProperty("http://example.org/predicate");
        for (int i = 0; i < num; i++) {
            if (i % 100 == 0) {
                ds.addNamedModel("http://example.org/graphs/" + (i / 100), m);
                m = ModelFactory.createDefaultModel();
            }
            if (i % 10 == 0) {
                currSubj = m.createResource("http://example.org/subjects/" + (i / 10));
            }
            m.add(currSubj, predicate, m.createTypedLiteral(i));
        }
        if (!m.isEmpty()) {
            ds.addNamedModel("http://example.org/graphs/extra", m);
        }
        this.writeTuples(ds, writer);
    }

    @Override
    protected final void generateTuples(Writer writer, int num) throws IOException {
        this.writeGoodTuples(writer, num);
        writer.close();
    }

    @Override
    protected final void generateMixedTuples(Writer writer, int num) throws IOException {
        // Write good data
        this.writeGoodTuples(writer, num / 2);

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
