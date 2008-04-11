/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.lib;

import static com.hp.hpl.jena.tdb.Const.SizeOfLong;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lib.Bytes;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.NodeType;
import com.hp.hpl.jena.tdb.pgraph.PGraphException;

public class NodeLib
{
    public static String encode(Node node)  { return encode(node, null) ; }

    public static String encode(Node node, PrefixMapping pmap)
    {
        if ( node.isBlank() )
            return "_:"+node.getBlankNodeLabel() ;
        return FmtUtils.stringForNode(node, pmap) ;
    }

    public static Node decode(String s)     { return decode(s, null) ; }
    
    public static Node decode(String s, PrefixMapping pmap)
    {
        if ( s.startsWith("_:") )   
        {
            s = s.substring(2) ;
            return Node.createAnon(new AnonId(s)) ;
        }
        
        return SSE.parseNode(s, pmap) ;
    }
    
    public static long hash(Node n)
    {
        // Get the bits and pieces for a value
        String lexForm ; 
        String lang = "" ;
        String datatype ;
        
        NodeType nt = NodeType.lookup(n) ;
        switch(nt) 
        {
            case URI:
                return hash(n.getURI(), null, null, nt) ;
            case BNODE:
                return hash(n.getBlankNodeLabel(), null, null, nt) ;
            case LITERAL:
                return hash(n.getLiteralLexicalForm(),
                         n.getLiteralLanguage(),
                         n.getLiteralDatatypeURI(),nt) ; 
            case OTHER:
                throw new PGraphException("Attempt to hash somethign strange: "+n) ; 
        }
        throw new PGraphException("NodeType broken: "+n) ; 
    }

    private static long hash(String lex, String lang, String datatype, NodeType nodeType)
    {
        if ( datatype == null )
            datatype = "" ;
        if ( lang == null )
            lang = "" ;
        String toHash = lex + "|" + lang + "|" + datatype+"|"+nodeType.getName() ;
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            digest.update(toHash.getBytes("UTF8"));
            byte[] hash = digest.digest();
            BigInteger bigInt = new BigInteger(hash);
            return bigInt.longValue();
        }
        catch (NoSuchAlgorithmException e)
        { e.printStackTrace(); }
        catch (UnsupportedEncodingException e)
        { e.printStackTrace(); }
        return -1;
    }
    
    public static long getId(Record r, int idx)
    {
        return Bytes.getLong(r.getKey(), idx) ;
    }
    
    public static Record record(RecordFactory factory, NodeId id1, NodeId id2, NodeId id3)
    {
        byte[] b = new byte[3*NodeId.SIZE] ;
        Bytes.setLong(id1.getId(), b, 0) ;  
        Bytes.setLong(id2.getId(), b, SizeOfLong) ;
        Bytes.setLong(id3.getId(), b, 2*SizeOfLong) ;
        return factory.create(b) ;
    }
    
    public static Record record(RecordFactory factory, long id1, long id2, long id3)
    {
        byte[] b = new byte[3*SizeOfLong] ;
        Bytes.setLong(id1, b, 0) ;  
        Bytes.setLong(id2, b, SizeOfLong) ;
        Bytes.setLong(id3, b, 2*SizeOfLong) ;
        return factory.create(b) ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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