/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemList extends ItemLocation //implements Iterable<Item> 
{
  private List items = new ArrayList() ;
  
  public ItemList(int line, int column)
  { super(line, column) ; }
  
  public ItemList() { super(noLine, noColumn) ; }
  
  public int size() { return items.size() ; }
  public boolean isEmpty() { return items.isEmpty() ; }
  
  public void addAll(ItemList itemList) { items.addAll(itemList.items) ; }
  public void add(Item item) { items.add(item) ; }
  public Item get(int idx) { return (Item)items.get(idx) ; }
//  public List getList() { return items ; }
  public Iterator iterator() { return items.iterator() ; }
  
  public String toString()
  { 
      String str = "" ;
      if ( hasLocation() )
          str = str.concat(location()) ;
      
      return str+items.toString() ; }
    
//    private List<Item> items = new ArrayList<Item>() ;
//    
//    public ItemList(int line, int column)
//    { super(line, column) ; }
//    
//    public ItemList() { super(noLine, noColumn) ; }
//    
//    public int size() { return items.size() ; }
//    public boolean isEmpty() { return items.isEmpty() ; }
//    
//    public void addAll(ItemList itemList) { items.addAll(itemList.items) ; }
//    public void add(Item item) { items.add(item) ; }
//    public Item get(int idx) { return items.get(idx) ; }
////    public List getList() { return items ; }
//    public Iterator<Item> iterator() { return items.iterator() ; }
//    
//    @Override
//    public String toString()
//    { 
//        String str = "" ;
//        if ( hasLocation() )
//            str = str.concat(location()) ;
//        
//        return str+items.toString() ; }
    
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