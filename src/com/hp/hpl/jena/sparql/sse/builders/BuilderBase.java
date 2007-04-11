/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.ItemLocation;
import com.hp.hpl.jena.sparql.sse.builders.ExprBuildException;
import com.hp.hpl.jena.sparql.util.FmtUtils;


public class BuilderBase
{
    public static String shortPrint(Item item)
    {
        if ( item.isWord() ) return item.getWord();
        if ( item.isNode() ) return FmtUtils.stringForNode(item.getNode());
        return shortPrint(item.getList()) ;
    }
    
    public static String shortPrint(ItemList list)
    {
        if ( list.size() == 0 ) return "()" ;
        if ( list.get(0).isWord() ) 
        {
            if ( list.size() == 1 )
                return "("+list.get(0).getWord()+")";
            else
                return "("+list.get(0).getWord()+" ...)";
        }
        return "(...)" ;
    }

    public static void checkNode(Item item)
    {
        if ( item.isNode() ) 
            return ;
        broken(item, "Not a node: "+BuilderBase.shortPrint(item)) ;
    }
    
    public static void checkWord(Item item)
    {
        if ( item.isWord() ) 
            return ;
        broken(item, "Not a word: "+BuilderBase.shortPrint(item)) ;
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
        if ( ! list.get(0).isWordIgnoreCase(tag) )
            broken(list, "List does not start ("+tag+ "...) : "+BuilderBase.shortPrint(list)) ;
    }

    public static void checkList(Item item)
    {
        if ( item.isList() ) 
            return ;
        broken(item, "Not a list: "+BuilderBase.shortPrint(item)) ;
    }

    public static void checkList(Item item, String msg)
    {
        if ( item.isList() )
            return ;
        if ( msg == null && item.isWord())
            msg = "Attempt to use a word where list expected: "+shortPrint(item) ;
        if ( msg == null && item.isNode())
            msg = "Attempt to use a node where list expected: "+shortPrint(item) ;
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
            msg =  "Wrong number of arguments: ("+len1+"-"+len2+")/"+list.size()+" : "+shortPrint(list) ;
        else
            msg = msg+" : "+shortPrint(list) ;
        broken(list, msg) ;
    }
    
    public static void checkLength(int len, ItemList list, String msg)
    {
        if ( list.size() == len )
            return ;
        
        if ( msg == null )
            msg =  "Wrong number of arguments: "+len+"/"+list.size()+" : "+shortPrint(list) ;
        else
            msg = msg+" : "+shortPrint(list) ;
        broken(list, msg) ;
    }

    
    
    public static void broken(ItemLocation location, String msg, Item item)
    {
        msg = msg(location, msg) ;
        System.err.println(msg+": "+shortPrint(item)) ;
        throw new ExprBuildException(msg) ;
    }

    public static void broken(ItemLocation location, String msg)
    {
        msg = msg(location, msg) ;
        System.err.println(msg) ;
        throw new ExprBuildException(msg) ;
    }
    
    public static String msg(ItemLocation location, String msg)
    {
        if ( location != null )
            msg = location.location()+" "+msg ;
        return msg ;
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