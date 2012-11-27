/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.sse.builders;


import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;

/** Build a prefixmapping, tagged (prefixmap pairs) or (prefixmapping pairs)
 * each pair being a PrefixName, but must end : and an IRI.
 * 
 * Can also just a list of pairs. */

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
            if ( StrUtils.contains(prefix, ":") )
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
