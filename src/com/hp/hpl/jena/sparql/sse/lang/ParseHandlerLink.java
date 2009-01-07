/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.lang;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;

public class ParseHandlerLink extends ParseHandlerResolver
{
    // Untested.
    // Implements (link@ LABEL) and (@name LABEL expression) to give non-trees.
    // Caveat: other things assume tree walking.
    
    static final String tagLink = "link@" ;
    static final String tagName = "@name" ;
    
    public ParseHandlerLink(Prologue prologue)                  { super(prologue) ; }
    
    String currentName = null ;
    Map<String, Item> namedItems = new HashMap<String, Item>() ;    // Item => Item
    
    // ----
    
    @Override
    public void parseFinish()
    {
        // Check links.
        super.parseFinish() ;
    }
    
    // ----
    
    @Override
    protected void declItem(ItemList list, Item item)
    {
        if ( list.getFirst().isSymbol(tagLink) )
        {
            System.err.println("Not written: "+item) ;
            super.declItem(list, item) ;
            return ;
        }
        
        if ( list.getFirst().isSymbol(tagName) )
        {
            if ( ! item.isSymbol() )
                throwException("Must be a symbol for a named item: "+item.shortString(), item) ;
            
            if ( namedItems.containsKey(item.getSymbol()) )
                throwException("Name already defined: "+item, item) ;
            currentName = item.getSymbol() ;
            // Add it anyway.  Removed in form processing.
            super.declItem(list, item) ;
            return ;
        }
        
        super.declItem(list, item) ;
    }

    @Override
    protected boolean endOfDecl(ItemList list, Item item)
    {
        // XXX No.  This does not allow for nested "@names"
        super.setFormResult(item) ;
        if ( namedItems.containsKey(currentName) )
            throwException("Name already defined: "+currentName, item) ;
        namedItems.put(currentName, item) ;
        currentName = null ;
        return super.endOfDecl(list, item) ;
    }

    @Override
    protected boolean isForm(Item tag)
    {
        if ( tag.isSymbol(tagLink) || tag.isSymbol(tagName) )
            return true ;
        return super.isForm(tag) ;
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