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
    Stack listStack = new Stack() ;
    Stack pmapStack = new Stack() ;
    PrefixMapping currentMap = new PrefixMappingImpl() ;
    
    public void listStart(Item listItem)
    {
        listStack.push(listItem) ;
    }
    
    public void listFinish(Item listItem)
    {
        if ( listItem.isTagged(prefixTag) )
        {
            pmapStack.pop() ;
            currentMap = (PrefixMapping)pmapStack.peek() ;
        }
        listStack.pop() ;
    }
    
    public void listAdd(Item listItem, Item elt)
    {
        if ( listItem.getList().size() == 0 
            && elt.isWord(prefixTag) )
        {
            // wait for first thing which must be a 
        }
        
        
        // And skip this?
    }
    
    public Item itemWord(Item item) { return item ; }
    public Item itemNode(Item item) { return item ; }
    public Item itemPName(Item item) { return item ; }
    
    private static void isPrefix(Item item)
    {
        item.isTagged(prefixTag) ;
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