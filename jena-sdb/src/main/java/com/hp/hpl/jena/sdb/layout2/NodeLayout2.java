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

package com.hp.hpl.jena.sdb.layout2;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBException;

/** Operations associated with nodes and layout 2 (the Triples+Nodes layout)
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
            LoggerFactory.getLogger(NodeLayout2.class).warn(tmp) ;
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
        String lexForm = null ; 
        
        if ( n.isURI() )           lexForm = n.getURI() ;
        else if ( n.isLiteral() )  lexForm = n.getLiteralLexicalForm() ;
        else if ( n.isBlank() )    lexForm = n.getBlankNodeLabel() ;
        else throw new SDBException("Attempt to hash a variable") ;
                         
        String datatypeStr = "" ;
        if ( n.isLiteral() )
            datatypeStr = n.getLiteralDatatypeURI() ;
        String langStr = "" ;
        if ( n.isLiteral() )
            langStr = n.getLiteralLanguage() ;
        ValueType vType = ValueType.lookup(n) ;

        return hash(lexForm,langStr,datatypeStr,vType.getTypeId());
    }
    
    public static long hash(String lex, String lang, String datatype, int type)
    {
        if ( datatype == null )
            datatype = "" ;
        if ( lang == null )
            lang = "" ;
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
