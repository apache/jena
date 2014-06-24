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

package com.hp.hpl.jena.sparql.sse.lang;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;

public class ParseHandlerLink extends ParseHandlerResolver
{
    // Untested.
    // Implements (link@ LABEL) and (@name LABEL expression) to give non-trees.
    // Caveat: other things assume tree walking.
    
    static final String tagLink = "link@" ;
    static final String tagName = "@name" ;
    
    public ParseHandlerLink(Prologue prologue)                  { super(prologue) ; }
    
    String currentName = null ;
    Map<String, Item> namedItems = new HashMap<>() ;    // Item => Item
    
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
