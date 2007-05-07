/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.n3.RelURI;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.sse.*;
import com.hp.hpl.jena.sparql.util.PrefixMapping2;

/** Resolve URIs, expand any prefixed names */

public class ResolvePrefixedNames
{
    public static String tagPrefix = "prefix" ;

    // Need process(Item, ??) => Item, Pmap?
    
    /*
     * (prefix LIST_PAIRS BODY ...)
     * 
     * (prefix  ((x: <http://example>)
     *           (y: <http://example>))
     *      body
     *  ) => body with expansions
     */

    public static Item resolve(Item item, PrefixMapping pmap)
    {
        return process(item, pmap) ;
    }

    public static Item resolve(Item item)
    {
        return process(item, null) ;
    }

    public static Node resolve(Node node, PrefixMapping pmap)
    { return resolve(node, pmap) ; }
    
    public static Node resolve(Node node, PrefixMapping pmap, ItemLocation location)
    {
        if ( ! node.isURI() )
            return node ;
        
        String uri = node.getURI() ;
        if ( uri.startsWith(":") )
        {
            String qname = uri.substring(1) ;
            if ( pmap != null )
                uri = pmap.expandPrefix(qname) ;
            if ( uri == null || uri.equals(qname) )
                BuilderBase.broken(location, "Can't resolve prefixed name: "+uri) ;
            return Node.createURI(uri) ;
        }
        else
        {
            uri = RelURI.resolve(uri) ;
            return Node.createURI(uri) ;
        }
    }

    // Returns a node if resolved - else null.
    public static Node resolve(String word, PrefixMapping pmap, ItemLocation location)
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

    private static Item process(Item item, PrefixMapping pmap)
    {
        if ( item.isList() )
            return process(item, item.getList(), pmap) ;
        else if ( item.isNode() ) 
            return process(item, item.getNode(), pmap) ;
        else if ( item.isWord() )
            return process(item, item.getWord(), pmap) ;
        else
            System.err.println("broken item") ;
        return null ;
    }

    private static Item process(Item item, ItemList list, PrefixMapping pmap)
    {
        if ( list.isEmpty() )
            return item ; 

        Item head = list.get(0) ;
        if ( ! head.isWordIgnoreCase(tagPrefix) )
        {
            ItemList newItemList = new ItemList(list.getLine(), list.getColumn()) ; 
            for ( Iterator iter = list.iterator() ; iter.hasNext(); )
            {
                Item sub = (Item)iter.next();
                sub = process(sub, pmap) ;
                newItemList.add(sub) ;
            }
            return Item.createList(newItemList) ;
        }

        BuilderBase.checkTagged(item, 3, tagPrefix, "Expected (prefix PREFIXES BODY)") ;
        if ( ! list.get(1).isList() )
            BuilderBase.broken(list, "("+tagPrefix+" ...) is not list of prefix/IRI pairs") ;

        ItemList prefixes = list.get(1).getList() ;
        Item body = list.get(2) ;

        PrefixMapping2 ext = new PrefixMapping2(pmap) ;
        PrefixMapping newMappings = ext.getLocalPrefixMapping() ;

        parsePrefixes(newMappings, prefixes) ;

        // Push (on the current execution frame) the existing prefix map.
        PrefixMapping pmapPush = pmap ;
        Item result = process(body, ext) ;
        pmap = pmapPush ;
        return result ;
    }

    private static void parsePrefixes(PrefixMapping newMappings, ItemList prefixes)
    {
        for ( Iterator iter = prefixes.iterator() ; iter.hasNext() ; )
        {
            Item pair = (Item)iter.next() ;
            if ( !pair.isList() || pair.getList().size() != 2 )
                BuilderBase.broken(pair, "Not a prefix/IRI pair") ;
            Item prefixItem = pair.getList().get(0) ;
            Item iriItem = pair.getList().get(1) ;

            // Maybe a Node (fake prefixed name) or a Word, depending on parser set up.
            
            String prefix = null ;

            // -- Prefix
            if ( prefixItem.isWord() )
                prefix = prefixItem.getWord() ;

            if ( prefixItem.isNode())
            {
                Node n = prefixItem.getNode() ;
                if ( ! n.isURI() )
                    BuilderBase.broken(pair, "Prefix part is not a prefixed name: "+pair) ;

                prefix = n.getURI();
                // It will look like :x:
                
                if ( ! prefix.startsWith(":") )
                    BuilderBase.broken(pair, "Prefix part is not a prefix name: "+pair) ;
                prefix = prefix.substring(1) ;
            }            

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

    private static Item process(Item item, String word, PrefixMapping pmap)
    {
        Node n = resolve(word, pmap, item) ;
        if ( n == null )
            return item ;
        return Item.createNode(n) ; 
    }

    private static Item process(Item item, Node node, PrefixMapping pmap)
    {
        if ( ! node.isURI() )
            return item ;
        
        Node node2 = resolve(node, pmap, item) ;
        if ( node2 == null )
            BuilderBase.broken(item, "Can't resolve "+node.getURI()) ;
        return Item.createNode(node2) ;
    }
}


/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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