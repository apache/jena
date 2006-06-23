/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBException;

/** Operations associated with Nodes and layouyt 2 (the Triples+Nodes layout)
 * 
 * @author Andy Seaborne
 * @version $Id$
 */

public class NodeLayout2
{
    // Turn into the lexcial form

    public static String nodeToLex(Node node)
    {
        if ( node.isURI() )        return node.getURI() ;
        if ( node.isLiteral() )    return node.getLiteralLexicalForm() ;
        if ( node.isBlank() )      return node.getBlankNodeId().getLabelString() ;
        throw new SDBException("Can't create lexical representation for "+node) ;
    }
    
    public static String nodeToLexTruncate(Node node, int length)
    { return nodeToLexTruncate(node, length, null) ; }

    public static String nodeToLexTruncate(Node node, int length, String logPrefix)
    {
        String lex = nodeToLex(node) ;
        
        if ( length < 0 )
            return lex ;
        
        if ( lex.length() > length )
        {
            String tmp = "Too long ("+length+"/"+lex.length()+"): "+lex.substring(0,20)+"..." ;
            if ( logPrefix != null )
                tmp = logPrefix+": "+tmp ;
            LogFactory.getLog(NodeLayout2.class).warn(tmp) ;
            lex = lex.substring(0, length) ;
        }
        return lex ;
    }
    
    public static int nodeToType(Node node)
    {
        return ValueType.lookup(node).getTypeId() ;
//        ValueType vType = ValueType.lookup(node) ;
//        if ( vType == ValueType.OTHER )
//            throw new SDBException("Can't find node type for "+node) ;
//        return vType.getTypeId() ;
    }
    
     public static String nodeToLang(Node node)
    {
        if ( !node.isLiteral() ) return "" ;
        String lang = node.getLiteralLanguage() ;
        if ( lang == null ) return "" ;
        return lang ;
    }

    
    public static long hash(Node n)
    {
        // Get the bits and pieces for a value
        String lexForm = (n.isURI()) ? n.getURI() : n.getLiteralLexicalForm() ;
        String datatypeStr = null ;
        if ( n.isLiteral() )
            datatypeStr = n.getLiteralDatatypeURI() ;
        if ( datatypeStr == null )
            datatypeStr = "" ;
        String langStr = null ;
        if ( n.isLiteral() )
            langStr = n.getLiteralLanguage() ;
        if ( langStr == null )
            langStr = "" ;
        ValueType vType = ValueType.lookup(n) ;

        return hash(lexForm,langStr,datatypeStr,vType.getTypeId());
    }
    
    public static long hash(String lex, String lang, String datatype, int type)
    {
        String toHash = lex + "|" + lang + "|" + datatype;
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            digest.update(toHash.getBytes("UTF8"));
            digest.update((byte) type);
            byte[] hash = digest.digest();
            BigInteger bigInt = new BigInteger(hash);
            return bigInt.longValue();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        
        return -1;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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