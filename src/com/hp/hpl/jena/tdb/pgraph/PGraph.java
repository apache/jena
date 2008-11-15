/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexRecord;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.query.QueryHandler;

import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.graph.GraphSyncListener;
import com.hp.hpl.jena.tdb.graph.GraphTDBQueryHandler;
import com.hp.hpl.jena.tdb.graph.UpdateListener;
import com.hp.hpl.jena.tdb.lib.Sync;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.solver.reorder.Reorderable;
import com.hp.hpl.jena.tdb.store.NodeTable;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Machinary to implement the Jena interfaces.
 *  This wraps a "nodes and triples" style graph (in TripleTable)
 *  with the code and data needed for ARQ and Jena. 
 */

public class PGraph extends GraphBase implements Sync, Reorderable
{
    private static Logger log = LoggerFactory.getLogger(PGraph.class) ;
    
    // ---- Record factories
    final static RecordFactory indexRecordFactory = new RecordFactory(LenIndexRecord, 0) ; 
    final static RecordFactory nodeRecordFactory = new RecordFactory(LenNodeHash, SizeOfNodeId) ;
    
    private final GraphTDBQueryHandler queryHandler = new GraphTDBQueryHandler(this) ;
    //private final TransactionHandler transactionHandler = null ; //new GraphTDBTransactionHandler(this) ;
    private ReorderTransformation reorderTransform = null ;

    private TripleTable1 tripleTable ;

    protected PGraph() {}
    
    public PGraph(TripleIndex spo, TripleIndex pos, TripleIndex osp, 
                    NodeTable nodeTable, ReorderTransformation reorderTransform, Location location)
    {
        if ( spo == null )
            throw new TDBException("SPO index is required") ;

        this.tripleTable = new TripleTable1(spo, pos, osp, nodeTable, location) ; 
        this.reorderTransform = reorderTransform ;
        
        int syncPoint = SystemTDB.SyncTick ;
        if ( syncPoint > 0 )
            this.getEventManager().register(new GraphSyncListener(this, syncPoint)) ;
        this.getEventManager().register(new UpdateListener(this)) ;
    }
    
    @Override
    public QueryHandler queryHandler()
    { return queryHandler ; }
    
//    @Override
//    public TransactionHandler getTransactionHandler()
//    { return transactionHandler ; }
    
    @Override
    public void performAdd( Triple t ) 
    { 
        boolean changed = tripleTable.add(t) ;
        if ( ! changed )
            duplicate(t) ;
    }

    private void duplicate(Triple t)
    {
        if ( TDB.getContext().isTrue(SystemTDB.symLogDuplicates) && log.isInfoEnabled() )
        {
            String $ = FmtUtils.stringForTriple(t, this.getPrefixMapping()) ;
            log.info("Duplicate: ("+$+")") ;
        }
    }

    @Override
    public void performDelete( Triple t ) 
    { 
        boolean changed = tripleTable.delete(t) ;
    }
    
//    // Make sure that the stage generator isn't installed.
//    @Override
//    public QueryHandler queryHandler()
//    { return new GraphQueryHandlerTDB(this) ; }
    
    @Override
    protected ExtendedIterator graphBaseFind(TripleMatch m)
    {
        Iterator<Triple> iter = tripleTable.find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
        if ( iter == null )
            return new com.hp.hpl.jena.util.iterator.NullIterator() ;
        return new MapperIterator(iter) ;
    }

    static class MapperIterator extends NiceIterator
    {
        Iterator<Triple> iter ;
        MapperIterator(Iterator<Triple> iter)
        {
            this.iter = iter ;
        }
        
        @Override
        public boolean hasNext() { return iter.hasNext() ; } 
        
        @Override
        public Triple next() { return iter.next(); }
    }
    
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
                public boolean findContractSafe() { return false; }
                public boolean handlesLiteralTyping() { return false; } /* ** */
            } ; 
        
        return super.getCapabilities() ;
    }
    
    @Override
    final public void close()
    {
        tripleTable.close();
        super.close() ;
    }
    
    @Override
    public void sync(boolean force)
    {
        tripleTable.sync(force);
    }

    /** Reorder processor - may be null, for "none" */ 
    public ReorderTransformation getReorderTransform()      { return reorderTransform ; }

    public TripleTable1 getTripleTable()                     { return tripleTable ; }
    public Location getLocation()                           { return tripleTable.getLocation() ; }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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