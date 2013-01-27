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

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleBoundary;

/**
    StatementBoundaryBase - a base class for StatementBoundarys, with
    built-in converstion to triples and a continueWith as well as a stopAt.
*/
public abstract class StatementBoundaryBase implements StatementBoundary
    {
    /**
         Method to over-ride to define what stops the boundary search; default
         definition is !continueWith(s). <i>exactly one</code> of these two methods
         must be defined.
    */
    @Override
    public boolean stopAt( Statement s ) 
        { return !continueWith( s ); }

    /**
         Method to over-ride to define what continues the boundary search; default
         definition is !stopAt(s). <i>exactly one</code> of these two methods
         must be defined.
    */
    public boolean continueWith( Statement s ) 
        { return !stopAt( s ); }
    
    /**
         Expresses this StatementBoundary as a TripleBoundary.
    */
    @Override
    public final TripleBoundary asTripleBoundary( Model m ) 
        { return convert( m, this ); }

    /**
         Answer a TripleBoundary that is implemented in terms of a StatementBoundary. 
    */
    public static TripleBoundary convert( final Model s, final StatementBoundary b )
        {
        return new TripleBoundary()
            { @Override
            public boolean stopAt( Triple t ) { return b.stopAt( s.asStatement( t ) ); } };
        }
    }
