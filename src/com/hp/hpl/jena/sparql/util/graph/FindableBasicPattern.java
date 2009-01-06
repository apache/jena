/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;




class FindableBasicPattern implements Findable
{
    private BasicPattern triples ;

    FindableBasicPattern(BasicPattern triples) { this.triples = triples ; }

    public Iterator<Triple> find(Node s, Node p, Node o)
    {
        if ( s == Node.ANY ) s = null ;
        if ( p == Node.ANY ) p = null ;
        if ( o == Node.ANY ) o = null ;
        
        List<Triple> r = new ArrayList<Triple>() ;
        for ( Iterator<Triple> iter = triples.iterator() ; iter.hasNext(); )
        {
            Triple t = iter.next();
            if ( s != null && ! t.getSubject().equals(s) ) continue ;
            if ( p != null && ! t.getPredicate().equals(p) ) continue ;
            if ( o != null && ! t.getObject().equals(o) ) continue ;
            r.add(t) ;
        }
        return r.iterator() ;
    }
    
    public boolean contains(Node s, Node p, Node o)
    {
        if ( s == Node.ANY ) s = null ;
        if ( p == Node.ANY ) p = null ;
        if ( o == Node.ANY ) o = null ;
        for ( Iterator<Triple> iter = triples.iterator() ; iter.hasNext(); )
        {
            Triple t = iter.next();
            if ( s != null && ! t.getSubject().equals(s) ) continue ;
            if ( p != null && ! t.getPredicate().equals(p) ) continue ;
            if ( o != null && ! t.getObject().equals(o) ) continue ;
            return true ;
        }
        return false ;
    }
}


/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */