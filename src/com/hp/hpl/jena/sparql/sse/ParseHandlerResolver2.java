/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.Stack;

import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.core.Prologue;


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
 * @version $Id$
 */

public class ParseHandlerResolver2 extends ParseHandlerForm
{
    private static final String prefixTag       = "prefix" ;
    private static final String baseTag         = "base" ;
    private PrefixMapping       topMap          = null ;
    private String              topBase         = null ;
    private PrefixMapping       prefixMap ;
    private IRIResolver         resolver ;
    private Stack               state           = new Stack() ; // Previous proglogues (not the current one)
    
    public ParseHandlerResolver2() { this(null, null) ; }

    public ParseHandlerResolver2(PrefixMapping pmap) { this(pmap, null) ; }
    
    public ParseHandlerResolver2(PrefixMapping pmap, String base)
    { 
        if ( pmap == null )
            pmap = new PrefixMappingImpl() ;
        prefixMap = pmap ;
        resolver = new IRIResolver(base) ;
    }
    
    public ParseHandlerResolver2(Prologue prologue)
    {
        prefixMap = prologue.getPrefixMapping() ;
        resolver = prologue.getResolver() ;
    }

    
    protected void declItem(ItemList list, Item item)
    {
        if ( list.size() > 2 )
            throwException("(base ...): To many items in form", item) ;

        // Base.
        if ( ! item.isNode() )
            throwException("(base ...): not an RDF node for the base.", item) ;
        if ( ! item.getNode().isURI() )
            throwException("(base ...): not an IRI for the base.", item) ;
        
        // Push Prologue.
        
        // Prefix.
        
    }
    
    protected boolean endOfDecl(ItemList list, Item item)
    { 
        if ( list.size() == 2 )
            return true ;
        return false ;
    }

    protected boolean isForm(Item tag)
    {
        return tag.isSymbol(baseTag) ;
    }
    
    public void emitIRI(int line, int column, String iriStr)
    { 
        // resolve as normal. No special action.
        //if ( inPrefixDecl ) {}
        iriStr = resolveIRI(iriStr, line, column) ;
        super.emitIRI(line, column, iriStr) ;
    }
    
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

    protected String resolvePrefixedName(String pname, int line, int column)
    { 
        if ( prefixMap == null )
            throwException("No prefix mapping for prefixed name: "+pname, line, column) ;
        
        if ( ! pname.contains(":") )
            throwException("Prefixed name does not have a ':': "+pname, line, column) ;
        
        String uri = prefixMap.expandPrefix(pname) ;
        if ( uri == null || uri.equals(pname) )
            throwException("Can't resolve prefixed name: "+pname, line, column) ;
        return uri ;
    }
    
    private String resolveIRI(String iriStr, int line, int column) 
    {
        if ( resolver != null )
            return resolver.resolve(iriStr) ;
        return iriStr ;
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