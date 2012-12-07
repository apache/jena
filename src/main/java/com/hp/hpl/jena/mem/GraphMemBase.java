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

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.BulkUpdateHandler ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.impl.GraphBase ;
import com.hp.hpl.jena.graph.impl.TripleStore ;

/**
     GraphMemBase - a common base class for GraphMem and SmallGraphMem.
     Any GraphMemBase maintains a reference count, set to one when it is created,
     and incremented by the method <code>openAgain()</code>. When the graph 
     is closed, the count is decrememented, and when it reaches 0, the tables are 
     trashed and GraphBase.close() called. Thus in normal use one close is enough,
     but GraphMakers using GraphMems can arrange to re-use the same named 
     graph.
    
*/
public abstract class GraphMemBase extends GraphBase
    {
    /**
         The number-of-times-opened count.
    */
    protected int count;
    
    /**
        This Graph's TripleStore. Visible for <i>read-only</i> purposes only.
    */
    public final TripleStore store;
    
    /**
         initialise a GraphMemBase with its count set to 1.
    */
    public GraphMemBase( )
    {
        store = createTripleStore();
        count = 1; 
    }
    
    protected abstract TripleStore createTripleStore();
    
    /**
         Note a re-opening of this graph by incrementing the count. Answer
         this Graph.
    */
    public GraphMemBase openAgain()
        { 
        count += 1; 
        return this;
        }

    /**
         Sub-classes over-ride this method to release any resources they no
         longer need once fully closed.
    */
    protected abstract void destroy();

    /**
         Close this graph; if it is now fully closed, destroy its resources and run
         the GraphBase close.
    */
    @Override
    public void close()
        {
        if (--count == 0)
            {
            destroy();
            super.close();
            }
        }
    
    @Override
    @Deprecated
    public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bulkHandler == null) bulkHandler = new GraphMemBulkUpdateHandler( this );
        return bulkHandler;
        }

    /**
        Answer true iff this triple can be compared for sameValueAs by .equals(),
        ie, it is a concrete triple with a non-literal object.
    */
    protected final boolean isSafeForEquality( Triple t )
        { return t.isConcrete() && !t.getObject().isLiteral(); }
    }
