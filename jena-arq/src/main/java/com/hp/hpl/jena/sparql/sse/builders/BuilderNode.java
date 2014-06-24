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

import java.math.BigInteger ;
import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class BuilderNode
{
    public static Node buildNode(Item item)
    {
        if ( item.isSymbol("true") )
            return NodeConst.nodeTrue ;
        if ( item.isSymbol("false") )
            return NodeConst.nodeFalse ;
        if ( !item.isNode() )
            BuilderLib.broken(item, "Not a node", item) ;
        return item.getNode() ;
    }
    
    public static List<Node> buildNodeList(Item item)
    {
        BuilderLib.checkList(item) ;
        ItemList list = item.getList() ;
        return buildNodeList(list) ;
    }
    
    public static List<Node> buildNodeList(ItemList list)
    {
        List<Node> nodes = new ArrayList<>() ;
        for (Item item : list)
        {
            Node v = buildNode(item) ;
            nodes.add(v) ;
        }
        return nodes ;
    }
    
    public static Var buildVar(Item item)
    {
        if ( ! item.isNode() || !Var.isVar(item.getNode()) )
            BuilderLib.broken(item, "Not a variable", item) ;
//        if ( ! Var.isNamedVar(item.getNode()) )
//            BuilderBase.broken(item, "Not a named variable", item) ;
        return Var.alloc(item.getNode()) ;
    }
    
    public static List<Var> buildVarList(Item item)
    {
        BuilderLib.checkList(item) ;
        ItemList list = item.getList() ;
        return buildVarList(list) ;
    }
        
    public static List<Var> buildVarList(ItemList list)
    {
        if ( list.size() > 0 && list.getFirst().isSymbol(Tags.tagVars) )
            list = list.cdr() ;
        
        List<Var> vars = new ArrayList<>() ;
        for (Item x : list)
        {
            Var v = buildVar(x) ;
            vars.add(v) ;
        }
        
        return vars ;
    }

    public static String buildSymbol(Item item)
    {
        if ( !item.isSymbol() )
            BuilderLib.broken(item, "Not a symbol", item) ;
        return item.getSymbol() ;
    }
    
    public static List<Var> buildVars(ItemList list)
    {
        list = BuilderLib.skipTag(list, Tags.tagVars) ;
        
        List<Var> x = new ArrayList<>() ;
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
            BuilderLib.broken(item, "Not an integer: "+item) ;
        Node node = item.getNode() ;
        if ( ! node.isLiteral() )
            BuilderLib.broken(item, "Not an integer: "+item) ;

        NodeValue nv = NodeValue.makeNode(node) ;
        if ( ! nv.isInteger() )
            BuilderLib.broken(item, "Not an integer: "+item) ;
        return nv.getInteger() ;
    }

    public static long buildLong(Item item)
    { 
        BigInteger i = buildInteger(item, false) ;
        return i.longValue() ;
    }

    public static long buildLong(Item item, int dft)
    { 
        BigInteger i = buildInteger(item, true) ;
        if ( i == null )
            return dft ;
        return i.longValue() ;
    }

    public static long buildLong(ItemList list, int idx)
    {
        return buildLong(list.get(idx)) ;
    }
    
    public static long buildLong(ItemList list, int idx, int dft)
    { 
        return buildLong(list.get(idx), dft) ;
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
