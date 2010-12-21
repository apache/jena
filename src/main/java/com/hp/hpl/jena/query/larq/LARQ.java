/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.larq;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Version;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.sparql.util.Symbol;

public class LARQ
{
    public static void init()   {} 
    
    // The number of results returned by default
    public static final int NUM_RESULTS             = 1000 ; // should we increase this? -- PC
    
    public static final Version LUCENE_VERSION      = Version.LUCENE_29 ;
    
    public static final boolean AVOID_DUPLICATES    = true;

    // The field that is the index
    public static final String fIndex               = "index" ;

    // This is used to unindex
    public static final String fIndexHash           = "hash" ;
    
    // Object literals
    public static final String fLex                 = "lex" ;
    public static final String fLang                = "lang" ;
    public static final String fDataType            = "datatype" ;
    // Object URI
    public static final String fURI                 = "uri" ;
    // Object bnode
    public static final String fBNodeID             = "bnode" ;

    // The symbol used to register the index in the query context
    public static final Symbol indexKey     = ARQConstants.allocSymbol("lucene") ;

    public static void setDefaultIndex(IndexLARQ index)
    { setDefaultIndex(ARQ.getContext(), index) ; }
    
    public static void setDefaultIndex(Context context, IndexLARQ index)
    { context.set(LARQ.indexKey, index) ; }
    
    public static IndexLARQ getDefaultIndex()
    { return getDefaultIndex(ARQ.getContext()) ; }
    
    public static IndexLARQ getDefaultIndex(Context context)
    { return (IndexLARQ)context.get(LARQ.indexKey) ; }
    
    public static void removeDefaultIndex()
    { removeDefaultIndex(ARQ.getContext()) ; }
    
    public static void removeDefaultIndex(Context context)
    { context.unset(LARQ.indexKey) ; }

//    public static void index(Document doc, Node indexNode)
//    {
//        if ( ! indexNode.isLiteral() )
//            throw new ARQLuceneException("Not a literal: "+indexNode) ;
//        index(doc, indexNode.getLiteralLexicalForm()) ;
//    }        
     
    public static void index(Document doc, Node node, String indexContent)
    {
        Field indexField = new Field(LARQ.fIndex, indexContent, Field.Store.NO, Field.Index.ANALYZED) ;
        doc.add(indexField) ;

        Field indexHashField = new Field(LARQ.fIndexHash, hash(node, indexContent), Field.Store.NO, Field.Index.NOT_ANALYZED) ;
        doc.add(indexHashField) ;
    }        
     
    public static void index(Document doc, Node node, Reader indexContent)
    {
        Field indexField = new Field(LARQ.fIndex, indexContent) ;
        doc.add(indexField) ;

        Field indexHashField = new Field(LARQ.fIndexHash, hash(node, indexContent), Field.Store.NO, Field.Index.NOT_ANALYZED) ;
       	doc.add(indexHashField) ;
    }

	public static Query unindex(Node node, String indexStr)  
	{
		BooleanQuery query = new BooleanQuery();
		query.add(new TermQuery(new Term(LARQ.fIndexHash, hash(node, indexStr))) , Occur.MUST);
        
		return query;
	}

	public static Query unindex(Node node, Reader indexContent) 
	{
		BooleanQuery query = new BooleanQuery();
		query.add(new TermQuery(new Term(LARQ.fIndexHash, hash(node, indexContent))) , Occur.MUST);
        
		return query;
	}

    public static void store(Document doc, Node node)
    {
        // Store.
        if ( node.isLiteral() )
            storeLiteral(doc, (Node_Literal)node) ;
        else if ( node.isURI() )
            storeURI(doc, (Node_URI)node) ;
        else if ( node.isBlank() )
            storeBNode(doc, (Node_Blank)node) ;
        else
            throw new ARQLuceneException("Can't store: "+node) ;
    }

    public static Node build(Document doc)
    {
        String lex = doc.get(LARQ.fLex) ;
        if ( lex != null )
            return buildLiteral(doc) ;
        String uri = doc.get(LARQ.fURI) ;
        if ( uri != null )
            return Node.createURI(uri) ;
        String bnode = doc.get(LARQ.fBNodeID) ;
        if ( bnode != null )
            return Node.createAnon(new AnonId(bnode)) ;
        throw new ARQLuceneException("Can't build: "+doc) ;
    }

    public static boolean isString(Literal literal)
    {
        RDFDatatype dtype = literal.getDatatype() ;
        if ( dtype == null )
            return true ;
        if ( dtype.equals(XSDDatatype.XSDstring) )
            return true ;
        return false ;
    }
    
    private static void storeURI(Document doc, Node_URI node)
    { 
        String x = node.getURI() ;
        Field f = new Field(LARQ.fIndex, x, Field.Store.NO, Field.Index.ANALYZED) ;
        doc.add(f) ;
        f = new Field(LARQ.fURI, x, Field.Store.YES, Field.Index.NO) ;
        doc.add(f) ;
    }

    private static void storeBNode(Document doc, Node_Blank node)
    { 
        String x = node.getBlankNodeLabel() ;
        Field f = new Field(LARQ.fIndex, x, Field.Store.NO, Field.Index.ANALYZED) ;
        doc.add(f) ;
        f = new Field(LARQ.fBNodeID, x, Field.Store.YES, Field.Index.NO) ;
        doc.add(f) ;
    }
    
    private static void storeLiteral(Document doc, Node_Literal node)
    {
        String lex = node.getLiteralLexicalForm() ;
        String datatype = node.getLiteralDatatypeURI() ;
        String lang = node.getLiteralLanguage() ;

        Field f = new Field(LARQ.fLex, lex, Field.Store.YES, Field.Index.NO) ;
        doc.add(f) ;
        
        if ( lang != null )
        {
            f = new Field(LARQ.fLang, lang, Field.Store.YES, Field.Index.NO) ;
            doc.add(f) ;
        }

        if ( datatype != null )
        {       
            f = new Field(LARQ.fDataType, datatype, Field.Store.YES, Field.Index.NO) ;
            doc.add(f) ;
        }
    }
    
    private static Node buildLiteral(Document doc)
    {
        String lex = doc.get(LARQ.fLex) ;
        if ( lex == null )
            return null ;
        String datatype = doc.get(LARQ.fDataType) ;
        String lang = doc.get(LARQ.fLang) ;
        return NodeFactory.createLiteralNode(lex, lang, datatype) ;
    }

    private static String hash (String str) 
    {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(str.getBytes("UTF8"));
            byte[] hash = digest.digest();
            BigInteger bigInt = new BigInteger(hash);
            return bigInt.toString();
        } catch (NoSuchAlgorithmException e) {
        	new ARQLuceneException("hash", e);
        } catch (UnsupportedEncodingException e) {
        	new ARQLuceneException("hash", e);
        }

        return null;
    }
    
    private static String hash (Node node, String str) 
    {
        String lexForm = null ; 
        String datatypeStr = "" ;
        String langStr = "" ;
        
        if ( node.isURI() ) {
        	lexForm = node.getURI() ;
        } else if ( node.isLiteral() ) {
        	lexForm = node.getLiteralLexicalForm() ;
            datatypeStr = node.getLiteralDatatypeURI() ;
            langStr = node.getLiteralLanguage() ;
        } else if ( node.isBlank() ) {
        	lexForm = node.getBlankNodeLabel() ;
        } else {
        	throw new ARQLuceneException("Unable to hash node:"+node) ;
        }

        return hash (lexForm + "|" + langStr + "|" + datatypeStr + "|" + str);
    }
    
    private static String hash (Node node, Reader reader)
    {
        String lexForm = null ; 
        String datatypeStr = "" ;
        String langStr = "" ;
        
        if ( node.isURI() ) {
        	lexForm = node.getURI() ;
        } else if ( node.isLiteral() ) {
        	lexForm = node.getLiteralLexicalForm() ;
            datatypeStr = node.getLiteralDatatypeURI() ;
            langStr = node.getLiteralLanguage() ;
        } else if ( node.isBlank() ) {
        	lexForm = node.getBlankNodeLabel() ;
        } else {
        	throw new ARQLuceneException("Unable to hash node:"+node) ;
        }
    	
    	StringBuffer sb = new StringBuffer();
		try {
	        int charsRead;
			do {
		    	char[] buffer = new char[1024];
		        int offset = 0;
		        int length = buffer.length;
		        charsRead = 0;
				while (offset < buffer.length) {
					charsRead = reader.read(buffer, offset, length);
					if (charsRead == -1)
						break;
					offset += charsRead;
					length -= charsRead;
				}
				sb.append(buffer);
			} while (charsRead != -1);
			reader.reset();
		} catch (IOException e) {
			new ARQLuceneException("hash", e);
		}
		
		return hash (lexForm + "|" + langStr + "|" + datatypeStr + "|" + sb.toString());
    }

}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
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