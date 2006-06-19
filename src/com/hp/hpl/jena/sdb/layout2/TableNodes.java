/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
/**
 * @author Andy Seaborne
 * @version $Id: TableNodes.java,v 1.2 2006/04/19 17:23:32 andy_seaborne Exp $
 */

public class TableNodes extends SqlTable
{
    private static Log log = LogFactory.getLog(TableNodes.class) ;
    
    public static final String tableName       = "Nodes" ;
    
//    // Note: URIs are actually stored in the lexical slot.  It's only datatype URIs that need be shorter. 
//    public static final int LexicalLength          = 10*1024 ;
//    public static final int LexicalLengthIndex     = 200 ;  // Index the first X bytes
    public static final int UriLength              = 200 ;
//    public static final int UriLengthIndex         = 200 ;
    
    public static final String colId           = "id" ;
    public static final String colHash         = "hash" ;
    public static final String colLex          = "lex" ;
    
//    public static final Map<String, String> colNameMap = new HashMap<String, String>() ;
//    static {
//        colNameMap.put("{"+colId+"}",        colId) ;
//        colNameMap.put("{"+colHash+"}",      colHash) ;
//        colNameMap.put("{"+colLex+"}",       colLex) ; 
//        colNameMap.put("{"+colLang+"}",      colLang) ; 
//        colNameMap.put("{"+colDatatype+"}",  colDatatype) ; 
//        colNameMap.put("{"+colType+"}",      colType) ; 
//        colNameMap.put("{"+colInt+"}",       colInt) ; 
//        colNameMap.put("{"+colDouble+"}",    colDouble) ; 
//        colNameMap.put("{"+colDatetime+"}",  colDatetime) ; 
//    }
    
    public TableNodes(String aliasName)
    { super(tableName, aliasName) ; }
    
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
            log.warn(tmp) ;
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
    
     public static String nodeToLang(Node node)
    {
        if ( !node.isLiteral() ) return "" ;
        String lang = node.getLiteralLanguage() ;
        if ( lang == null ) return "" ;
        return lang ;
    }

}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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