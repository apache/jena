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

package com.hp.hpl.jena.sdb.layout1;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;


public class CodecSimple implements EncoderDecoder
{
    private PrefixMapping prefixMapping ;
    
    public CodecSimple() { prefixMapping = new PrefixMappingImpl() ; }
    
    public CodecSimple(PrefixMapping pMap) { prefixMapping = pMap ; }
    
    // Does not need to make the string SQL-safe
    @Override
    public String encode(Node node)
    {
        if ( node.isBlank() )
            return "_:"+node.getBlankNodeId().getLabelString() ;
        String s = FmtUtils.stringForNode(node, prefixMapping) ;
        return s ; 
    }
    
    @Override
    public Node decode(String s)
    {
        if ( s.startsWith("Double"))
            System.err.println(s) ;
        
        if ( s.startsWith("_:") )
            return NodeFactory.createAnon(new AnonId(s.substring(2))) ;
        return stringToNode(s, prefixMapping) ; 
    }
    
    // ParserUtils??
    // -> expr
    // -> GraphTerm
    // -> ??
    static Node stringToNode(String string, PrefixMapping pmap)
    {
        return SSE.parseNode(string, pmap) ;
    }
}
