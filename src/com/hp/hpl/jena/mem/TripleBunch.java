/*
    (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: TripleBunch.java,v 1.6 2006-10-09 14:16:21 chris-dollin Exp $
*/
package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.StageElement;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    A bunch of triples - a strippled-down set with specialised methods. A
    bunch is expected tos tore triples that share some useful property 
    (such as having the same subject or predicate).
    
    @author kers
*/
public interface TripleBunch 
    {
    /**
        A TripleBunch may become empty as a side-effect of a .remove on one
        of its iterators: a container can request notification of this by passing
        a <code>NotifyEmpty</code> object in when the iterator is constructed,
        and its <code>emptied</code> method is called when the bunch
        becomes empty.
        @author kers
    */
    public interface NotifyEmpty
        { public void emptied(); }
    
    /**
        Answer true iff this TripleBunch contains a triple .equals to <code>t</code>.
    */
    public abstract boolean contains( Triple t );
    
    /**
        Answer true iff this TripleBunch contains a triple with .sameValueAs
        subject, predicate, and object. (Typically this only matters for the
        object. For example, integer literals with different numbers of leading
        zeroes can be .sameValueAs but not .equals).
    */
    public abstract boolean containsBySameValueAs( Triple t );
    
    /**
        Answer the number of triples in this bunch.
    */
    public abstract int size();
    
    /**
        Add <code>t</code> to the triples in this bunch. If <code>t</code>
        is already a member, nothing happens. The bunch now .contains this
        triple.
    */
    public abstract void add( Triple t );
    
    /**
         Remove <code>t</code> from the triples in this bunch. If it wasn't
         a member, nothing happens. The bunch no longer .contains this triple.
    */
    public abstract void remove( Triple t );
    
    /**
        Answer an iterator over all the triples in this bunch. It is unwise to
        .remove from this iterator. (It may become illegal.)
    */
    public abstract ExtendedIterator iterator();
    
    /**
        Answer an iterator over all the triples in this bunch. If use of .remove on
        this iterator empties the bunch, the <code>emptied</code> method of
        <code>container</code> is invoked.
    */
    public abstract ExtendedIterator iterator( NotifyEmpty container );
    
    /**
         For every triple t in this bunch that matches <code>s<code>, invoke
         <code>next.run(d)</code>. <code>d</code> may have been 
         side-effected by the match. <code>app</code> is the main reason
         that TripleBunch exists at all: it's a way to iterate as fast as possible
         over the triples in the context of a graph query, without having to
         construct an Iterator object which has to maintain the iteration state
         in instance variables.
    */
    public abstract void app( Domain d, StageElement next, MatchOrBind s );
    }
/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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