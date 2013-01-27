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

/*
    INSERT HP DISCLAIMER HERE
    
    Dynamic intersection, May 2002, hedgehog
*/

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;


/**
    an implementation of a dynamic intersection of two models.
*/

@Deprecated
public class Intersection extends Dyadic implements Graph
	{
	public Intersection( Graph L, Graph R )
	    {
	    super( L, R );
	    }
	    
	@Override public void performAdd( Triple t )
	    {
	    L.add( t );
	    R.add( t );
	    }

	@Override public void performDelete( Triple t )
		{
		if (this.contains( t )) L.delete( t );
		}
		
	@Override protected ExtendedIterator<Triple> _graphBaseFind( TripleMatch s )
		{
        return L.find( s ) .filterKeep(  ifIn( R ) );
		}
	}
