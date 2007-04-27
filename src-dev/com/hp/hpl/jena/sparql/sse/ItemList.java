/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemList extends ItemLocation //implements Iterable<Item> 
{
    private List elements = new ArrayList() ;

    public ItemList(int line, int column)
    { super(line, column) ; }

    public ItemList() { super(noLine, noColumn) ; }

    public int size() { return elements.size() ; }
    public boolean isEmpty() { return elements.isEmpty() ; }

    public void addAll(ItemList itemList) { elements.addAll(itemList.elements) ; }
    public void add(Item item){ elements.add(item) ; }
    public Item get(int idx) { return (Item)elements.get(idx) ; }
//  public List getList() { return items ; }
    public Iterator iterator() { return elements.iterator() ; }
    
    public Item     car()
    { 
        if ( elements.size() == 0 )
            throw new ItemException("ItemList.car: list is zero length") ;
        return (Item)elements.get(0) ;
    }
    public ItemList cdr()
    {
        if ( elements.size() == 0 )
            throw new ItemException("ItemList.cdr: list is zero length") ;
        ItemList x = new ItemList(super.getLine(), super.getColumn()) ;
        if ( elements.size() == 0 )
            return x ; 
        x.elements = this.elements.subList(1, size()) ;
        return x ;
    }
    
    public String toString()
    { 
        String str = "" ;
        if ( hasLocation() )
            str = str.concat(location()) ;

        return str+elements.toString() ; }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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