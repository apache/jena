/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lib;

import java.util.*;

import com.hp.hpl.jena.sparql.lib.iterator.Iter;

/** Datastructure factory - allows indirecly to other implementations */ 

public class DS
{
    private DS() {}
    
    public static <X> Set<X> set() { return new HashSet<X>(); }  
    public static <X> Set<X> set(int initialSize) { return new HashSet<X>(initialSize); }  
    public static <X> Set<X> set(Set<X> other) { return new HashSet<X>(other); }  

    // Trove for sets
//    public static <X> Set<X> set() { return new gnu.trove.THashSet<X>(); }  
//    public static <X> Set<X> set(int initialSize) { return new gnu.trove.THashSet<X>(initialSize); }  
//    public static <X> Set<X> set(Set<X> other) { return new gnu.trove.THashSet<X>(other); }  
    
    public static <K, V> Map<K,V> map() { return new HashMap<K,V>(); }  
    public static <K, V> Map<K,V> map(int initialSize) { return new HashMap<K,V>(initialSize); }  
    public static <K, V> Map<K,V> map(Map<K,V> other) { return new HashMap<K,V>(other); }  

    public static <T> Iterator<T> nothing() { return Iter.nullIterator() ; }
    
    public static <T> List<T> list() { return new ArrayList<T>(); }  
    public static <T> List<T> list(int initialSize) { return new ArrayList<T>(initialSize); }  
    public static <T> List<T> list(List<T> other) { return new ArrayList<T>(other); }

    // Trove for maps
//  public static <K, V> Map<K,V> map() { return new gnu.trove.THashMap<K,V>(); }  
//  public static <K, V> Map<K,V> map(int initialSize) { return new gnu.trove.THashMap<K,V>(initialSize); }  
//  public static <K, V> Map<K,V> map(Map<K,V> other) { return new gnu.trove.THashMap<K,V>(other); }  
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