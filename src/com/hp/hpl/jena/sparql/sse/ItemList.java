/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;

public class ItemList extends ItemLocation //implements Iterable<Item> 
{
    private final List elements ;
    // Pointer to the start of the list
    // Because of repeated cdr's, we can not use a .subList (leads to a whole
    // chain subLists, one per call).
    private final int offset ;       
    private int index(int i) { return offset+i ; }
    private int _size()      { return elements.size()-offset; }
    private List slice()     { return elements.subList(offset, elements.size()) ; }
    
    public ItemList(int line, int column)
    { this(line, column, 0, new ArrayList()) ; }

    public ItemList() { this(noLine, noColumn) ; }

    public ItemList(int line, int column, int offset, List elts)
    { 
        super(line, column) ;
        this.elements = elts ;
        this.offset = offset ;
    }
    
    public int size() { return _size() ; }
    public boolean isEmpty() { return _size()==0 ; }

    public int hashCode() { return elements.hashCode() ^ offset ; }
    public boolean equals(Object other)
    { 
        if ( ! ( other instanceof ItemList) ) return false ;
        ItemList list = (ItemList)other ;
        
        if ( list.size() != size() ) return false ;
        return slice().equals(list.slice()) ;
    }
    
    public void addAll(ItemList itemList) { elements.addAll(itemList.elements) ; }
    
    public void add(Item item){ elements.add(item) ; }
    public void add(Node node){ elements.add(Item.createNode(node)) ; }
    public void add(String symbol){ elements.add(Item.createSymbol(symbol)) ; }
    
    public Item get(int idx) { return (Item)elements.get(index(idx)) ; }

    public Item getFirst()      { return get(0) ; }

    public Item getLast()       { return get(_size()-1) ; }

    public Iterator iterator() { return elements.listIterator(offset) ; }
    
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
    
    public String toString()
    { 
        String str = "" ;
        if ( hasLocation() )
            str = str.concat(location()) ;
        return str+slice().toString() ; }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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