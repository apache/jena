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

    @Override
    public Tuple<Node> asTuple(Triple triple)
    {
        return Tuple.create(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }
    
    @Override
    protected Iterator<Tuple<NodeId>> countThis()
    {
        return tripleTable.getNodeTupleTable().findAll() ;
    }

    @Override
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
            sync() ;
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
    public void sync()
    {
        if ( dataset != null )
            dataset.sync() ;
        else
        {
            prefixes.sync() ;
            tripleTable.sync();
        }
    }
    
    @Override
    public String toString() { return Utils.className(this) ; }
}
