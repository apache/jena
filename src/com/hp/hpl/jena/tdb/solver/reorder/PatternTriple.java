/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.reorder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;


/** A mutable triple pattern */
public final class PatternTriple
{
    public Item subject ;
    public Item predicate ;
    public Item object ;
    
    public static PatternTriple parse(Item pt)
    { 
        ItemList list = pt.getList();
        return new PatternTriple(list.get(0), list.get(1), list.get(2)) ; 
    }
    
    public PatternTriple(Item s, Item p, Item o)
    {
        set(normalize(s), normalize(p), normalize(o)) ;
    }
    
    private void set(Item s, Item p, Item o) 
    {
        subject =    s ;
        predicate =  p ;
        object =     o ;
    }
    
    public PatternTriple(Node s, Node p, Node o)
    {
        set(normalize(s),
            normalize(p),
            normalize(o)) ;
    }
    
    public PatternTriple(Triple triple)
    {
        this(triple.getSubject(),
             triple.getPredicate(),
             triple.getObject()) ;
    }
    
    @Override
    public String toString()
    { return subject+" "+predicate+" "+object ; }
    
    private static Item normalize(Item x)
    { return x != null ? x : PatternElements.ANY ; }
    
    private static Item normalize(Node x)
    { return x != null ? Item.createNode(x) : PatternElements.ANY ; }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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