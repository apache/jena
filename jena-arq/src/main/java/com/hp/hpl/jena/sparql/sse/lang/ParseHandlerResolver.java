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

import java.util.ArrayDeque ;
import java.util.Deque ;
import java.util.Iterator ;

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderPrefixMapping ;

/** Resolve syntacic forms like (base ...) and (prefix...)
 *  where the syntax modifies the enclosed sub term.
 *  
 *  
 *  Forms:
 *    (FORM DECL... TERM) => where TERM is the result.
 *  Examples 
 *    (prefix (PREFIXES) TERM) => TERM with prefix names expanded
 *    (base IRI TERM) => TERM with IRIs resolved to absolute IRIs
 *  
 *  The DECL part can not itself have nested, independent forms
 *  unless a subclass (carefully) manages that. */

public class ParseHandlerResolver extends ParseHandlerForm
{
    private static final String prefixTag       = "prefix" ;
    private static final String baseTag         = "base" ;
    private PrefixMapping       topMap          = null ;
    private String              topBase         = null ;
    private Prologue            prologue        = null ; 
    private ItemList            declList        = null ;
    private Deque<Prologue>     state           = new ArrayDeque<>() ; // Previous prologues (not the current one)
    
    public ParseHandlerResolver(Prologue p)
    {
        prologue = p ;
    }
    
    @Override
    protected void declItem(ItemList list, Item item)
    {
        if ( list != declList )
            // Deeper
            return ;
        
        // Prefix - deeper than one.
        boolean isBase = list.get(0).isSymbol(baseTag) ; 
        boolean isPrefix = list.get(0).isSymbol(prefixTag) ;
        
        // Old state has already been saved.
        if ( isBase )
        {        
            if ( ! item.isNode() )
                throwException("(base ...): not an RDF node for the base.", item) ;
            if ( ! item.getNode().isURI() )
                throwException("(base ...): not an IRI for the base.", item) ;
    
            String baseIRI = item.getNode().getURI() ;
            prologue = prologue.sub(baseIRI) ;
            // Remeber first base seen 
            if ( topBase == null )
                topBase = baseIRI ;
            return ;
        }
        
        if ( isPrefix )
        {
            PrefixMapping newMappings = BuilderPrefixMapping.build(item) ; 
            prologue = prologue.sub(newMappings) ;
            // Remember first prefix mapping seen. 
            if( topMap == null )
                topMap = newMappings ;
            return ;
        }
        throwException("Inconsistent: "+list.shortString(), list) ;
    }
    
    @Override
    protected boolean endOfDecl(ItemList list, Item item)
    { 
        // Both (base...) and (prefix...) have one decl item 
        if ( declList == list && list.size() == 2 )
        {
            declList = null ;
            return true ;
        }
        return false ;
    }

    @Override
    protected boolean isForm(Item tag)
    {
        return tag.isSymbol(baseTag) || tag.isSymbol(prefixTag) ;
    }
    
    @Override
    protected void startForm(ItemList list)
    {
        // Remember the top of declaration
        declList = list ;
        state.push(prologue) ;
    }

    private void dump()
    {
        Iterator<Prologue> iter = state.iterator() ;
        for ( ; iter.hasNext() ; )
        {
            Prologue p = iter.next() ;
            System.out.println("  Prologue: "+p.getBaseURI()) ;
        }
    }
    
    @Override
    protected void finishForm(ItemList list)
    { 
        // Check list length
        prologue = state.pop() ;
        // Restore state 
        
        // Choose the result.
        if ( list.size() > 2 )
        {
            Item item = list.getLast() ;
            super.setFormResult(item) ;
        }
    }

    @Override
    public void emitIRI(int line, int column, String iriStr)
    { 
        iriStr = resolveIRI(iriStr, line, column) ;
        super.emitIRI(line, column, iriStr) ;
    }
    
    @Override
    public void emitPName(int line, int column, String pname)
    {
        if ( inFormDecl() )
        {
            // Record a faked PName.  Works with BuilderPrefixMapping
            Item item = Item.createSymbol(pname, line, column) ;
            listAdd(item) ;
            return ;
        }
        String iriStr = resolvePrefixedName(pname, line, column) ;
        super.emitIRI(line, column, iriStr) ;
    }

    @Override
    protected String resolvePrefixedName(String pname, int line, int column)
    { 
        if ( prologue.getPrefixMapping() == null )
            throwException("No prefix mapping for prefixed name: "+pname, line, column) ;
        
        if ( ! StrUtils.contains(pname, ":") )
            throwException("Prefixed name does not have a ':': "+pname, line, column) ;
        
        String uri =  prologue.expandPrefixedName(pname) ;
        if ( uri == null )
            throwException("Can't resolve prefixed name: "+pname, line, column) ;
        return uri ;
    }
    
    private String resolveIRI(String iriStr, int line, int column) 
    {
        if ( prologue.getResolver() != null )
            return prologue.getResolver().resolveToStringSilent(iriStr) ;
        return iriStr ;
    }
 
}
