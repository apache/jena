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

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    <p>
      A ReificationWrapperGraph wraps an existing graph and provides a
      reification API on that graph that stores the reification triples as 
      triples in that graph. Unlike Simple(ha!)Reifier, it does not maintain
      independent state about the reification fragments.
    </p>
    
     <p>
       One use (the provoking use) is to wrap TDB and SDB graphs to give
       a reification API that respects persistent data.
     </p>
     
 	@author kers
*/
public final class ReificationWrapperGraph extends WrappedGraph
    {
    protected final ReificationStyle style;
    
    /**
        Initialise this wrapper with the base graph and the preferred
        reification style. <i>Only <b>Standard</b> is officially supported</i>.
        
     	@param base
     	@param style
     */
    public ReificationWrapperGraph( Graph base, ReificationStyle style )
        {
        super( base );
        this.style = style;  
        this.reifier = new ReificationWrapper( this, style );
        }
    
    /**
        Answer the wrapped graph.
    */
    public Graph getBase()
        { return base; }
    
    @Override public ExtendedIterator<Triple> find( TripleMatch tm )
        { return find( tm.asTriple() ); }
    
    @Override public ExtendedIterator<Triple> find( Node s, Node p, Node o )
        { return find( Triple.create( s, p, o ) ); }
    
    private ExtendedIterator<Triple> find( Triple t )
        { 
        ExtendedIterator<Triple> found = base.find( t );
        ExtendedIterator<Triple> result = reifier.getStyle().conceals() ? found.filterDrop( ReificationWrapper.isReificationTriple ) : found;
        return SimpleEventManager.notifyingRemove( this, result );
        }
    
    /**
        Answer the number of unconcealed triples in this graph, which is the
        number of triples in the base graph minus the number of concealed
        triples (and concealed triples are not supported).
        
     	@see com.hp.hpl.jena.graph.impl.WrappedGraph#size()
    */
    @Override public int size()  
        { 
        ReificationWrapper br = (ReificationWrapper) reifier;
        return base.size() - br.countConcealed();
        }
    
    /**
        An identifying string. For diagnostic messages only.
    */
    @Override public String toString()
        { return "<rwg " + index + ">"; }
    
    private static int count = 0;
    private int index = count++;
    }
