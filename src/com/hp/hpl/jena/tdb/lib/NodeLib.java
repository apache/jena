/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.lib;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfLong;

import java.io.UnsupportedEncodingException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import lib.Bytes;

import com.hp.hpl.jena.rdf.model.AnonId;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.sse.SSEParseException;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.pgraph.Hash;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.NodeType;

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
        
        try {
            return SSE.parseNode(s, pmap) ;
        } catch (SSEParseException ex)
        {
            ALog.fatal(NodeLib.class, "decode: Failed to parse: "+s) ;
            throw ex ;
        }
    }
    /** Get the triples in the form of a List<Triple> */
    public static List<Triple> tripleList(OpBGP opBGP)
    {
        return tripleList(opBGP.getPattern()) ;
    }
    
    /** Get the triples in the form of a List<Triple> */
    public static List<Triple> tripleList(BasicPattern pattern)
    {
        return tripleList(pattern.getList()) ;
    }
    
    /** Cast a list (known to be triples, e.g. from Java 1.4) to a List<Triple> */
    public static List<Triple> tripleList(List<?> triples)
    {
        @SuppressWarnings("unchecked")
        List<Triple> x = (List<Triple>)triples ;
        return x ;
    }
    
    /** Get the triples in the form of a List<Triple> */
    public static List<Quad> quadList(OpQuadPattern opQuad)
    {
        return quadList(opQuad.getQuads()) ;
    }
    
    /** Get the triples in the form of a List<Triple> */
    public static List<Quad> quadList(QuadPattern pattern)
    {
        return quadList(pattern.getList()) ;
    }

    /** Cast a list (known to be triples, e.g. from Java 1.4) to a List<Triple> */
    public static List<Quad> quadList(List<?> quads)
    {
        @SuppressWarnings("unchecked")
        List<Quad> x = (List<Quad>)quads ;
        return x ;
    }
    
    public static Hash hash(Node n)
    { 
        Hash h = new Hash(LenNodeHash) ;
        setHash(h, n) ;
        return h ;
    }
    
    public static void setHash(Hash h, Node n) 
    {
        NodeType nt = NodeType.lookup(n) ;
        switch(nt) 
        {
            case URI:
                hash(h, n.getURI(), null, null, nt) ;
                return ;
            case BNODE:
                hash(h, n.getBlankNodeLabel(), null, null, nt) ;
                return ;
            case LITERAL:
                hash(h,
                     n.getLiteralLexicalForm(),
                     n.getLiteralLanguage(),
                     n.getLiteralDatatypeURI(), nt) ;
                return  ;
            case OTHER:
                throw new TDBException("Attempt to hash something strange: "+n) ; 
        }
        throw new TDBException("NodeType broken: "+n) ; 
    }
    
    private static void hash(Hash h, String lex, String lang, String datatype, NodeType nodeType)
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
            if ( h.getLen() == 16 )
                // MD5 is 16 bytes.
                digest.digest(h.getBytes(), 0, 16) ;
            else
            {
                byte b[] = digest.digest(); // 16 bytes.
                // Avoid the copy? If length is 16.  digest.digest(bytes, 0, length) needs 16 bytes
                System.arraycopy(b, 0, h.getBytes(), 0, h.getLen()) ;
            }
            return ;
        }
        catch (NoSuchAlgorithmException e)
        { e.printStackTrace(); }
        catch (UnsupportedEncodingException e)
        { e.printStackTrace(); } 
        catch (DigestException ex)
        { ex.printStackTrace(); }
        return ;
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