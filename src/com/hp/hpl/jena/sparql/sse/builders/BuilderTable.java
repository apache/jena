/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;

public class BuilderTable
{
    static final public String tagTable = "table" ;
    
    static public interface Build { Op make(ItemList list) ; }
    
    public static Table build(Item item)
    {
        BuilderBase.checkTagged(item, tagTable, "Not a (table ...)") ;
//        return buildTable(item.getList()) ;
//        //return null ;
//    }
//
//    private static Table buildTable(ItemList list)
//    {
//        if ( list.size() == 0 )
//            Builder.broken(list, "Empty list") ;
//        if ( ! list.get(0).isWord())
//            Builder.broken(list, "Does not start with a tag: "+list.get(0)) ;
//        String tag = list.get(0).getWord() ;
//        
//        Builder.checkTag(list, tagTable) ;
//        
        ItemList list = item.getList() ;
        int start = 1 ;
        if ( list.size() == 1 )
            // Null table;
            return TableFactory.createEmpty() ;

        // Result set - cols.
//      List vars = null ;
//      Item itemCols = list.get(1) ;
//      if ( itemCols.isList() && itemCols.getList().get(0).isWordIgnoreCase("cols") )
//      {
//          vars = BuilderNode.buildVars(list.get(1).getList(), 1) ;
//          start++ ;
//      }
//          ....        
//      if ( vars != null )
//          table.setVars(vars) ;
        
        Table table = TableFactory.create() ;
        for ( int i = start ; i < list.size() ; i++ )
        {
            Item itemRow = list.get(i) ;
            Binding b = BuilderBinding.build(itemRow) ;
            table.addBinding(b) ;
        }
        return table ;
    }
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