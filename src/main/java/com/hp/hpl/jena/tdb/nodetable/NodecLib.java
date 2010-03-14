/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.tdb.lib.NodeFmtLib ;
import com.hp.hpl.jena.tdb.lib.NodeLib ;

/** Utilities for encoding/decoding nodes as strings.
 * Normally use a Nodec (byte buffers) instead.
 */

public class NodecLib
{
    // Better sharing with NodecSSE
    
    // Characters in IRIs that are illegal and cause SSE problems, but we wish to keep.
    final private static char MarkerChar = '_' ;
    final private static char[] invalidIRIChars = { MarkerChar , ' ' } ; 
    
    public static String encode(Node node) { return encode(node, null) ; }

    public static String encode(Node node, PrefixMapping pmap)
    {
        if ( node.isBlank() )
            // Raw label.
            return "_:"+node.getBlankNodeLabel() ;
        if ( node.isURI() ) 
        {
            // Pesky spaces
            //throw new TDBException("Space found in URI: "+node) ;
            String x = StrUtils.encode(node.getURI(), '_', invalidIRIChars) ;
            if ( x != node.getURI() )
                node = Node.createURI(x) ; 
        }
        
        return NodeFmtLib.serialize(node) ;
    }

    public static Node decode(String s)     { return decode(s, null) ; }
    
    public static Node decode(String s, PrefixMapping pmap)
    {
        if ( s.startsWith("_:") )   
        {
            s = s.substring(2) ;
            return Node.createAnon(new AnonId(s)) ;
        }

        if ( s.startsWith("<") )
        {
            s = s.substring(1,s.length()-1) ;
            s = StrUtils.decode(s, MarkerChar) ;
            return Node.createURI(s) ;
        }
        
        try {
            // SSE invocation is expensive (??).
            // Try TokenizerText?
            // Use for literals only.
            Node n = SSE.parseNode(s, pmap) ;
            return n ;
        } catch (SSEParseException ex)
        {
            ALog.fatal(NodeLib.class, "decode: Failed to parse: "+s) ;
            throw ex ;
        }
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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