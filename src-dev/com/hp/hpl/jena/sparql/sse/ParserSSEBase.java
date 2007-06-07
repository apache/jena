/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.lang.ParserBase;

public class ParserSSEBase extends ParserBase
{
    private ParseHandler handler = new ParseHandlerPlain() ;
    
    public void setHandler(ParseHandler handler) { this.handler = handler ; }
    
    private VarAlloc varAlloc = new VarAlloc("") ;
    protected Var createVariable()
    {
        return varAlloc.allocVar() ;
    }
    
    //@Override
    protected Node createNodeFromPrefixedName(String s, int line, int column)
    {
        LogFactory.getLog(ParserSSEBase.class).warn("Call to createNodeFromPrefixedName") ;
        return Node.createURI(":"+s) ;
    }
    
    protected void listStart(Item list)
    { 
        if ( handler == null ) return ;
        handler.listStart(list) ;
    }
    
    protected void listFinish(Item list)
    {
        if ( handler == null ) return ;
        handler.listFinish(list) ;
    }

    protected void listAdd(Item list, Item elt)
    {
        if ( handler == null )
            list.getList().add(elt) ;
        handler.listAdd(list, elt) ;
    }
    
    protected Item itemWord(Item item)
    { 
        if ( handler == null ) return item ;
        Item item2 = handler.itemWord(item) ; 
        return item2 != null ? item2 : item ; 
    }
    
    protected Item itemNode(Item item)
    { 
        if ( handler == null ) return item ;
        Item item2 = handler.itemNode(item) ; 
        return item2 != null ? item2 : item ; 
    }

    protected Item itemPName(Item item)
    { 
        if ( handler == null ) return item ;
        Item item2 = handler.itemPName(item) ; 
        return item2 != null ? item2 : item ; 
    }
    
    protected void throwParseException(String msg, int line, int column)
    {
        throw new SSEParseException("Line " + line + ", column " + column + ": " + msg,
                                    line, column) ;
    }
    
    static class ParseHandlerNull implements ParseHandler
    {
        public Item itemNode(Item item)             { return item ; }
        public Item itemPName(Item item)            { return item ; }
        public Item itemWord(Item item)             { return item ; }
        public void listAdd(Item item, Item elt)    { return ; }
        public void listFinish(Item item)           { return ; }
        public void listStart(Item item)            { return ; }
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