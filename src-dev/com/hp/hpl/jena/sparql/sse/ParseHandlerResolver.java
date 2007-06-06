/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.Stack;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

public class  ParseHandlerResolver implements ParseHandler 
{
    static final String prefixTag = "prefix" ;
    Stack prefixTags = new Stack() ;
    Stack pmapStack = new Stack() ;
    PrefixMapping currentMap = new PrefixMappingImpl() ;
    
    // States 
    // 0 : Nothing
    // 1 : Waiting for list to start
    // 2 : Seeing prefix pairs
    // 3 : In
    
    private static final int  STATE_OUTSIDE         = 10 ;
    private static final int  STATE_PROCESS_DECL    = 20 ;
    private static final int  STATE_SEEN_DECL       = 30 ;
    private static final int  STATE_BODY            = 40 ;
    private int state = 0 ;
    int depth = 0 ;
    
    public void listStart(Item listItem)
    {
        if ( state == STATE_PROCESS_DECL )
            depth++ ;
    }
    
    public void listFinish(Item listItem)
    {
        if ( state == STATE_PROCESS_DECL )
            --depth ;
        
        if ( listItem.isTagged(prefixTag) )
        {
            pmapStack.pop() ;
            currentMap = (PrefixMapping)pmapStack.peek() ;
        }
    }
    
    public void listAdd(Item listItem, Item elt)
    {
        // Prefix.
        // 1 - spot the tag (do not add elements to list)
        // 2 - Get prefix mappings s(do not add elements to list)
        // 3 - process body
        
        if ( state == STATE_OUTSIDE &&
             listItem.getList().size() == 0 &&
             elt.isWord(prefixTag) )
        {
            // It's  (prefix ...)
            state = STATE_PROCESS_DECL ;
            // Remember this list 
            prefixTags.push(listItem) ;
            return ;
        }

        if ( listItem == prefixTags.peek() &&
             state == STATE_PROCESS_DECL )
        {
            // Adding the decls.
            PrefixMapping pm = parseDecls(elt) ;
            pmapStack.push(pm) ;
            state = STATE_BODY ;
            return ;
        }

        // Body, first element, clear up.
        // ?? If body missing/empty?
        if ( listItem == prefixTags.peek() &&
             state == STATE_BODY )
        {
            prefixTags.pop() ; 
            state = STATE_OUTSIDE ;
            // And next step will make list!=0 so no later tag detection at start of body 
        }

        listItem.getList().add(listItem) ;
    }
    
    // Process a prefix declaration list
    private PrefixMapping parseDecls(Item elt)
    {
        return null ;
    }

    public Item itemWord(Item item)     { return item ; }
    public Item itemNode(Item item)     { return item ; }
    public Item itemPName(Item item)    { return item ; }
    
//    private static void isPrefix(Item item)
//    {
//        item.isTagged(prefixTag) ;
//    }
    
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