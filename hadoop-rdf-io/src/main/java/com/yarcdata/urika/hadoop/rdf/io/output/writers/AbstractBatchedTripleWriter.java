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
 * Abstract batched record writer for triple formats
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public abstract class AbstractBatchedTripleWriter<TKey> extends AbstractBatchedNodeTupleWriter<TKey, Triple, TripleWritable> {

    private Graph g = GraphFactory.createDefaultGraph();

    protected AbstractBatchedTripleWriter(Writer writer, long batchSize) {
        super(writer, batchSize);
    }

    @Override
    protected final long add(TripleWritable value) {
        g.add(value.get());
        return g.size();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected final long writeOutput(Writer writer) {
        if (this.g.size() == 0)
            return 0;
        RDFDataMgr.write(writer, this.g, this.getRdfLanguage());
        this.g.clear();
        return this.g.size();
    }

    /**
     * Gets the RDF language used for output
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();
}
