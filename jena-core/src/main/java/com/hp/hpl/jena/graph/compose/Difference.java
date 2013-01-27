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
import com.hp.hpl.jena.util.iterator.*;

/**
    Class representing the dynamic set difference L - R of two graphs. This is updatable;
    the updates are written through to one or other of the base graphs.
*/
@Deprecated
public class Difference extends Dyadic implements Graph 
	{
    /**
        Initialise a graph representing the difference L - R.
    */
	public Difference( Graph L, Graph R )
		{ super( L, R ); }
		
    /**
        Add a triple to the difference: add it to the left operand, and remove it from the 
        right operand.
    */
	@Override public void performAdd( Triple t )
		{
		L.add( t );
		R.delete( t );
		}

    /**
        Remove a triple from the difference: remove it from the left operand. [It could
        be added to the right operand instead, but somehow that feels less satisfactory.]
    */
	@Override public void performDelete( Triple t )
		{ L.delete( t ); }

	@Override public ExtendedIterator<Triple> _graphBaseFind( TripleMatch t ) 
		{ return L.find( t ). filterDrop ( ifIn( R ) ); }
	}
