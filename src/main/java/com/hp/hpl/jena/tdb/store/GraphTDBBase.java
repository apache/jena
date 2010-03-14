/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;


import static com.hp.hpl.jena.sparql.core.Quad.isQuadUnionGraph ;

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;

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
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.graph.BulkUpdateHandlerTDB ;
import com.hp.hpl.jena.tdb.graph.GraphSyncListener ;
import com.hp.hpl.jena.tdb.graph.QueryHandlerTDB ;
import com.hp.hpl.jena.tdb.graph.TransactionHandlerTDB ;
import com.hp.hpl.jena.tdb.graph.UpdateListener ;
import com.hp.hpl.jena.tdb.lib.NodeFmtLib ;
import com.hp.hpl.jena.tdb.migrate.GraphBase2 ;
import com.hp.hpl.jena.tdb.migrate.Reifier2 ;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NiceIterator ;

/** General operations for TDB graphs (free-standing graph, default graph and named graphs) */
public abstract class GraphTDBBase extends GraphBase2 implements GraphTDB
{
    private final QueryHandlerTDB queryHandler = new QueryHandlerTDB(this) ;
    private final TransactionHandler transactionHandler = new TransactionHandlerTDB(this) ;
    private final BulkUpdateHandler bulkUpdateHandler = new BulkUpdateHandlerTDB(this) ;
    protected final DatasetGraphTDB dataset ;
    protected final Node graphNode ;
    protected final int syncPoint ;
    private long epoch = 4 ;                // And reads are always even.

    public GraphTDBBase(DatasetGraphTDB dataset, Node graphName)
    { 
        super() ;
        this.dataset = dataset ; 
        this.graphNode = graphName ;
        syncPoint = dataset.getConfigValueAsInt(Names.pSyncTick, SystemTDB.SyncTick) ;
        if ( syncPoint > 0 )
            this.getEventManager().register(new GraphSyncListener(this, syncPoint)) ;
        this.getEventManager().register(new UpdateListener(this)) ;
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
    public void sync() { sync(true) ; }

    //@Override
    public abstract void sync(boolean force) ;
    
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
    
    // /*static/* - remove when MRSW checkign is stable. 
    protected /*static*/ ExtendedIterator<Triple> graphBaseFindWorker(TripleTable tripleTable, TripleMatch m)
    {
        // See also SolverLib.execute
        Iterator<Triple> iter = tripleTable.find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
        if ( iter == null )
            return com.hp.hpl.jena.util.iterator.NullIterator.instance() ;
        
        boolean b = iter.hasNext() ;
        
        return new MapperIteratorTriples(iter, this) ;
    }
    
    protected /*static*/ ExtendedIterator<Triple> graphBaseFindWorker(DatasetGraphTDB dataset, Node graphNode, TripleMatch m)
    {
        Node gn = graphNode ;
        // Explicitly named union graph. 
        if ( isQuadUnionGraph(gn) )
            gn = Node.ANY ;

        Iterator<Quad> iter = dataset.getQuadTable().find(gn, m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
        if ( iter == null )
            return com.hp.hpl.jena.util.iterator.NullIterator.instance() ;
        
        Iterator<Triple> iterTriples = new ProjectQuadsToTriples((gn == Node.ANY ? null : gn) , iter) ;
        
        if ( gn == Node.ANY )
            iterTriples = Iter.distinct(iterTriples) ;
        return new MapperIteratorTriples(iterTriples, this) ;
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
    public void finishRead()        {}

    //@Override
    public void startRead()         {}
    
    //@Override
    public final void startUpdate() { epoch ++ ; }
    //@Override
    public final void finishUpdate() { epoch ++ ; }

    @Override
    protected final int graphBaseSize()
    {
        Iterator<?> iter = countThis() ;
        return (int)Iter.count(iter) ;
    }
    
    // Convert from Iterator<Triple> to ExtendedIterator
    static class MapperIteratorTriples extends NiceIterator<Triple>
    {
        private final Iterator<Triple> iter ;
        private GraphTDBBase base ;
        private long epoch ;
        private boolean finished = false ;
        
        MapperIteratorTriples(Iterator<Triple> iter, GraphTDBBase base) 
        { 
            this.iter = iter ;
            this.base = base ;
            this.epoch = base.epoch ;
            finished = ! iter.hasNext() ;
        }
        
        private void checkCourrentModification()
        {
            if ( finished )
                return ;
            
            long now = base.epoch ;
            if ( now != epoch )
                throw new ConcurrentModificationException() ;
        }
        
        @Override public boolean hasNext()
        { 
            // Problem - the underlying iterator may have finished.
            // Solved by .hashNext in next() ;
            // This is good because many iterators do their the work in .hasNext();
            checkCourrentModification() ;
            boolean b = iter.hasNext() ;
            if ( ! b )
                finished = true ; 
            return b ;
        }
        
        @Override public Triple next()
        { 
            checkCourrentModification() ;
            try { 
                Triple t = iter.next();
                if ( ! iter.hasNext() )
                    // Iterator finished.  Note this.
                    finished = true ;
                return t ;
            }
            catch (NoSuchElementException ex) { finished = true ; throw ex ; }
        }
        
        @Override public void remove()     
        //{ throw new UnsupportedOperationException() ; }
        {
            checkCourrentModification() ; 
            iter.remove() ;
            epoch = base.epoch ;
        }
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