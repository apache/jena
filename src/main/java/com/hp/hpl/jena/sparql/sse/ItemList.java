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

package com.hp.hpl.jena.sparql.sse;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;

public class ItemList extends ItemLocation implements Iterable<Item> 
{
    private final List<Item> elements ;
    // Pointer to the start of the list
    // Because of repeated cdr's, we can not use a .subList (leads to a whole
    // chain subLists, one per call).
    private final int offset ;       
    private int index(int i) { return offset+i ; }
    private int _size()      { return elements.size()-offset; }
    private List<Item> slice()     { return elements.subList(offset, elements.size()) ; }
    
    public ItemList(int line, int column)
    { this(line, column, 0, new ArrayList<Item>()) ; }

    public ItemList() { this(noLine, noColumn) ; }

    public ItemList(int line, int column, int offset, List<Item> elts)
    { 
        super(line, column) ;
        this.elements = elts ;
        this.offset = offset ;
    }
    
    public int size() { return _size() ; }
    public boolean isEmpty() { return _size()==0 ; }

    @Override
    public int hashCode() { return elements.hashCode() ^ offset ; }
    @Override
    public boolean equals(Object other)
    { 
        if ( this == other ) return true ;
        if ( ! ( other instanceof ItemList) ) return false ;
        ItemList list = (ItemList)other ;
        
        if ( list.size() != size() ) return false ;
        return slice().equals(list.slice()) ;
    }
    
    public void addAll(ItemList itemList) { elements.addAll(itemList.elements) ; }
    
    public void add(Item item){ elements.add(item) ; }
    public void add(Node node){ elements.add(Item.createNode(node)) ; }
    public void add(String symbol){ elements.add(Item.createSymbol(symbol)) ; }
    
    public Item get(int idx) { return elements.get(index(idx)) ; }

    public Item getFirst()      { return get(0) ; }

    public Item getLast()       { return get(_size()-1) ; }

    @Override
    public Iterator<Item> iterator() { return elements.listIterator(offset) ; }
    
    public Item car()
    { 
        if (  _size() == 0 )
            throw new ItemException("ItemList.car: list is zero length") ;
        return get(0) ;
    }
    public ItemList cdr()
    {
        if (  _size() == 0 )
            throw new ItemException("ItemList.cdr: list is zero length") ;
        ItemList x = new ItemList(super.getLine(), super.getColumn(), offset+1, elements) ;
        return x ;
    }
    
    public ItemList cdrOrNull()
    {
        if (  _size() == 0 )
            return null ;
        ItemList x = new ItemList(super.getLine(), super.getColumn(), offset+1, elements) ;
        return x ;
    }
    
    public ItemList sublist(int start)
    {
        if (  _size() < start+offset )
            return null ;
        ItemList x = new ItemList(noLine, noColumn, offset+start, elements) ;
        return x ;
    }
    
    /** Slice of the list from start (inclusive) to finish (exclusive) */
    public ItemList sublist(int start, int finish)
    {
        if ( start < 0 || finish < 0 || finish < start )
            return null ;
        
        if (  _size() < start )
            return null ;
        if ( finish > _size() )
            return null ;
        
        ItemList x = new ItemList() ;
        // Take a slice.
        // Note : this is a copy.
        // Note: List.subList just puts a wrapper around the overlying list
        // but don't do this a lot because ArrayList.get recurses to the core list (may run out of stack).
        
        // Better would an adjusting slice over base array but it's quite tricky to do the calculations correctly.
        // This way is mnore likely to be correct.
        
        x.elements.addAll(elements.subList(start+offset, finish+offset)) ;
        return x ;
    }
    
    public String shortString()
    {
        if (  _size() == 0 ) return "()" ;
        if ( get(0).isSymbol() ) 
        {
            if ( _size() == 1 )
                return "("+get(0).getSymbol()+")";
            else
                return "("+get(0).getSymbol()+" ...)";
        }
        return "(...)" ;
    }
    
    @Override
    public String toString()
    { 
        String str = "" ;
        if ( hasLocation() )
            str = str.concat(location()) ;
        return str+slice().toString() ; }
}
