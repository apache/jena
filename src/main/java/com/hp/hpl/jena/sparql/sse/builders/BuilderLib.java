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

import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.ItemLocation ;

public class BuilderLib
{
 
    public static void checkNode(Item item)
    {
        if ( item.isNode() ) 
            return ;
        broken(item, "Not a node: "+item.shortString()) ;
    }
    
    public static void checkSymbol(Item item)
    {
        if ( item.isSymbol() ) 
            return ;
        broken(item, "Not a symbol: "+item.shortString()) ;
    }
    
    public static void checkTagged(Item item, String tag, String msg)
    {
        if ( item.isTagged(tag) ) return ;
        broken(item, msg, item) ;
    }

    public static void checkTagged(Item item, int len, String tag, String msg)
    {
        if ( item.isTagged(tag) && item.getList().size() == len ) 
            return ;
        broken(item, msg, item) ;
    }

    
    public static void checkTag(ItemList list, String tag)
    {
        if ( list.size() == 0 )
            broken(list, "Empty list") ;
        if ( ! list.get(0).isSymbolIgnoreCase(tag) )
            broken(list, "List does not start ("+tag+ "...) : "+list.shortString()) ;
    }

    public static void checkList(Item item)
    {
        if ( item.isList() ) 
            return ;
        broken(item, "Not a list: "+item.shortString()) ;
    }

    public static void checkList(Item item, String msg)
    {
        if ( item.isList() )
            return ;
        if ( msg == null && item.isSymbol())
            msg = "Attempt to use a symbol where list expected: "+item.shortString() ;
        if ( msg == null && item.isNode())
            msg = "Attempt to use a node where list expected: "+item.shortString() ;
        if ( msg == null )
            msg = "Not a list" ;
        broken(item, msg) ; 
    }
    
    public static void warning(ItemLocation location, String msg)
    {
        msg = msg(location, msg) ;
        System.err.println(msg) ;
    }

    public static void checkLength(int len1, int len2, ItemList list, String msg)
    {
        if ( list.size() >= len1 && list.size() <= len2 )
            return ; 
        if ( msg == null )
            msg =  "Wrong number of arguments: ("+len1+"-"+len2+")/"+list.size()+" : "+list.shortString() ;
        else
            msg = msg+" : "+list.shortString() ;
        broken(list, msg) ;
    }
    
    
    
    public static void checkLength(int len, ItemList list, String msg)
    {
        if ( list.size() == len )
            return ;
        
        if ( msg == null )
            msg =  "Wrong number of arguments: "+len+"/"+list.size()+" : "+list.shortString() ;
        else
            msg = msg+" : "+list.shortString() ;
        broken(list, msg) ;
    }

    public static void checkLengthAtLeast(int len, ItemList list, String msg)
    {
        if ( list.size()>= len )
            return ;
        
        if ( msg == null )
            msg =  "Too few arguments: want > "+len+" :got : "+list.size()+" : "+list.shortString() ;
        else
            msg = msg+" : "+list.shortString() ;
        broken(list, msg) ;
    }
    
    public static void broken(Item item, String msg)
    {
        broken(item, msg, item) ;
    }
    
    public static void broken(String msg)
    {
        System.err.println(msg) ;
        exception(msg) ;
    }
    
    public static void exception(String msg)
    {
        throw new ExprBuildException(msg) ;
    }
    
    public static void broken(ItemLocation location, String msg, Item item)
    {
        msg = msg(location, msg) ;
        System.err.println(msg+": "+item.shortString()) ;
        exception(msg) ;
    }

    public static void broken(ItemList list, String msg)
    {
        broken(list, msg, list) ; 
    }

    public static void broken(ItemLocation location, String msg, ItemList list)
    {
        msg = msg(location, msg) ;
        System.err.println(msg+": "+list.shortString()) ;
        exception(msg) ;
    }
    
    public static void broken(ItemLocation location, String msg)
    {
        msg = msg(location, msg) ;
        System.err.println(msg) ;
        exception(msg) ;
    }
    
    public static String msg(ItemLocation location, String msg)
    {
        if ( location != null )
            msg = location.location()+": "+msg ;
        return msg ;
    }

    public static ItemList skipTag(ItemList list, String tag)
    {
        if ( list.size() > 0 )
        {
            if ( list.get(0).isSymbol(tag) )
                list = list.cdr() ;
        }
        return list ;
    }
}
