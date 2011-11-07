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

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class BuilderBinding
{
    public static Binding build(Item item)
    {
        BuilderLib.checkList(item, "Attempt to build a binding from non-list: "+item) ;
        return buildBinding(item.getList()) ;
    }
    
    private static Binding buildBinding(ItemList list)
    {
        // (row or (binding
        if ( list.size() == 0 )
            BuilderLib.broken(list, "Empty list") ;
        
        Item head = list.get(0) ;
        
        if ( ! head.isSymbolIgnoreCase(Tags.tagRow) && ! head.isSymbolIgnoreCase(Tags.tagBinding) )
            BuilderLib.broken(list, "Does not start ("+Tags.tagRow+" ...) or ("+Tags.tagBinding+" ...)", head) ;
        
        BindingMap binding = BindingFactory.create() ;
        for ( int i = 1 ; i < list.size() ; i++ )
        {
            Item item = list.get(i) ;
            BuilderLib.checkList(item, "Attempt to build a binding pair from non-list: "+item) ;
            ItemList pair = item.getList() ;
            BuilderLib.checkLength(2, pair, "Need a pair for a binding") ;
            
            Var v = BuilderNode.buildVar(pair.get(0)) ;
            Item cdr = pair.get(1) ;
            // undef
            if ( cdr.isSymbolIgnoreCase(Tags.tagUndef) || cdr.isSymbolIgnoreCase(Tags.tagNull) )
                continue ;
            
            BuilderLib.checkNode(cdr) ;
            Node node = BuilderNode.buildNode(item.getList().get(1)) ;
            if ( node == null )
                BuilderLib.broken(item.getList().get(1), "Null node from "+item.getList().get(1)) ;
            if ( node.isVariable() )
                BuilderLib.broken(item.getList().get(1), "No variables as table values: "+FmtUtils.stringForNode(node)) ;
            if ( !node.isConcrete() )
                BuilderLib.broken(item.getList().get(1), "Ony concrete nodes as table values: "+FmtUtils.stringForNode(node)) ;
            binding.add(v, node) ;
        }
        return binding ;
    }
}
