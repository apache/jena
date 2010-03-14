package com.hp.hpl.jena.tdb.lib;

import org.openjena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.riot.lang.SinkToGraphTriples ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDB ;

/** @see SinkToGraphTriples */ 
public class SinkQuadsToDataset implements Sink<Quad>
{
    /* See also SinkToGraphTriples */ 
    private final DatasetGraph dataset ;
    private Node graphNode = null ;
    private Graph graph = null ;

    public SinkQuadsToDataset(DatasetGraph dataset)
    {
        this.dataset = dataset ;
    }
    
    public void send(Quad quad)
    {
        if ( graph == null || ! Utils.equal(quad.getGraph(), graphNode) )
        {
            // graph == null ==> Uninitialized
            // not equals ==> different graph to last time.
            graphNode = quad.getGraph() ;
            if ( quad.isTriple() )
                graph = dataset.getDefaultGraph() ;
            else
                graph = dataset.getGraph(graphNode) ;
        }
        graph.add(quad.asTriple()) ;
    }

    public void flush()
    {
        TDB.sync(dataset) ;
    }

    public void close()
    {}
}