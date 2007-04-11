/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang.sse.builders;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.n3.RelURI;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.lang.sse.*;
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

            Node n = prefixItem.getNode() ;
            if ( ! n.isURI() )
                BuilderBase.broken(pair, "Prefix part is not a prefixed name: "+pair) ;

            String prefix = n.getURI();
            Node iriNode = iriItem.getNode() ;

            if ( iriNode == null || ! iriNode.isURI() )
                BuilderBase.broken(pair, "Not an IRI: "+iriItem) ;

            String iri = iriNode.getURI();
            // It will look like :x:
            prefix = prefix.substring(1) ;
            prefix = prefix.substring(0, prefix.length()-1) ;

            newMappings.setNsPrefix(prefix, iri) ;
        }
    }

    private static Item process(Item item, String word, PrefixMapping pmap)
    { return item ; }

    private static Item process(Item item, Node node, PrefixMapping pmap)
    {
        if ( ! node.isURI() )
            return item ;
        String uri = node.getURI() ;
        if ( uri.startsWith(":") )
        {
            String qname = uri.substring(1) ;
            if ( pmap != null )
                uri = pmap.expandPrefix(qname) ;
            if ( uri == null || uri.equals(qname) )
                BuilderBase.broken(item, "Can't resolve "+qname) ;
            return Item.createNode(Node.createURI(uri)) ;
        }
        else
        {
            uri = RelURI.resolve(uri) ;
            return Item.createNode(Node.createURI(uri)) ;
        }
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