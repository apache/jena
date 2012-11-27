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

package org.openjena.riot.out;

import java.io.StringWriter ;
import java.io.Writer ;
import java.net.MalformedURLException ;
import java.util.Map ;

import org.openjena.riot.system.PrefixMap ;
import org.openjena.riot.system.Prologue ;
import org.openjena.riot.system.RiotChars ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.IRIRelativize ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Presentation utilitiles for Nodes, Triples, Quads and more */ 
public class NodeFmtLib
{
    // See OutputLangUtils.
    // See and use EscapeStr
    
    static PrefixMap dftPrefixMap = new PrefixMap() ;
    static {
        PrefixMapping pm = ARQConstants.getGlobalPrefixMap() ;
        Map<String, String> map = pm.getNsPrefixMap() ;
        for ( Map.Entry<String, String> e : map.entrySet() )
            dftPrefixMap.add(e.getKey(), e.getValue() ) ;
    }

    public static String str(Triple t)
    {
        return strNodes(t.getSubject(), t.getPredicate(),t.getObject()) ;
    }

    public static String str(Quad q)
    {
        return strNodes(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject()) ;
    }
    

    // Worker
    public static String strNodes(Node ... nodes)
    {
        StringWriter sw = new StringWriter() ;
        boolean first = true ;
        for ( Node n : nodes ) 
        {
            if ( ! first )
            {
                sw.append(" ") ;
                first = false ;
            }
            str(sw, n) ;
        }
        return sw.toString() ; 
    }

    public static String str(Node n)
    {
        StringWriter sw = new StringWriter() ;
        str(sw, n) ;
        return sw.toString() ; 
    }

    private static final boolean onlySafeBNodeLabels = true ;

    //public static String displayStr(Node n) { return serialize(n) ; }

    public static void str(Writer w, Node n)
    { serialize(w, n, null, null) ; }

    public static void serialize(Writer w, Node n, Prologue prologue)
    { serialize(w, n, prologue.getBaseURI(), prologue.getPrefixMap()) ; }

    
    public static void serialize(Writer w, Node n, String base, PrefixMap prefixMap)
    {
        if ( prefixMap == null )
            prefixMap = dftPrefixMap ;
        NodeFormatter formatter = new NodeFormatterTTL(base, prefixMap) ;
        formatter.format(w, n) ;
    }
    
    // ---- Blank node labels.
    
    // Strict N-triples only allows [A-Za-z][A-Za-z0-9]
    static char encodeMarkerChar = 'X' ;

    // These two form a pair to convert bNode labels to a safe (i.e. legal N-triples form) and back agains. 
    
    // Encoding is:
    // 1 - Add a Letter 
    // 2 - Hexify, as Xnn, anything outside ASCII A-Za-z0-9
    // 3 - X is encoded as XX
    
    private static char LabelLeadingLetter = 'B' ; 
    
    public static String encodeBNodeLabel(String label)
    {
        StringBuilder buff = new StringBuilder() ;
        // Must be at least one char and not a digit.
        buff.append(LabelLeadingLetter) ;
        
        for ( int i = 0 ; i < label.length() ; i++ )
        {
            char ch = label.charAt(i) ;
            if ( ch == encodeMarkerChar )
            {
                buff.append(ch) ;
                buff.append(ch) ;
            }
            else if ( RiotChars.isA2ZN(ch) )
                buff.append(ch) ;
            else
                Chars.encodeAsHex(buff, encodeMarkerChar, ch) ;
        }
        return buff.toString() ;
    }

    // Assumes that blank nodes only have characters in the range of 0-255
    public static String decodeBNodeLabel(String label)
    {
        StringBuilder buffer = new StringBuilder() ;

        if ( label.charAt(0) != LabelLeadingLetter )
            return label ;
        
        // Skip first.
        for ( int i = 1; i < label.length(); i++ )
        {
            char ch = label.charAt(i) ;
            
            if ( ch != encodeMarkerChar )
            {
                buffer.append(ch) ;
            }
            else
            {
                // Maybe XX or Xnn.
                char ch2 = label.charAt(i+1) ;
                if ( ch2 == encodeMarkerChar )
                {
                    i++ ;
                    buffer.append(ch) ;
                    continue ;
                }
                
                // Xnn
                i++ ;
                char hiC = label.charAt(i) ;
                int hi = Bytes.hexCharToInt(hiC) ;
                i++ ;
                char loC = label.charAt(i) ;
                int lo = Bytes.hexCharToInt(loC) ;

                int combined = ((hi << 4) | lo) ;
                buffer.append((char) combined) ;
            }
        }

        return buffer.toString() ;
    }
    
    // ---- Relative URIs.
    
    static private int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.CHILD ;
    static public String abbrevByBase(String uri, String base)
    {
        if ( base == null )
            return null ;
        IRI baseIRI = IRIFactory.jenaImplementation().construct(base) ;
        IRI rel = baseIRI.relativize(uri, relFlags) ;
        String r = null ;
        try { r = rel.toASCIIString() ; }
        catch (MalformedURLException  ex) { r = rel.toString() ; }
        return r ;
    }
}
