/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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