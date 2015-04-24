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

package org.apache.jena.sparql.graph;

import java.util.Iterator ;

import org.apache.jena.atlas.data.DataBag ;
import org.apache.jena.atlas.data.ThresholdPolicy ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Capabilities ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.TripleStore ;
import org.apache.jena.mem.GraphMemBase ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;

/**
 * A Graph based on top of a DataBag.  This means it has some limitations:
 * <ol>
 * <li>Cannot add any triples after you call find() unless you first call clear() or getBulkUpdateHandler().removeAll().</li>
 * <li>Cannot remove any triples except by calling clear() or getBulkUpdateHandler().removeAll().</li>
 * <li>There is no indexing, so find() will always scan all triples.</li>
 * <li>The size() method is not guaranteed to be accurate, treat it as an estimate.</li>
 * <li>You must call close() in order to release any resources (such as spill files).</li>
 * </ol>
 */
public abstract class GraphDataBag extends GraphMemBase
{
    private final ThresholdPolicy<Triple> thresholdPolicy ;
    private DataBag<Triple> db ;
    
    public GraphDataBag(ThresholdPolicy<Triple> thresholdPolicy)
    {
        super() ;
        
        this.thresholdPolicy = thresholdPolicy;
        
        capabilities = new Capabilities()
        {
            @Override
            public boolean sizeAccurate() { return false; }
            @Override
            public boolean addAllowed() { return addAllowed( false ); }
            @Override
            public boolean addAllowed( boolean every ) { return true; } 
            @Override
            public boolean deleteAllowed() { return deleteAllowed( false ); }
            @Override
            public boolean deleteAllowed( boolean every ) { return false; } 
            @Override
            public boolean canBeEmpty() { return true; }
            @Override
            public boolean iteratorRemoveAllowed() { return false; }
            @Override
            public boolean findContractSafe() { return true; }
            @Override
            public boolean handlesLiteralTyping() { return true; }
        };
        
        this.db = createDataBag();
    }
    
    protected abstract DataBag<Triple> createDataBag();
    
    protected ThresholdPolicy<Triple> getThresholdPolicy()
    {
        return thresholdPolicy;
    }

    @Override
    protected TripleStore createTripleStore()
    {
        // No TripleStore for us
        return null ;
    }
    
    @Override
    public void performAdd(Triple t)
    {
        db.add(t) ;
    }

    @Override
    public int graphBaseSize()
    {
        return (int) db.size() ;
    }

    @Override
    protected void destroy()
    {
        db.close() ;
    }

    @Override
    public void clear()
    {
        db.close() ;
        db = createDataBag() ;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m)
    {
        Iterator<Triple> iter = Iter.filter(db.iterator(), m::matches) ;
        return WrappedIterator.create(iter) ;
    }
}

