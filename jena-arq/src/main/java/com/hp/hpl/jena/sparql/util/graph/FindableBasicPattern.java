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

package com.hp.hpl.jena.sparql.util.graph;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;




class FindableBasicPattern implements Findable
{
    private BasicPattern triples ;

    FindableBasicPattern(BasicPattern triples) { this.triples = triples ; }

    @Override
    public Iterator<Triple> find(Node s, Node p, Node o)
    {
        if ( s == Node.ANY ) s = null ;
        if ( p == Node.ANY ) p = null ;
        if ( o == Node.ANY ) o = null ;
        
        List<Triple> r = new ArrayList<>() ;
        for ( Triple t : triples )
        {
            if ( s != null && !t.getSubject().equals( s ) )
            {
                continue;
            }
            if ( p != null && !t.getPredicate().equals( p ) )
            {
                continue;
            }
            if ( o != null && !t.getObject().equals( o ) )
            {
                continue;
            }
            r.add( t );
        }
        return r.iterator() ;
    }
    
    @Override
    public boolean contains(Node s, Node p, Node o)
    {
        if ( s == Node.ANY ) s = null ;
        if ( p == Node.ANY ) p = null ;
        if ( o == Node.ANY ) o = null ;
        for ( Triple t : triples )
        {
            if ( s != null && !t.getSubject().equals( s ) )
            {
                continue;
            }
            if ( p != null && !t.getPredicate().equals( p ) )
            {
                continue;
            }
            if ( o != null && !t.getObject().equals( o ) )
            {
                continue;
            }
            return true;
        }
        return false ;
    }
}
