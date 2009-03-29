/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import java.nio.ByteBuffer;

import atlas.lib.Bytes;
import atlas.lib.StrUtils;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.FmtUtils;

/** Simple encoder/decoder for nodes that uses the SSE string encoding.
 *  The encoding is a length (4 bytes) and a UTF-8 string.
 *  
 *  Note that this is not compatible with UTF-8 strings written with
 *  the standard Java mechanism {@link java.io.DataInput DataInput}/
 *  {@link java.io.DataOutput DataOutput} because they are limited to
 *  64K bytes of UTF-8 data (2 byte length code).
 */

public class NodecSSE implements Nodec
{
    private static boolean SafeChars = false ;
    // Characters in IRIs that are illegal and cause SSE problems, but we wish to keep.
    final private static char MarkerChar = '_' ;
    final private static char[] invalidIRIChars = { MarkerChar , ' ' } ; 
    
    public NodecSSE() {}
    
    @Override
    public void encode(Node node, ByteBuffer bb, int idx, PrefixMapping pmap)
    {
        if ( node.isURI() ) 
        {
            // Pesky spaces etc
            String x = StrUtils.encode(node.getURI(), MarkerChar, invalidIRIChars) ;
            if ( x != node.getURI() )
                node = Node.createURI(x) ; 
        }
        
        String str ;
        if ( node.isBlank() )
            str = "_:"+node.getBlankNodeLabel() ;
        else 
            str = FmtUtils.stringForNode(node, pmap) ;
        if ( idx != 0 )
        {
            bb.position(idx) ;
            bb = bb.slice() ;
        }
        // String -> bytes
        // XXX Length issues
        bb.position(4) ;
        Bytes.toByteBuffer(str, bb) ;
        bb.position(0) ;
        bb.putInt(idx) ;
    }

    @Override
    public Node decode(ByteBuffer bb, int idx, PrefixMapping pmap)
    {
        // XXX Length issues
        int x = bb.getInt(idx) ;
        // Get string.
        bb.position(idx+4) ;
        bb.limit();
        // Bytes -> String 
        String str = Bytes.fromByteBuffer(bb) ;
        // String -> Node
        Node n = SSE.parseNode(str, pmap) ;
        if ( n.isURI() && n.getURI().indexOf(MarkerChar) >= 0 )
        {
            String uri = StrUtils.decode(n.getURI(), '_') ;
            if ( uri != n.getURI() )
                n = Node.createURI(uri) ;
        }
        return n ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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