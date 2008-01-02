/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;

public class BuilderNode
{
    public static Node buildNode(Item item)
    {
        if ( !item.isNode() )
            BuilderBase.broken(item, "Not a node", item) ;
        return item.getNode() ;
    }
    
    public static List buildNodeList(Item item)
    {
        BuilderBase.checkList(item) ;
        ItemList list = item.getList() ;
        return buildNodeList(list) ;
    }
    
    public static List buildNodeList(ItemList list)
    {
        List nodes = new ArrayList() ;
        for ( Iterator iter = list.iterator() ; iter.hasNext(); )
        {
            Item x = (Item)iter.next() ;
            Node v = buildNode(x) ;
            nodes.add(v) ;
        }
        return nodes ;
    }
    
    public static Var buildVar(Item item)
    {
        if ( ! item.isNode() || !Var.isVar(item.getNode()) )
            BuilderBase.broken(item, "Not a variable", item) ;
//        if ( ! Var.isNamedVar(item.getNode()) )
//            BuilderBase.broken(item, "Not a named variable", item) ;
        return Var.alloc(item.getNode()) ;
    }
    
    public static List buildVarList(Item item)
    {
        BuilderBase.checkList(item) ;
        ItemList list = item.getList() ;
        return buildVarList(list) ;
    }
        
    public static List buildVarList(ItemList list)
    {
        List vars = new ArrayList() ;
        for ( Iterator iter = list.iterator() ; iter.hasNext(); )
        {
            Item x = (Item)iter.next() ;
            Var v = buildVar(x) ;
            vars.add(v) ;
        }
        
        return vars ;
    }

    public static String buildSymbol(Item item)
    {
        if ( !item.isSymbol() )
            BuilderBase.broken(item, "Not a symbol", item) ;
        return item.getSymbol() ;
    }
    
    public static List buildVars(ItemList list)
    {
        List x = new ArrayList() ;
        for ( int i = 0 ; i < list.size() ; i++ )
        {
            Item item = list.get(i) ;
            Var var = BuilderNode.buildVar(item) ;
            x.add(Var.alloc(item.getNode()));
        }
        return x ;
    }

    private static BigInteger buildInteger(Item item, boolean allowDefault)
    {
        //Item item = list.get(idx) ;
        
        if ( allowDefault && item.equals(Item.defaultItem) )
            return null ;
        
        if ( !item.isNode() )
            BuilderBase.broken(item, "Not an integer: "+item) ;
        Node node = item.getNode() ;
        if ( ! node.isLiteral() )
            BuilderBase.broken(item, "Not an integer: "+item) ;

        NodeValue nv = NodeValue.makeNode(node) ;
        if ( ! nv.isInteger() )
            BuilderBase.broken(item, "Not an integer: "+item) ;
        return nv.getInteger() ;
    }

    public static int buildInt(Item item)
    { 
        BigInteger i = buildInteger(item, false) ;
        return i.intValue() ;
    }

    public static int buildInt(Item item, int dft)
    { 
        BigInteger i = buildInteger(item, true) ;
        if ( i == null )
            return dft ;
        return i.intValue() ;
    }

    public static int buildInt(ItemList list, int idx)
    {
        return buildInt(list.get(idx)) ;
    }
    
    public static int buildInt(ItemList list, int idx, int dft)
    { 
        return buildInt(list.get(idx), dft) ;
    }
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