/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.Writer;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFWriterRegistry;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Abstract batched record writer for quad formats
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public abstract class AbstractBatchedQuadWriter<TKey> extends AbstractBatchedNodeTupleWriter<TKey, Quad, QuadWritable> {

    private DatasetGraph g = DatasetGraphFactory.createMem();

    protected AbstractBatchedQuadWriter(Writer writer, long batchSize) {
        super(writer, batchSize);
    }

    @Override
    protected final long add(QuadWritable value) {
        g.add(value.get());
        return g.size();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected final long writeOutput(Writer writer) {
        if (this.g.size() == 0)
            return 0;
        RDFDataMgr.write(writer, this.g, RDFWriterRegistry.defaultSerialization(this.getRdfLanguage()));

        // Clear the dataset graph
        @SuppressWarnings("unchecked")
        List<Node> graphNames = IteratorUtils.toList(this.g.listGraphNodes());
        for (Node graphName : graphNames) {
            this.g.removeGraph(graphName);
        }
        this.g.getDefaultGraph().clear();

        return this.g.size();
    }

    /**
     * Gets the RDF language used for output
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();
}
