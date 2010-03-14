/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json;

import java.util.*;

public class JsonArray extends JsonValue implements List<JsonValue>
{
    private List<JsonValue> array = new ArrayList<JsonValue>() ;
    
    @Override
    public boolean isArray()        { return true ; }
    
    @Override
    public JsonArray getAsArray()   { return this ; }

    @Override
    public int hashCode()
    {
        return array.hashCode() ;
    }

    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof JsonArray) ) return false ;
        return array.equals(((JsonArray)other).array) ;
    }

    @Override
    public void visit(JsonVisitor visitor)
    { visitor.visit(this) ; }

    //@Override
    public boolean add(JsonValue e)
    {
        return array.add(e) ;
    }

    //@Override
    public void add(int index, JsonValue element)
    { array.add(index, element) ; } 

    //@Override
    public boolean addAll(Collection< ? extends JsonValue> c)
    {
        return array.addAll(c) ;
    }

    //@Override
    public boolean addAll(int index, Collection< ? extends JsonValue> c)
    {
        return array.addAll(index, c) ;
    }

    //@Override
    public void clear()
    { array.clear() ; }

    //@Override
    public boolean contains(Object object)
    {
        return array.contains(object) ;
    }

    //@Override
    public boolean containsAll(Collection< ? > c)
    {
        return array.containsAll(c) ;
    }

    //@Override
    public JsonValue get(int index)
    {
        return array.get(index) ;
    }

    //@Override
    public int indexOf(Object o)
    {
        return array.indexOf(o) ;
    }

    //@Override
    public boolean isEmpty()
    {
        return array.isEmpty() ;
    }

    //@Override
    public Iterator<JsonValue> iterator()
    {
        return array.iterator() ;
    }

    //@Override
    public int lastIndexOf(Object o)
    {
        return array.lastIndexOf(o) ;
    }

    //@Override
    public ListIterator<JsonValue> listIterator()
    {
        return array.listIterator() ;
    }

    //@Override
    public ListIterator<JsonValue> listIterator(int index)
    {
        return array.listIterator(index) ;
    }

    //@Override
    public boolean remove(Object o)
    {
        return array.remove(o) ;
    }

    //@Override
    public JsonValue remove(int index)
    {
        return array.remove(index) ;
    }

    //@Override
    public boolean removeAll(Collection< ? > c)
    {
        return array.removeAll(c) ;
    }

    //@Override
    public boolean retainAll(Collection< ? > c)
    {
        return  array.retainAll(c) ;
    }

    //@Override
    public JsonValue set(int index, JsonValue element)
    {
        return array.set(index, element) ;
    }

    //@Override
    public int size()
    {
        return array.size() ;
    }

    //@Override
    public List<JsonValue> subList(int fromIndex, int toIndex)
    {
        return array.subList(fromIndex, toIndex) ; 
    }

    //@Override
    public Object[] toArray()
    {
        return array.toArray() ;
    }

    //@Override
    public <T> T[] toArray(T[] a)
    {
        return array.toArray(a) ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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