/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFWriterRegistry;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * An abstract record writer for whole file triple formats
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public abstract class AbstractWholeFileQuadWriter<TKey> extends AbstractWholeFileNodeTupleWriter<TKey, Quad, QuadWritable> {

    private DatasetGraph g = DatasetGraphFactory.createMem();

    protected AbstractWholeFileQuadWriter(Writer writer) {
        super(writer);
    }

    @Override
    protected final void add(QuadWritable value) {
        this.g.add(value.get());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void writeOutput(Writer writer) {
        RDFDataMgr.write(writer, this.g, RDFWriterRegistry.defaultSerialization(this.getRdfLanguage()));
    }

    /**
     * Gets the RDF language to write the output in
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();

}
