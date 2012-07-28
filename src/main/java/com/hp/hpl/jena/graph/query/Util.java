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

package com.hp.hpl.jena.graph.query;

import java.util.Set;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;

/**
	Util: some utility code used by graph query that doesn't seem to belong 
    anywhere else that it can be put.

*/
public class Util 
    {
	/**
         Answer a new set which is the union of the two argument sets.
	*/
	public static <T> Set<T> union( Set<T> x, Set<T> y )
    	{
    	Set<T> result = CollectionFactory.createHashedSet( x );
    	result.addAll( y );
    	return result;
    	}

    /**
    	Answer a new set which contains exactly the names of the variable[ node]s
        in the triple.
    */
	public static Set<String> variablesOf( Triple t )
    	{
    	Set<String> result = CollectionFactory.createHashedSet();
        addIfVariable( result, t.getSubject() );
        addIfVariable( result, t.getPredicate() );
        addIfVariable( result, t.getObject() );
    	return result;
    	}
    
    private static void addIfVariable( Set<String> result, Node n )
        { if (n.isVariable()) result.add( n.getName() ); }
    }
