/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib ;


/** Key-value slot, with chaining for lookup. */  
public class Slot<K,V>
{
    private final Slot<K,V> previous ;
    private final K key ;
    private final V value ;

    /** Create a slot with no key, value or parent - can be used a slot chain root */  
    public Slot()               { this(null, null, null); }

    public Slot(K key, V value) { this(key, value, null); }

    private Slot(K key, V value, Slot<K, V> previous)
    {
        this.key = key ;
        this.value = value ;
        this.previous = previous ;
    }

    public Slot<K,V> extend(K key, V value)
    {
        return new Slot<K,V>(key, value, this) ;
    }
    
    public final V find(K k)
    {
        // Java, tail recursion, lack thereof.
        Slot<K,V> slot = this ;

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