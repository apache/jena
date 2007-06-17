/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

/** Spliter for parser handlers.
 *  Any results come from the second handler.
 * @author Andy Seaborne
 * @version $Id$
 */

public class ParseHandler2 implements ParseHandler
{
    private ParseHandler handler1 ;
    private ParseHandler handler2 ;
    
    public ParseHandler2(ParseHandler handler1, ParseHandler handler2)
    {
        this.handler1 = handler1 ;
        this.handler2 = handler2 ;
    }
    
    public void listStart(Item list)
    {
        handler1.listStart(list) ;
        handler2.listStart(list) ;
    }
    
    public Item listFinish(Item list)
    {
        handler1.listFinish(list) ;
        return handler2.listFinish(list) ;
    }
    
    public void listAdd(Item list, Item elt)
    {
        handler1.listAdd(list, elt) ;
        handler2.listAdd(list, elt) ;
    }
    
    public Item itemNode(Item item)
    {
        handler1.itemNode(item) ;
        return handler2.itemNode(item) ;
    }
    

    public Item itemWord(Item item)
    {
        handler1.itemWord(item) ;
        return handler2.itemWord(item) ;
    }

    public void parseFinish()
    {
        handler1.parseFinish() ;
        handler2.parseFinish() ;
    }

    public void parseStart()
    {
        handler1.parseStart() ;
        handler2.parseStart() ;
    }

    public String resolvePName(String pname)
    {
        handler1.resolvePName(pname) ;
        return handler2.resolvePName(pname) ;
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