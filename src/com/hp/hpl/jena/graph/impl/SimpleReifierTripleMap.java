/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: SimpleReifierTripleMap.java,v 1.1 2004-09-06 13:49:31 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;


class SimpleReifierTripleMap 
{
protected Map inverseMap = HashUtils.createMap();

protected Map forwardMap = HashUtils.createMap();    

public Triple putTriple( Node key, Triple value )
    {
    forwardMap.put( key, value );
    inversePut( value, key );
    return value;
    }

public void removeTriple( Node key, Triple value )
    {
    forwardMap.remove( key );
    inverseRemove( value, key );
    }

public ExtendedIterator allTriples( TripleMatch tm )
    {
    Triple t = tm.asTriple();
    Node subject = t.getSubject();
    if (subject.isConcrete())
        {
        Object x = forwardMap.get( subject );  
        return x == null
            ? new NiceIterator()
            : FragmentTripleIterator.toIterator( t, subject, x )
            ; 
        }
    else
        {
        final Iterator it = forwardMap.entrySet().iterator();   
        return new FragmentTripleIterator( t, it );
        }
    }
    
/**
    Return the fragment map as a read-only Graph of triples. We rely on the
    default code in GraphBase which allows us to only implement find(TripleMatch)
    to present a Graph. All the hard work is done by allTriples.
*/
public Graph asGraph()
    {
    return new GraphBase()
        { public ExtendedIterator find( TripleMatch tm ) { return allTriples( tm ); } };
    }

public void removeTriple( Node key )
    {
    Object t = forwardMap.get( key );
    forwardMap.remove( key );
    if (t instanceof Triple) inverseRemove( (Triple) t, key );
    }

/**
     Answer true iff we have a reified triple <code>t</code>.
*/
public boolean hasTriple( Triple t )
    { return inverseMap.containsKey( t ); }

/**
     Answer an iterator over all the fragment tags in this map.
 */
public Iterator tagIterator()
    { return forwardMap.keySet().iterator(); }
/**
 * @param value
 * @param key
 */
private void inverseRemove( Triple value, Node key )
    {
    Set s = (Set) inverseMap.get( value );
    if (s != null)
        {
        s.remove( key );
        if (s.isEmpty()) inverseMap.remove( value );
        }
    
    }

public Object get( Node tag )
    { return forwardMap.get( tag ); }

private void inversePut( Triple value, Node key )
    {
    Set s = (Set) inverseMap.get( value );
    if (s == null) inverseMap.put( value, s = new HashSet() );
    s.add( key );
    }        
}

/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/