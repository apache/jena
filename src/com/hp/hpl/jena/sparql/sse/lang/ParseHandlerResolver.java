/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.lang;

import java.util.Iterator;
import java.util.Stack;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.builders.BuilderPrefixMapping;
import com.hp.hpl.jena.sparql.util.StringUtils;


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
 *  unless a subclass (carefully) manages that. 
 *    
 * @author Andy Seaborne
 */

public class ParseHandlerResolver extends ParseHandlerForm
{
    private static final String prefixTag       = "prefix" ;
    private static final String baseTag         = "base" ;
    private PrefixMapping       topMap          = null ;
    private String              topBase         = null ;
    private Prologue            prologue        = null ; 
    private ItemList            declList        = null ;
    private Stack<Prologue>               state           = new Stack<Prologue>() ; // Previous prologues (not the current one)
    
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
        
        if ( ! StringUtils.contains(pname, ":") )
            throwException("Prefixed name does not have a ':': "+pname, line, column) ;
        
        String uri =  prologue.getPrefixMapping().expandPrefix(pname) ;
        if ( uri == null || uri.equals(pname) )
            throwException("Can't resolve prefixed name: "+pname, line, column) ;
        return uri ;
    }
    
    private String resolveIRI(String iriStr, int line, int column) 
    {
        if ( prologue.getResolver() != null )
            return prologue.getResolver().resolve(iriStr) ;
        return iriStr ;
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