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

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;

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

        // Maybe vars.
        List<Var> vars = null ; 
        if ( list.size() > 1 )
        {
            Item item0 = list.get(1) ;
            if ( item0.isTagged(Tags.tagVars) )
            {
                vars = BuilderNode.buildVarList(item0) ;
                list = list.cdr() ;
            }
        }
        
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
        
        Table table = TableFactory.create(vars) ;
        
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
