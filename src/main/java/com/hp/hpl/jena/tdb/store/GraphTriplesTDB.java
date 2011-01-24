/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Iterator;

import org.openjena.atlas.lib.Tuple ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/** A graph implementation that uses a triple table - free-standing graph or default graph of dataset */
public class GraphTriplesTDB extends GraphTDBBase
{
    private static Logger log = LoggerFactory.getLogger(GraphTriplesTDB.class) ;
    
    private final TripleTable tripleTable ;
    private final DatasetPrefixStorage prefixes ;
    
    public GraphTriplesTDB(DatasetGraphTDB dataset,
                           TripleTable tripleTable,
                           DatasetPrefixStorage prefixes)
    {
        super(dataset, null) ;
        this.tripleTable = tripleTable ;
        this.prefixes = prefixes ;
    }
    
    @Override
    protected boolean _performAdd( Triple t ) 
    { 
        boolean changed = tripleTable.add(t) ;
        if ( ! changed )
            duplicate(t) ;
        return changed ;
    }

    @Override
    protected boolean _performDelete( Triple t ) 
    { 
        boolean changed = tripleTable.delete(t) ;
        return changed ;
    }
    
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
    {
        return graphBaseFindWorker(tripleTable, m) ;
    }

//    @Override
//    public boolean isEmpty()        { return tripleTable.isEmpty() ; }
    
    @Override
    protected final Logger getLog() { return log ; }

    //@Override
    public Tuple<Node> asTuple(Triple triple)
    {
        return Tuple.create(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }
    
    @Override
    protected Iterator<Tuple<NodeId>> countThis()
    {
        return tripleTable.getNodeTupleTable().findAll() ;
    }

    //@Override
    public NodeTupleTable getNodeTupleTable()           { return tripleTable.getNodeTupleTable()   ; }
   
    @Override
    protected PrefixMapping createPrefixMapping()
    {
        return prefixes.getPrefixMapping() ;
    }

    @Override
    final public void close()
    {
        if ( dataset != null )
        {
            // Part of a dataset which may be cached and so "close" is meaningless.
            // At least sync it to flush data to disk.
            sync(true) ;
        }
        else            
        {
            // Free standing graph.  Clear up.
            prefixes.close();
            tripleTable.close();
            super.close() ;
        }
    }
    
    @Override
    public void sync(boolean force)
    {
        if ( dataset != null )
            dataset.sync(force) ;
        else
        {
            prefixes.sync(force) ;
            tripleTable.sync(force);
        }
    }
    
    @Override
    public String toString() { return Utils.className(this) ; }
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