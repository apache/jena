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

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.SimpleEventManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    Base class for the two-operand composition operations; has two graphs L and R
*/

public abstract class Dyadic extends CompositionBase
	{
	protected Graph L;
	protected Graph R;
	
    /**
        When the graph is constructed, copy the prefix mappings of both components
        into this prefix mapping. The prefix mapping doesn't change afterwards with the
        components, which might be regarded as a bug.
    */
	public Dyadic( Graph L, Graph R )
		{
		this.L = L;
		this.R = R;
        getPrefixMapping()
            .setNsPrefixes( L.getPrefixMapping() )
            .setNsPrefixes( R.getPrefixMapping() )
            ;
		}

	
	/**
	 * override graphBaseFind to return an iterator that will report when
	 * a deletion occurs.
	 */
	@Override
    protected final ExtendedIterator<Triple> graphBaseFind( TripleMatch m )
    {
		return SimpleEventManager.notifyingRemove( this, this._graphBaseFind( m ) );
    }
	
	/**
	 * The method that the overridden graphBaseFind( TripleMatch m ) calls to actually
	 * do the work of finding.
	 */
	protected abstract ExtendedIterator<Triple> _graphBaseFind( TripleMatch m );
	
    @Override
    public void close()
    	{
    	L.close();
    	R.close();
    	this.closed = true;
        }
        
    /**
        Generic dependsOn, true iff it depends on either of the subgraphs.
    */
    @Override
    public boolean dependsOn( Graph other )
        { return other == this || L.dependsOn( other ) || R.dependsOn( other ); }
 				
    public Union union( Graph X )
        { return new Union( this, X ); }

    /**
         Answer the left (first) operand of this Dyadic.
    */
    public Object getL()
        { return L; }

    /**
         Answer the right (second) operand of this Dyadic.
    */
    public Object getR()
        { return R; }
        
    }
