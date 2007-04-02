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
        return new Resolver(pmap).resolve(item) ;
    }
    
    public static Item resolve(Item item)
    {
        return new Resolver().resolve(item) ;
    }

    private static class Resolver
    {
        private PrefixMapping pmap ;
        Resolver(PrefixMapping pmap)    { this.pmap = pmap ; }
        Resolver()                      { this.pmap = null ; }
        
        public Item resolve(Item item)
        {
            if ( item.isList() )
                return resolve(item, item.getList()) ;
            else if ( item.isNode() ) 
                return resolve(item, item.getNode()) ;
            else if ( item.isWord() )
                return resolve(item, item.getWord()) ;
            else
                System.err.println("broken item") ;
            return null ;
        }
        
        public Item resolve(Item item, ItemList list)
        {
            if ( list.isEmpty() )
                return item ; 
                
            Item head = list.get(0) ;
            if ( ! head.isWordIgnoreCase("prefix") )
            {
                ItemList newItemList = new ItemList(list.getLine(), list.getColumn()) ; 
                for ( Iterator iter = list.iterator() ; iter.hasNext(); )
                {
                    Item sub = (Item)iter.next();
                    sub = resolve(sub) ;
                    newItemList.add(sub) ;
                }
                return Item.createList(newItemList) ;
            }
                 
            
            // It's (prefix ...)
            Builder.checkLength(3, list, "List is "+list.size()+"not 3 :: (prefix ...)") ;
            
            if ( ! list.get(1).isList() )
                Builder.broken(list, "(prefix ...) is not list of prefix/IRI pairs") ;
            
            ItemList prefixes = list.get(1).getList() ;
            Item body = list.get(2) ;

            PrefixMapping2 ext = new PrefixMapping2(pmap) ;
            PrefixMapping newMappings = ext.getLocalPrefixMapping() ;

            parsePrefixes(newMappings, prefixes) ;
            
            // Push (on the current execution frame) the existing prefix map. 
            PrefixMapping pmapPush = pmap ;
            pmap = ext ;
            Item result = resolve(body) ;
            pmap = pmapPush ;
            return result ;
        }
        
        static private void parsePrefixes(PrefixMapping newMappings, ItemList prefixes)
        {
            for ( Iterator iter = prefixes.iterator() ; iter.hasNext() ; )
            {
                Item pair = (Item)iter.next() ;
                if ( !pair.isList() || pair.getList().size() != 2 )
                    Builder.broken(pair, "Not a prefix/IRI pair") ;
                Item prefixItem = pair.getList().get(0) ;
                Item iriItem = pair.getList().get(1) ;

                Node n = prefixItem.getNode() ;
                if ( ! n.isURI() )
                    Builder.broken(pair, "Prefix part is not a prefixed name: "+pair) ;
                
                String prefix = n.getURI();
                Node iriNode = iriItem.getNode() ;
                
                if ( iriNode == null || ! iriNode.isURI() )
                    Builder.broken(pair, "Not an IRI: "+iriItem) ;
                
                String iri = iriNode.getURI();
                // It will look like :x:
                prefix = prefix.substring(1) ;
                prefix = prefix.substring(0, prefix.length()-1) ;
                
                newMappings.setNsPrefix(prefix, iri) ;
            }
        }

        public Item resolve(Item item, String word)
        { return item ; }
        
        public Item resolve(Item item, Node node)
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
                    Builder.broken(item, "Can't resolve "+qname) ;
                return Item.createNode(Node.createURI(uri)) ;
            }
            else
            {
                uri = RelURI.resolve(uri) ;
                return Item.createNode(Node.createURI(uri)) ;
            }
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