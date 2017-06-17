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

package org.apache.jena.sparql.util.graph;

import java.util.*;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;

public class FindableCollection implements Findable
{
    private Collection<Triple> triples ;

    public FindableCollection(Collection<Triple> triples) { this.triples = triples ; }
    
    @Override
    public Iterator<Triple> find(Node _s, Node _p, Node _o) {
        Node s = m(_s) ;
        Node p = m(_p) ;
        Node o = m(_o) ;
        return Iter.filter(triples.iterator(), (t)->matches(t, s, p, o)); 
    }
    
    static Node m(Node n) {
        return n == Node.ANY ? null : n ; 
    }
    
    // Does concrete t match the pattern (s,p,o)?
    /*package*/ static boolean matches(Triple t, Node s, Node p, Node o) {
        if ( s != null && ! Objects.equals(s, t.getSubject()) )
            return false ;
        if ( p != null && ! Objects.equals(p, t.getPredicate()) )
            return false ;
        if ( o != null && ! Objects.equals(o, t.getObject()) )
            return false ;
        return true ;
    }
    
    @Override
    public boolean contains(Node s, Node p, Node o)
    {
        if ( s == Node.ANY ) s = null ;
        if ( p == Node.ANY ) p = null ;
        if ( o == Node.ANY ) o = null ;
        for ( Triple t : triples ) {
            if ( matches(t, s, p, o) )
                return true;
        }
        return false ;
    }
}
