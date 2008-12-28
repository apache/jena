/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.Tags;

public class BuilderTable
{
    public static Table build(Item item)
    {
        BuilderLib.checkTagged(item, Tags.tagTable, "Not a (table ...)") ;

        ItemList list = item.getList() ;
        int start = 1 ;
        if ( list.size() == 1 )
            // Null table;
            return TableFactory.createEmpty() ;

        if ( list.size() == 2 && list.get(1).isSymbol() )
        {
            //  Short hand for well known tables
            String symbol = list.get(1).getSymbol() ;
            if ( symbol.equals("unit") ) 
                return TableFactory.createUnit() ;
            if ( symbol.equals("empty") ) 
                return TableFactory.createEmpty() ;
            BuilderLib.broken(list, "Don't recognized table symbol") ;
        }
        
        Table table = TableFactory.create() ;
        
        int count = 0 ;
        Binding lastBinding = null ;
        for ( int i = start ; i < list.size() ; i++ )
        {
            Item itemRow = list.get(i) ;
            Binding b = BuilderBinding.build(itemRow) ;
            table.addBinding(b) ;
            lastBinding = b ;
            count++ ;
        }
        // Was it the unit table?
        
        if ( table.size() == 1 )
        {
            // One row, no bindings.
            if ( lastBinding.isEmpty() )
                return TableFactory.createUnit() ;
        }
        
        return table ;
    }
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