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
import com.hp.hpl.jena.sparql.sse.builders.BuilderBase;
import com.hp.hpl.jena.sparql.util.PrefixMapping2;

public class  ParseHandlerResolver implements ParseHandler 
{
    /* Prefix tag.
     * Form: (prefix (DECLS) TERM)
     *   where 
     *      (DECLS) is a list of pairs, each pair being PNAME and URI.
     *      TERM is one SSE expression.
     *  That is, a prefix element is exactly 3 items long. 
     *  During DECL processing, we flag that no prefix processing should be done.
     *  This is the flag of the previous Frame.
     */
    
    private static class Frame
    {
        Item listItem ;
        int state = 0 ;     // Count of location in (prefix ...) 
        Item result ;
        PrefixMapping previous ;
        
        Frame(Item listItem, PrefixMapping previous )
        {
            this.listItem = listItem ;
            this.previous = previous ;
        }
    }

    private IRIResolver         resolver   = new IRIResolver() ;
    private static final String prefixTag  = "prefix" ;
    private Stack               frames     = new Stack() ;
    private boolean             inDecl     = false ;
    private PrefixMapping       currentMap = new PrefixMappingImpl() ;
    
    public ParseHandlerResolver() {}
    
    public void listStart(Item listItem) {}

    public Item listFinish(Item listItem)
    {
        // At end of a lits, if it's a (prefix ...)
        // pop the stack and return the inner form instead. 
        if ( isCurrent(listItem) )
        {
            // End of prefix item.
            Frame f = (Frame)frames.pop() ;
            currentMap = f.previous ;
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
        
        // Spot the start of a (prefix ...)?
        if ( ! inDecl && listItem.getList().size() == 1 && elt.isWord(prefixTag) )
        {
            if ( inDecl )
                // Occurrance of (prefix ..) in DECLS handled without special processing.
                return ;
            
            // It's  (prefix ...)
            inDecl = true ;
            Frame f = new Frame(listItem, currentMap) ;
            frames.push(f) ;
            return ;
        }

        if ( ! isCurrent(listItem) )
            return ;
        
        // (prefix (DECLS) ...)
        if ( listItem.getList().size() == 2 )
        {
            PrefixMapping2 ext = new PrefixMapping2(currentMap) ;
            PrefixMapping newMappings = ext.getLocalPrefixMapping() ;
            parsePrefixes(newMappings, elt) ;
            currentMap = ext ;
            inDecl = false ;
            return ;
        }

        // (prefix (DECLS) TERM)
        if ( listItem.getList().size() == 3 )
        {
            Frame f = (Frame)frames.peek();
            f.result = elt ;
            return ;
        }
        
        BuilderBase.broken(listItem, "(prefix ...) has too many terms") ;
        
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
        Node node = resolve(lex, currentMap, item) ;
        if ( node == null )
            BuilderBase.broken(item, "Internal error") ;
        item = Item.createNode(node, item.getLine(), item.getColumn()) ;
        return item ;
    }
    
    private boolean isCurrent(Item item)
    {
        if ( frames.size() == 0 )
            return false ;
        
        Frame f = (Frame)frames.peek();

        return f.listItem == item ;
    }
    
//    // Resolve from a node (prefix name encoded) 
//    private static Node resolve(Node node, PrefixMapping pmap, ItemLocation location)
//    {
//        if ( ! node.isURI() )
//            return node ;
//        
//        String uri = node.getURI() ;
//        if ( uri.startsWith(":") )
//        {
//            String qname = uri.substring(1) ;
//            if ( pmap != null )
//                uri = pmap.expandPrefix(qname) ;
//            if ( uri == null || uri.equals(qname) )
//                BuilderBase.broken(location, "Can't resolve prefixed name: "+uri) ;
//            return Node.createURI(uri) ;
//        }
//        else
//        {
//            uri = RelURI.resolve(uri) ;
//            return Node.createURI(uri) ;
//        }
//    }

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