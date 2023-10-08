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

package org.apache.jena.tdb1.store.nodetable;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.out.NodeFmtLib ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.SSE_ParseException ;
import org.apache.jena.tdb1.lib.NodeLib;

/** Utilities for encoding/decoding nodes as strings.
 * Normally use a Nodec (byte buffers) instead.
 */

public class NodecLib
{
    // STATUS UNCLEAR.
    // privateize the operations until checked.

    // Better sharing with NodecSSE

    // Characters in IRIs that are illegal and cause SSE problems, but we wish to keep.
    final private static char MarkerChar = '_' ;
    final private static char[] invalidIRIChars = { MarkerChar , ' ' } ;

    private /*public*/ static String encode(Node node) { return encode(node, null) ; }

    private /*public*/ static String encode(Node node, PrefixMapping pmap)
    {
        if ( node.isBlank() )
            // Raw label.
            return "_:"+node.getBlankNodeLabel() ;
        if ( node.isURI() )
        {
            // Pesky spaces
            //throw new TDBException("Space found in URI: "+node) ;
            String x = StrUtils.encodeHex(node.getURI(), '_', invalidIRIChars) ;
            if ( x != node.getURI() )
                node = NodeFactory.createURI(x) ;
        }

        return NodeFmtLib.strNT(node) ;
    }

    private /*public*/ static Node decode(String s)     { return decode(s, null) ; }

    private /*public*/ static Node decode(String s, PrefixMapping pmap)
    {
        if ( s.startsWith("_:") )
        {
            s = s.substring(2) ;
            return NodeFactory.createBlankNode(s) ;
        }

        if ( s.startsWith("<") )
        {
            s = s.substring(1,s.length()-1) ;
            s = StrUtils.decodeHex(s, MarkerChar) ;
            return NodeFactory.createURI(s) ;
        }

        try {
            // SSE invocation is expensive (??).
            // Try TokenizerText?
            // Use for literals only.
            Node n = SSE.parseNode(s, pmap) ;
            return n ;
        } catch (SSE_ParseException ex)
        {
            Log.error(NodeLib.class, "decode: Failed to parse: "+s) ;
            throw ex ;
        }
    }

}
