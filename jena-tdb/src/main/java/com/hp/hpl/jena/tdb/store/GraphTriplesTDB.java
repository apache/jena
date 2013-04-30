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

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Tuple ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;

/** A graph implementation that uses a triple table - concrete default graph of dataset */
public class GraphTriplesTDB extends GraphTDBBase
{
    // Collapse this into GraphTDBBase and have one class, no interface.
    // GraphNamedTDB should work.
    
    private static Logger log = LoggerFactory.getLogger(GraphTriplesTDB.class) ;
    
    public GraphTriplesTDB(DatasetGraphTDB dataset)
    {
        super(dataset, null) ;
    }

    @Override
    public boolean isEmpty()        { return dataset.getTripleTable().isEmpty() ; }
    
    @Override
    protected final Logger getLog() { return log ; }

    @Override
    protected Iterator<Tuple<NodeId>> countThis()
    {
        return getNodeTupleTable().findAll() ;
    }

    @Override
    public NodeTupleTable getNodeTupleTable()           { return dataset.getTripleTable().getNodeTupleTable()   ; }
   
    @Override
    protected PrefixMapping createPrefixMapping()
    {
        return dataset.getPrefixes().getPrefixMapping() ;
    }

    @Override
    public String toString() { return Utils.className(this) ; }
}
