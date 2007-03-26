/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sse.builders;

import sse.Item;
import sse.ItemTransformBase;
import sse.ItemTransformer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.n3.RelURI;
import com.hp.hpl.jena.shared.PrefixMapping;

/** Resolve URIs, expand any prefixed names */

public class ResolveURI
{
    // TODO Syntax for prefixes: c.f. Lisp "let"
    /*
     * (prefix LIST_PAIRS BODY ...)
     * 
     * (prefix  ((x: <http://example)
     *           (y: <http://example))
     *      body
     *  ) => body with expansions
     */
    
    
    
    public static Item resolve(Item item, PrefixMapping pmap)
    {
        return ItemTransformer.transform(new Resolver(pmap), item) ;
    }
    
    private static class Resolver extends ItemTransformBase
    {
        private PrefixMapping pmap ;
        Resolver(PrefixMapping pmap)
        { this.pmap = pmap ; }
        
//        @Override
//        public Item transform(Item item, ItemList list)
//        { }
        
//        @Override
//        public Item transform(Item item, String word)
//        { }
        
        //@Override
        public Item transform(Item item, Node node)
        {
            if ( node.isURI() )
            {
                String uri = node.getURI() ;
                if ( uri.startsWith(":") )
                {
                    String qname = uri.substring(1) ;
                    uri = pmap.expandPrefix(qname) ;
                    if ( uri == null )
                        throw new RuntimeException("Can't resolve "+qname) ;
                    return Item.createNode(Node.createURI(uri)) ;
                }
                else
                {
                    uri = RelURI.resolve(uri) ;
                    return Item.createNode(Node.createURI(uri)) ;
                }
            }
            return super.transform(item, node) ;
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