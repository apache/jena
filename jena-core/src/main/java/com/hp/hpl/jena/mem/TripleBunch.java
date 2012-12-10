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

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/**
    A bunch of triples - a stripped-down set with specialized methods. A
    bunch is expected to store triples that share some useful property 
    (such as having the same subject or predicate).

*/
public interface TripleBunch 
    {
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
    public abstract ExtendedIterator<Triple> iterator();
    
    /**
        Answer an iterator over all the triples in this bunch. If use of .remove on
        this iterator empties the bunch, the <code>emptied</code> method of
        <code>container</code> is invoked.
    */
    public abstract ExtendedIterator<Triple> iterator( HashCommon.NotifyEmpty container );
    
    }
