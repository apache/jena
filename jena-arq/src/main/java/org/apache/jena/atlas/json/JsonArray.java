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

package org.apache.jena.atlas.json;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;
import java.util.ListIterator ;

public class JsonArray extends JsonValue implements List<JsonValue>, Iterable<JsonValue>
{
    private List<JsonValue> array = new ArrayList<>() ;
    
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

    @Override
    public boolean add(JsonValue e)
    {
        return array.add(e) ;
    }

    public boolean add(String e)
    {
        return array.add(new JsonString(e)) ;
    }

    public boolean add(long val)
    {
        return array.add(JsonNumber.value(val)) ;
    }

    public boolean add(boolean b)
    {
        return array.add(new JsonBoolean(b)) ;
    }

    @Override
    public void add(int index, JsonValue element)
    { array.add(index, element) ; } 

    @Override
    public boolean addAll(Collection< ? extends JsonValue> c)
    {
        return array.addAll(c) ;
    }

    @Override
    public boolean addAll(int index, Collection< ? extends JsonValue> c)
    {
        return array.addAll(index, c) ;
    }

    @Override
    public void clear()
    { array.clear() ; }

    @Override
    public boolean contains(Object object)
    {
        return array.contains(object) ;
    }

    @Override
    public boolean containsAll(Collection< ? > c)
    {
        return array.containsAll(c) ;
    }

    @Override
    public JsonValue get(int index)
    {
        return array.get(index) ;
    }

    @Override
    public int indexOf(Object o)
    {
        return array.indexOf(o) ;
    }

    @Override
    public boolean isEmpty()
    {
        return array.isEmpty() ;
    }

    @Override
    public Iterator<JsonValue> iterator()
    {
        return array.iterator() ;
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return array.lastIndexOf(o) ;
    }

    @Override
    public ListIterator<JsonValue> listIterator()
    {
        return array.listIterator() ;
    }

    @Override
    public ListIterator<JsonValue> listIterator(int index)
    {
        return array.listIterator(index) ;
    }

    @Override
    public boolean remove(Object o)
    {
        return array.remove(o) ;
    }

    @Override
    public JsonValue remove(int index)
    {
        return array.remove(index) ;
    }

    @Override
    public boolean removeAll(Collection< ? > c)
    {
        return array.removeAll(c) ;
    }

    @Override
    public boolean retainAll(Collection< ? > c)
    {
        return  array.retainAll(c) ;
    }

    @Override
    public JsonValue set(int index, JsonValue element)
    {
        return array.set(index, element) ;
    }

    @Override
    public int size()
    {
        return array.size() ;
    }

    @Override
    public List<JsonValue> subList(int fromIndex, int toIndex)
    {
        return array.subList(fromIndex, toIndex) ; 
    }

    @Override
    public Object[] toArray()
    {
        return array.toArray() ;
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return array.toArray(a) ;
    }
}
