/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import static com.hp.hpl.jena.sparql.core.Quad.isUnionGraph ;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.graph.BulkUpdateHandler ;
import com.hp.hpl.jena.graph.Capabilities ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Reifier ;
import com.hp.hpl.jena.graph.TransactionHandler ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.query.QueryHandler ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.sparql.graph.GraphBase2 ;
import com.hp.hpl.jena.sparql.graph.Reifier2 ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.graph.BulkUpdateHandlerTDB ;
import com.hp.hpl.jena.tdb.graph.QueryHandlerTDB ;
import com.hp.hpl.jena.tdb.graph.TransactionHandlerTDB ;
import com.hp.hpl.jena.tdb.lib.NodeFmtLib ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/** General operations for TDB graphs (free-standing graph, default graph and named graphs) */
public abstract class GraphTDBBase extends GraphBase2 implements GraphTDB
{
    private final QueryHandlerTDB queryHandler = new QueryHandlerTDB(this) ;
    private final TransactionHandler transactionHandler = new TransactionHandlerTDB(this) ;
    private final BulkUpdateHandler bulkUpdateHandler = new BulkUpdateHandlerTDB(this) ;
    protected final DatasetGraphTDB dataset ;
    protected final Node graphNode ;

    public GraphTDBBase(DatasetGraphTDB dataset, Node graphName)
    { 
        super() ;
        this.dataset = dataset ; 
        this.graphNode = graphName ;
    }
    
    /** Reorder processor - may be null, for "none" */
    //@Override
    public final ReorderTransformation getReorderTransform()    { return dataset.getTransform() ; }
    
    //@Override
    public final Location getLocation()                         { return dataset.getLocation() ; }
    
    //@Override
    public final Node getGraphNode()                            { return graphNode ; }
    
    //@Override
    public final DatasetGraphTDB getDataset()                   { return dataset ; }
    
    //@Override
    public Lock getLock()                                       { return dataset.getLock() ; }
    
    // Intercept performAdd/preformDelete and bracket in start/finish markers   
    
    @Override
    public final void performAdd(Triple triple)
    { 
        // Should we do try{}finally{}?
        // Here, no, if there is an exeception, the database is bad. 
        startUpdate() ;
        _performAdd(triple) ;
        finishUpdate() ;
    }

    @Override
    public final void performDelete(Triple triple)
    {
        startUpdate() ;
        _performDelete(triple) ;
        finishUpdate() ;
    }
    
    protected abstract boolean _performAdd( Triple triple ) ;
    
    protected abstract boolean _performDelete( Triple triple ) ;
    
    //@Override
    public abstract void sync() ;
    
    @Override
    // make submodels think about this.
    public abstract String toString() ;
    
    protected void duplicate(Triple t)
    {
        if ( TDB.getContext().isTrue(SystemTDB.symLogDuplicates) && getLog().isInfoEnabled() )
        {
            String $ = NodeFmtLib.displayStr(t, this.getPrefixMapping()) ;
            getLog().info("Duplicate: ("+$+")") ;
        }
    }
    
    protected static ExtendedIterator<Triple> graphBaseFindWorker(TripleTable tripleTable, TripleMatch m)
    {
        // See also SolverLib.execute
        Iterator<Triple> iter = tripleTable.find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
        if ( iter == null )
            return com.hp.hpl.jena.util.iterator.NullIterator.instance() ;

        // Look now!
        boolean b = iter.hasNext() ;
        return WrappedIterator.createNoRemove(iter) ;
    }
    
    protected static ExtendedIterator<Triple> graphBaseFindWorker(DatasetGraphTDB dataset, Node graphNode, TripleMatch m)
    {
        Node gn = graphNode ;
        // Explicitly named union graph. 
        if ( isUnionGraph(gn) )
            gn = Node.ANY ;

        Iterator<Quad> iter = dataset.getQuadTable().find(gn, m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
        if ( iter == null )
            return com.hp.hpl.jena.util.iterator.NullIterator.instance() ;
        
        Iterator<Triple> iterTriples = new ProjectQuadsToTriples((gn == Node.ANY ? null : gn) , iter) ;
        
        if ( gn == Node.ANY )
            iterTriples = Iter.distinct(iterTriples) ;
        return WrappedIterator.createNoRemove(iterTriples) ;
    }
    
    @Override
    protected Reifier constructReifier()
    {
        return new Reifier2(this) ;
    }
    
    protected abstract Logger getLog() ;
    
    /** Iterator over something that, when counted, is the graph size. */
    protected abstract Iterator<?> countThis() ;
    
    

    //@Override
    public void startRead()             { dataset.startRead() ; }
    //@Override
    public void finishRead()            { dataset.finishRead() ; }

    //@Override
    public final void startUpdate()     { dataset.startUpdate() ; }
    //@Override
    public final void finishUpdate()    { dataset.finishUpdate() ; }

    @Override
    protected final int graphBaseSize()
    {
        Iterator<?> iter = countThis() ;
        return (int)Iter.count(iter) ;
    }
    
    // Convert from Iterator<Quad> to Iterator<Triple>
    static class ProjectQuadsToTriples implements Iterator<Triple>
    {
        private final Iterator<Quad> iter ;
        private final Node graphNode ;
        /** Project quads to triples - check the graphNode is as expected if not null */
        ProjectQuadsToTriples(Node graphNode, Iterator<Quad> iter) { this.graphNode = graphNode ; this.iter = iter ; }
        //@Override
        public boolean hasNext() { return iter.hasNext() ; }
        
        //@Override
        public Triple next()
        { 
            Quad q = iter.next();
            if ( graphNode != null && ! q.getGraph().equals(graphNode))
                throw new InternalError("ProjectQuadsToTriples: Quads from unexpected graph") ;
            return q.asTriple() ;
        }
        //@Override
        public void remove() { iter.remove(); }
    }
    
    @Override
    public BulkUpdateHandler getBulkUpdateHandler() {return bulkUpdateHandler ; }

    @Override
    public Capabilities getCapabilities()
    {
        if ( capabilities == null )
            capabilities = new Capabilities(){
                public boolean sizeAccurate() { return true; }
                public boolean addAllowed() { return true ; }
                public boolean addAllowed( boolean every ) { return true; } 
                public boolean deleteAllowed() { return true ; }
                public boolean deleteAllowed( boolean every ) { return true; } 
                public boolean canBeEmpty() { return true; }
                public boolean iteratorRemoveAllowed() { return false; } /* ** */
                public boolean findContractSafe() { return true; }
                public boolean handlesLiteralTyping() { return false; } /* ** */
            } ; 
        
        return super.getCapabilities() ;
    }
    
    @Override
    public QueryHandler queryHandler()
    { return queryHandler ; }
    
    @Override
    public TransactionHandler getTransactionHandler()
    { return transactionHandler ; }
    
//    protected GraphStatisticsHandler createStatisticsHandler()
//    { return null; }
 
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */