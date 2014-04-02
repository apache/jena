/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * An abstract record writer for whole file triple formats
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public abstract class AbstractWholeFileTripleWriter<TKey> extends AbstractWholeFileNodeTupleWriter<TKey, Triple, TripleWritable> {

    private Graph g = GraphFactory.createDefaultGraph();

    protected AbstractWholeFileTripleWriter(Writer writer) {
        super(writer);
    }

    @Override
    protected final void add(TripleWritable value) {
        this.g.add(value.get());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected final void writeOutput(Writer writer) {
        RDFDataMgr.write(writer, this.g, this.getRdfLanguage());
    }

    /**
     * Gets the RDF language to write the output in
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();

}
