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

package org.apache.jena.atlas.lib ;


/** Key-value slot, with chaining for lookup. */  
public class Cell<K,V>
{
    private final Cell<K,V> previous ;
    private final K key ;
    private final V value ;

    /** Create a slot with no key, value or parent - can be used a slot chain root */  
    public Cell()               { this(null, null, null); }

    public Cell(K key, V value) { this(key, value, null); }

    private Cell(K key, V value, Cell<K, V> previous)
    {
        this.key = key ;
        this.value = value ;
        this.previous = previous ;
    }

    public Cell<K,V> extend(K key, V value)
    {
        return new Cell<>(key, value, this) ;
    }
    
    public final V find(K k)
    {
        // Java, tail recursion, lack thereof.
        Cell<K,V> slot = this ;

        while (slot != null)
        {
            // Defend against null keys (e.g. the root of a slot chain). 
            if ( k.equals(slot.key) )
                return slot.value ;
//            if ( previous == null )
//              return null ;
            slot = slot.previous ;
        }
        return null ;
    }
    
      /* As it should be ... */
//    public final V find(K k)
//    {
//        if ( k.equals(key) )
//            return value ;
//        if ( previous == null )
//            return null ;
//        return previous.find(k) ;
//    }

    private static final String sep = ", " ;
    private void str(int level, StringBuilder acc)
    {
        if ( key == null && value == null )
            return ;

        if ( level != 0 )
            acc.append(sep) ;
        acc.append("(") ;
        acc.append(key.toString()) ;
        acc.append("->") ;
        acc.append(value.toString()) ;
        acc.append(")") ;
        if ( previous != null )
            previous.str(level+1, acc) ;
    }

    @Override
    public String toString()
    { 
        StringBuilder sb = new StringBuilder() ;
        sb.append("{ ") ;
        str(0, sb) ;
        sb.append(" }") ;
        return sb.toString() ;
    }
}
