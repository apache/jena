/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;

import java.util.Iterator;
import java.util.Stack;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.sse.builders.BuilderBase;
import com.hp.hpl.jena.sparql.util.PrefixMapping2;

public class  ParseHandlerResolver implements ParseHandler 
{
    // TODO: (base ...)
    // TODO: returning/setting the top PrefixMapping. 
    
    /* Prefix tag.
     * Form: (prefix (DECLS) TERM)
     *   where 
     *      (DECLS) is a list of pairs, each pair being PNAME and URI.
     *      TERM is one SSE expression.
     *  That is, a prefix element is exactly 3 items long. 
     *  During DECL processing, we flag that no prefix processing should be done.
     *  This is the flag of the previous Frame.
     */
    
    private static final String prefixTag  = "prefix" ;
    private static final String baseTag    = "base" ;

    private boolean             inDecl     = false ;
    private PrefixMapping       prefixMap ;
    private IRIResolver         resolver ;
    private FrameStack          frameStack = new FrameStack() ;
    
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
    
    public void listStart(Item listItem) {}

    public Item listFinish(Item listItem)
    {
        // At end of a lits, if it's a (prefix ...) or (base ...)
        // pop the stack and return the inner form instead. 
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
        // Always add to the listItem: keeps the tracking of the (prefix ..) terms easier.
        listItem.getList().add(elt) ;
        
        // Spot the start of a (prefix ...) or (base ...)
        if ( ! inDecl && listItem.getList().size() == 1 )
        {
            if ( elt.isWord(prefixTag) || elt.isWord(baseTag))
            {
                // It's  (prefix ...) or (base...), not inside the delaration of an outer prefix/base
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
        if ( listItem.getList().size() == 2 )
        {
            if ( listItem.isTagged(baseTag) )
            {
                if ( !elt.isNode() )
                    BuilderBase.broken(elt, "(base  ...): not a URI for the base.") ;
                Node n = elt.getNode() ; 
                if ( ! n.isURI() )
                    BuilderBase.broken(elt, "(base  ...): not a URI for the base.") ;
                resolver = new IRIResolver(n.getURI()) ;
            }
            else
            {
                PrefixMapping2 ext = new PrefixMapping2(prefixMap) ;
                PrefixMapping newMappings = ext.getLocalPrefixMapping() ;
                parsePrefixes(newMappings, elt) ;
                prefixMap = ext ;
            }
            inDecl = false ;
            return ;
        }

        // (prefix (DECLS) TERM)
        // (base <IRI> TERM)
        if ( listItem.getList().size() == 3 )
        {
            Frame f = frameStack.getCurrent() ;
            f.result = elt ;
            return ;
        }
        
        BuilderBase.broken(listItem, "("+listItem.getWord()+" ...) has too many terms") ;
    }
    
    public Item itemWord(Item item)     { return item ; }
    
    public Item itemNode(Item item)
    {
        Node n = item.getNode() ; 
        if ( n.isURI() )
        {
            String x = n.getURI() ;
            x = resolver.resolve(x) ;
            n = Node.createURI(x) ;
            return Item.createNode(n, item.getLine(), item.getColumn()) ;
        }
        return item ;
    }

    public Item itemPName(Item item)
    { 
        if ( inDecl ) return item ;

        String lex = item.getWord() ;
        Node node = resolve(lex, prefixMap, item) ;
        if ( node == null )
            BuilderBase.broken(item, "Internal error") ;
        item = Item.createNode(node, item.getLine(), item.getColumn()) ;
        return item ;
    }
    
    // ----------------
    
    // Returns a word if resolved - else null.
    private static Node resolve(String word, PrefixMapping pmap, ItemLocation location)
    {
        if ( pmap == null )
            return null ;
        
        if ( ! word.contains(":") )
            return null ;
        
        String uri = pmap.expandPrefix(word) ;
        if ( uri == null || uri.equals(word) )
        {
            // Unresolved in some way.  
            BuilderBase.broken(location, "Can't resolve prefixed name: "+uri) ;
            // OR Make into a funny node
            return Node.createURI(":"+word) ;
        }
        return Node.createURI(uri) ;
    }
    
    private static void parsePrefixes(PrefixMapping newMappings, Item elt)
    {
        if ( ! elt.isList() )
            BuilderBase.broken(elt, "Prefixes must be a list: "+elt) ;
        
        ItemList prefixes = elt.getList() ; 
        for ( Iterator iter = prefixes.iterator() ; iter.hasNext() ; )
        {
            Item pair = (Item)iter.next() ;
            if ( !pair.isList() || pair.getList().size() != 2 )
                BuilderBase.broken(pair, "Not a prefix/IRI pair") ;
            Item prefixItem = pair.getList().get(0) ;
            Item iriItem = pair.getList().get(1) ;

            // Maybe a Node (fake prefixed name) or a Word, depending on parser set up.
            
            String prefix = null ;

            // -- Prefix as word
            if ( prefixItem.isWord() )
                prefix = prefixItem.getWord() ;

            // -- Prefix as Node
//            if ( prefixItem.isNode())
//            {
//                Node n = prefixItem.getNode() ;
//                if ( ! n.isURI() )
//                    BuilderBase.broken(pair, "Prefix part is not a prefixed name: "+pair) ;
//
//                prefix = n.getURI();
//                // It will look like :x:
//                
//                if ( ! prefix.startsWith(":") )
//                    BuilderBase.broken(pair, "Prefix part is not a prefix name: "+pair) ;
//                prefix = prefix.substring(1) ;
//            }            

            if ( prefix == null )
                BuilderBase.broken(pair, "Prefix part nor recognized: "+prefixItem) ;
            
            if ( ! prefix.endsWith(":") )
                BuilderBase.broken(pair, "Prefix part does not end with a ':': "+pair) ;
            prefix = prefix.substring(0, prefix.length()-1) ;
            if ( prefix.contains(":") )
                BuilderBase.broken(pair, "Prefix itseld contains a ':' : "+pair) ;
            // -- /Prefix
            
            Node iriNode = iriItem.getNode() ;

            if ( iriNode == null || ! iriNode.isURI() )
                BuilderBase.broken(pair, "Not an IRI: "+iriItem) ;

            String iri = iriNode.getURI();

            newMappings.setNsPrefix(prefix, iri) ;
        }
    }

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