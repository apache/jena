/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.Stack;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.sse.builders.BuilderBase;
import com.hp.hpl.jena.sparql.sse.builders.BuilderPrefixMapping;
import com.hp.hpl.jena.sparql.util.PrefixMapping2;

/** Resolve prefixed names in a prefix map and IRIs relative to a base.
 *  Forms: 
 *    (prefix (DECL) TERM) => TERM with prefix names expanded
 *    (base IRI TERM) => TERM with IRIs resolved to absolute IRIs
 * 
 *    
 * @author Andy Seaborne
 * @version $Id$
 */

public class ParseHandlerResolver implements ParseHandler 
{
    /*  Both forms have a common structure.
     *    Exactly 3 items long.
     *    There is a stack of previous settings (i.e. things always nest) 
     *  During DECL, for prefix, (the 2nd term) processing, we flag that no prefix processing should be done.
     *  Base can include prefixes, and relative URIs.
     *  
     *  The first base and first prefix mapping are remembered.
     *  Better: look at the first thing ever seen. 
     */
    
    // Maybe more restrictive.  Spot the special form (base BASE (prefix ....))
    
    private static final String prefixTag  = "prefix" ;
    private static final String baseTag    = "base" ;

    private PrefixMapping       topMap     = null ;
    private String              topBase    = null ;

    private boolean             inDecl     = false ;
    private PrefixMapping       prefixMap ;
    private IRIResolver         resolver ;
    private FrameStack          frameStack = new FrameStack() ;
    private int                 depth      = 0 ;
    
    public ParseHandlerResolver() { this(null, null) ; }

    public ParseHandlerResolver(PrefixMapping pmap) { this(pmap, null) ; }
    
    public ParseHandlerResolver(PrefixMapping pmap, String base)
    { 
        if ( pmap == null )
            pmap = new PrefixMappingImpl() ;
        prefixMap = pmap ;
        resolver = new IRIResolver(base) ;
    }
    
    public ParseHandlerResolver(Prologue prologue)
    {
        prefixMap = prologue.getPrefixMapping() ;
        resolver = prologue.getResolver() ;
    }

    public void parseStart()
    {}

    public void parseFinish()
    {
        if ( depth != 0 )
            LogFactory.getLog(ParseHandlerResolver.class).warn("Stack error: depth ="+depth+" at end of parse run") ;
    }
    
    public String resolvePName(String pname)
    { 
        if ( prefixMap == null )
            return null ;
        
        if ( ! pname.contains(":") )
            return null ;
        
        String uri = prefixMap.expandPrefix(pname) ;
        if ( uri == null || uri.equals(pname) )
            return null ;
        uri = resolver.resolve(uri) ;
        return uri ;
    }
    
    public String resolveIRI(String iri)
    {
        return resolver.resolve(iri) ;
    }
    
    public void listStart(Item listItem) { depth++ ; }

    public Item listFinish(Item listItem)
    {
        --depth ;
        // At end of a list,
        // If it's the current stack front, i.e. (prefix ...) or (base ...)
        //   pop the stack and return the inner form instead. 
        if ( frameStack.isCurrent(listItem) )
        {
            // End of prefix item.  Pop state.
            Frame f = frameStack.pop() ;
            prefixMap = f.prefixMap ;
            resolver = f.resolver ;
            Item result = f.result ;
            if ( result == null )
                result = Item.createNil(listItem.line, listItem.column) ;
            return result ;
        }

        return listItem ;
    }
    
    public void listAdd(Item listItem, Item elt)
    {
        ItemList list = listItem.getList() ;
        // Always add to the listItem: keeps the tracking of the (prefix ..) terms easier.
        list.add(elt) ;
        
        // Spot the start of a (prefix ...) or (base ...)
        if ( ! inDecl && list.size() == 1 )
        {
            if ( elt.isWordIgnoreCase(prefixTag) || elt.isWordIgnoreCase(baseTag))
            {
                // It's  (prefix ...) or (base...), not inside the declaration of an outer prefix/base
                // For base, leave processing of qnames on.
                if ( elt.isWordIgnoreCase(prefixTag) )
                    inDecl = true ;
                Frame f = new Frame(listItem, prefixMap, resolver) ;
                frameStack.push(f) ;
                return ;
            }
        }
        
        // If not a (prefix ...) or (base ...) , nothing to do, already added element.
        if ( ! frameStack.isCurrent(listItem) )
            return ;
        
        // (prefix (DECLS) ...)
        // (base <IRI> ...)
        if ( list.size() == 2 )
        {
            if ( listItem.isTaggedIgnoreCase(baseTag) )
            {
                if ( !elt.isNode() )
                    BuilderBase.broken(elt, "(base  ...): not a IRI for the base.") ;
                Node n = elt.getNode() ; 
                if ( ! n.isURI() )
                    BuilderBase.broken(elt, "(base  ...): Node not a IRI for the base.") ;
                resolver = new IRIResolver(n.getURI()) ;
                // Remember the first base seen
                if ( topBase == null )
                    topBase = n.getURI() ;
            }
            else
            {
                PrefixMapping newMappings = BuilderPrefixMapping.build(elt) ; 
                PrefixMapping2 ext = new PrefixMapping2(prefixMap, newMappings) ;
                
                prefixMap = ext ;
                // Remember first prefix mapping seen. 
                if( topMap == null )
                    topMap = newMappings ;
            }
            inDecl = false ;
            return ;
        }

        // ??
        // (prefix (DECLS) TERM)
        // (base <IRI> TERM)
        if ( list.size() == 3 )
        {
            Frame f = frameStack.getCurrent() ;
            f.result = elt ;
            return ;
        }
        
        BuilderBase.broken(listItem, BuilderBase.shortPrint(listItem)+" has too many terms ("+list.size()+")") ;
    }
    
    public Item itemWord(Item item)     { return item ; }
    
    public Item itemNode(Item item)     { return item ; } 
//    {
//        Node n = item.getNode() ; 
//        if ( n.isURI() )
//        {
//            String x = n.getURI() ;
//            x = resolver.resolve(x) ;
//            n = Node.createURI(x) ;
//            return Item.createNode(n, item.getLine(), item.getColumn()) ;
//        }
//        return item ;
//    }

    // ----------------
    
    private static class Frame
    {
        Item listItem ;
        Item result ;
        PrefixMapping prefixMap;
        IRIResolver resolver ;
        
        Frame(Item listItem, PrefixMapping pmap, IRIResolver resolver)
        {
            this.listItem = listItem ;
            this.prefixMap = pmap ;
            this.resolver = resolver ;
        }
    }

    // ----------------
    
    private static class FrameStack
    {
        private Stack frames    = new Stack() ;
    
        boolean isCurrent(Item item)
        {
            if ( frames.size() == 0 )
                return false ;
    
            Frame f = (Frame)frames.peek();
    
            return f.listItem == item ;
        }
    
        Frame getCurrent()
        {
            if ( frames.size() == 0 )
                return null ;
            return (Frame)frames.peek() ;
        }
    
        void push(Frame f) { frames.push(f) ; }
        Frame pop() { return (Frame)frames.pop() ; }
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