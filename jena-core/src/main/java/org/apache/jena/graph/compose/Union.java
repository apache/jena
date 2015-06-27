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

package org.apache.jena.graph.compose;

import java.util.Set ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.util.CollectionFactory ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
    A class representing the dynamic union of two graphs. Addition only affects the left 
    operand, deletion affects both. 
    @see MultiUnion
*/

public class Union extends Dyadic implements Graph 
	{
	public Union( Graph L, Graph R )
		{ super( L, R ); }
		
    /**
        To add a triple to the union, add it to the left operand; this is asymmetric.
    */
	@Override public void performAdd( Triple t )
		{ L.add( t ); }

    /**
        To remove a triple, remove it from <i>both</i> operands.
    */
	@Override public void performDelete( Triple t )
		{
		L.delete( t );
		R.delete( t );
		}

    @Override public boolean graphBaseContains( Triple t )
        { return L.contains( t ) || R.contains( t ); }
        
    /**
        To find in the union, find in the components, concatenate the results, and omit
        duplicates. That last is a performance penalty, but I see no way to remove it
        unless we know the graphs do not overlap.
    */
	@Override protected ExtendedIterator<Triple> _graphBaseFind( final Triple t ) 
	    {
	    Set<Triple> seen = CollectionFactory.createHashedSet();
        return recording( L.find( t ), seen ).andThen( rejecting( R.find( t ), seen ) ); 
	    // return L.find( t ) .andThen( rejecting( R.find( t ), L ) ); 
		}
	}
