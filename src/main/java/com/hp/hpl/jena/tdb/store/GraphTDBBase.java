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
    @Override
    public final ReorderTransformation getReorderTransform()    { return dataset.getTransform() ; }
    
    @Override
    public final Location getLocation()                         { return dataset.getLocation() ; }
    
    @Override
    public final Node getGraphNode()                            { return graphNode ; }
    
    @Override
    public final DatasetGraphTDB getDataset()                   { return dataset ; }
    
    @Override
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
    
    @Override
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

    @Override
    public void startRead()             { dataset.startRead() ; }
    @Override
    public void finishRead()            { dataset.finishRead() ; }

    @Override
    public final void startUpdate()     { dataset.startUpdate() ; }
    @Override
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
        @Override
        public boolean hasNext() { return iter.hasNext() ; }
        
        @Override
        public Triple next()
        { 
            Quad q = iter.next();
            if ( graphNode != null && ! q.getGraph().equals(graphNode))
                throw new InternalError("ProjectQuadsToTriples: Quads from unexpected graph") ;
            return q.asTriple() ;
        }
        @Override
        public void remove() { iter.remove(); }
    }
    
    @Override
    public BulkUpdateHandler getBulkUpdateHandler() {return bulkUpdateHandler ; }

    @Override
    public Capabilities getCapabilities()
    {
        if ( capabilities == null )
            capabilities = new Capabilities(){
                @Override
                public boolean sizeAccurate() { return true; }
                @Override
                public boolean addAllowed() { return true ; }
                @Override
                public boolean addAllowed( boolean every ) { return true; } 
                @Override
                public boolean deleteAllowed() { return true ; }
                @Override
                public boolean deleteAllowed( boolean every ) { return true; } 
                @Override
                public boolean canBeEmpty() { return true; }
                @Override
                public boolean iteratorRemoveAllowed() { return false; } /* ** */
                @Override
                public boolean findContractSafe() { return true; }
                @Override
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
