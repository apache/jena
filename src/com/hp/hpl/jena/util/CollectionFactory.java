/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: CollectionFactory.java,v 1.9 2009-01-16 18:03:19 andy_seaborne Exp $
*/
package com.hp.hpl.jena.util;

import java.util.*;

/**
    CollectionFactory - a central place for allocating sets and maps, mostly so that
    it's easy to plug in new implementations (eg trove).
    
 	@author kers
*/
public class CollectionFactory 
    {
    /**
         Answer a new Map which uses hashing for lookup.
    */
    public static <K,V> Map<K,V> createHashedMap() 
        { return new HashMap<K,V>(); }
    
    /**
         Answer a new Map which uses hashing for lookup and has initial size
         <code>size</code>.
    */
    public static <K,V> Map<K,V> createHashedMap( int size ) 
        { return new HashMap<K,V>( size ); }
    
    /**
         Answer a new Map which uses hashing for lookup and is initialised to be
         a copy of <code>toCopy</code>.
    */
    public static <K,V> Map<K,V> createHashedMap( Map<K,V> toCopy ) 
        { return new HashMap<K,V>( toCopy ); }
    
    /**
         Answer a new Set which uses haashing for lookup.
    */
    public static <T> Set<T> createHashedSet() 
        { return new HashSet<T>(); }
    
    /**
         Answer a new Set which uses hashing for lookup and is initialised as a copy
         of <code>toCopy</code>.
    */
    public static <T> Set<T> createHashedSet( Collection<T> toCopy ) 
        { return new HashSet<T>( toCopy ); }
    }

/*
    (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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