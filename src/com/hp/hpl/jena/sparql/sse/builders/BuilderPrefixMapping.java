/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.StringUtils;

/** Build a prefixmapping, tagged (prefixmap pairs) or (prefixmapping pairs)
 * each pair being a PrefixName, but must end : and an IRI.
 * 
 * Can also just a list of pairs.
 * 
 * @author Andy Seaborne
 */

public class BuilderPrefixMapping
{
    public static PrefixMapping build(Item elt)
    {
        PrefixMapping pmap = new PrefixMappingImpl() ;
        build(pmap, elt) ;
        return pmap ;
    }

    public static void build(PrefixMapping newMappings, Item elt)
    {
        if ( ! elt.isList() )
            BuilderLib.broken(elt, "Prefix mapping requires a list of pairs", elt) ;

        ItemList prefixes = elt.getList() ;
        
        // Strip (prefixmapping  ...)
        if ( elt.isTaggedIgnoreCase(Tags.tagPrefixMap) || elt.isTaggedIgnoreCase(Tags.tagPrefixMapping) )
        {
            BuilderLib.checkLength(2, elt.getList(), "Not of length 2"+elt.shortString()) ;
            // drop the tag
            prefixes = prefixes.cdr();
        }
        
        for (Item pair : prefixes)
        {
            if ( !pair.isList() || pair.getList().size() != 2 )
                BuilderLib.broken(pair, "Not a prefix/IRI pair") ;
            Item prefixItem = pair.getList().get(0) ;
            Item iriItem = pair.getList().get(1) ;

            // Maybe a Node (fake prefixed name) or a Symbol, depending on parser set up.
            
            String prefix = null ;

            // -- Prefix as symbol
            if ( prefixItem.isSymbol() )
                prefix = prefixItem.getSymbol() ;

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
                BuilderLib.broken(pair, "Prefix part not recognized: "+prefixItem) ;
            
            if ( ! prefix.endsWith(":") )
                BuilderLib.broken(pair, "Prefix part does not end with a ':': "+pair) ;
            prefix = prefix.substring(0, prefix.length()-1) ;
            if ( StringUtils.contains(prefix, ":") )
                BuilderLib.broken(pair, "Prefix itself contains a ':' : "+pair) ;
            // -- /Prefix
            
            Node iriNode = iriItem.getNode() ;

            if ( iriNode == null || ! iriNode.isURI() )
                BuilderLib.broken(pair, "Not an IRI: "+iriItem) ;

            String iri = iriNode.getURI();

            newMappings.setNsPrefix(prefix, iri) ;
        }
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