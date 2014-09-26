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

package com.hp.hpl.jena.sparql.util;

import java.util.Calendar;

import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/**
 * Various convenience helper methods for converting to and from nodes
 */
public class NodeFactoryExtra
{
    private static final PrefixMap prefixMappingDefault = PrefixMapFactory.createForInput(SSE.getDefaultPrefixMapRead()) ; 
    
    /** 
     * Parse a node - with convenience prefix mapping 
     * <p>
     * Allows surrounding white space
     * </p>
     * @param nodeString Node string to parse
     *  
     */ 
    public static Node parseNode(String nodeString)
    {
        return parseNode(nodeString, prefixMappingDefault) ;
    }

    private static PrefixMap pmapEmpty = PrefixMapFactory.create() ; 

    /** Parse a string into a node. 
     * <p>
     * Allows surrounding white space.
     * </p>
     * @param nodeString Node string to parse
     * @param pmap Prefix Map, null to use no prefix mappings
     * @return Parsed Node
     * @throws RiotException Thrown if a valid node cannot be parsed
     */ 
    public static Node parseNode(String nodeString, PrefixMap pmap)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(nodeString) ;
        if ( ! tokenizer.hasNext() )
            throw new RiotException("Empty RDF term") ; 
        Token token = tokenizer.next() ;
        Node node = token.asNode(pmap) ;
        if ( node == null )
            throw new RiotException("Bad RDF Term: "+nodeString) ;

        if ( tokenizer.hasNext() )
            throw new RiotException("Trailing characters in string: "+nodeString) ;
        if ( node.isURI() )
        {
            // Lightly test for bad URIs.
            String x = node.getURI() ;
            if ( x.indexOf(' ') >= 0 )
                throw new RiotException("Space(s) in  IRI: "+ nodeString) ;
        }
        return node ;
    }
    
    private static QueryParseException makeException(String msg, int line, int column)
    {
        return new QueryParseException(msg, line, column) ;
    }
    
    /** Create a literal Node, when the datatype, if given, is a string */ 
    public static Node createLiteralNode(String lex, String lang, String datatypeURI)
    {
        if ( datatypeURI != null && datatypeURI.equals("") )
            datatypeURI = null ;
        
        if ( lang != null && lang.equals("") )
            lang = null ;
        
        RDFDatatype dType = null ;
        if ( datatypeURI != null )
            dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
        
        Node n = NodeFactory.createLiteral(lex, lang, dType) ;
        return n ;
    }
    
    /** Node to int
     *  
     * @param node
     * @return The int value or Integer.MIN_VALUE.
     */
    public static int nodeToInt(Node node)
    {
        LiteralLabel lit = node.getLiteral() ;
        
        if ( ! XSDDatatype.XSDint.isValidLiteral(lit) )
            return Integer.MIN_VALUE ;
        int i = ((Number)lit.getValue()).intValue() ;
        return i ;
    }
    
    /** Node to long
     *  
     * @param node
     * @return The long value or Long.MIN_VALUE.
     */
    public static long nodeToLong(Node node)
    {
        LiteralLabel lit = node.getLiteral() ;
        
        if ( ! XSDDatatype.XSDlong.isValidLiteral(lit) )
            return Long.MIN_VALUE ;
        long i = ((Number)lit.getValue()).longValue() ;
        return i ;
    }
    
    /** Node to float
     *  
     * @param node
     * @return The float value or Float.NaN
     */

    public static float nodeToFloat(Node node)
    {
        LiteralLabel lit = node.getLiteral();
        
        if ( ! XSDDatatype.XSDfloat.isValidLiteral(lit) )
            return Float.NaN;
        float f = ((Number)lit.getValue()).floatValue();
        return f;
    }
    
    /** Node to double
     *  
     * @param node
     * @return The double value or Double.NaN
     */
    public static double nodeToDouble(Node node)
    {
        LiteralLabel lit = node.getLiteral();
        
        if ( ! XSDDatatype.XSDdouble.isValidLiteral(lit) )
            return Double.NaN;
        double d = ((Number)lit.getValue()).doubleValue();
        return d;
    }
    
    /** int to Node
     * 
     * @param integer
     * @return An xsd:integer
     */
    public static Node intToNode(int integer)
    {
        return NodeFactory.createLiteral(Integer.toString(integer), "", XSDDatatype.XSDinteger) ;
    }

    /** long to Node
     * 
     * @param integer
     * @return An xsd:integer
     */
    public static Node intToNode(long integer)
    {
        return NodeFactory.createLiteral(Long.toString(integer), "", XSDDatatype.XSDinteger) ;
    }

    /** float to Node
     * 
     * @param value
     * @return An xsd:float
     */
    public static Node floatToNode(float value)
    {
        return NodeFactory.createLiteral(Float.toString(value), "", XSDDatatype.XSDfloat) ;
    }
    
    /** double to Node
     * 
     * @param value
     * @return An double
     */
    public static Node doubleToNode(double value)
    {
        return NodeFactory.createLiteral(Double.toString(value), "", XSDDatatype.XSDdouble) ;
    }
    
    /** Calendar to xsd:dateTime Node */
    public static Node dateTimeToNode(Calendar c)
    {
        String lex = Utils.calendarToXSDDateTimeString(c);
        return NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdateTime);
    }
    
    /** Calendar to xsd:date Node */
    public static Node dateToNode(Calendar c)
    {
        String lex = Utils.calendarToXSDDateString(c);
        return NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdate);
    }
    
    /** Calendar to xsd:time Node */
    public static Node timeToNode(Calendar c)
    {
        String lex = Utils.calendarToXSDTimeString(c);
        return NodeFactory.createLiteral(lex, null, XSDDatatype.XSDtime);
    }

    /** Now, as xsd:dateTime Node */
    public static Node nowAsDateTime()
    {
        String lex = Utils.nowAsXSDDateTimeString() ;
        return NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdateTime) ;
    }

    /** Today, as xsd:date Node */
    public static Node todayAsDate()
    {
        String lex = Utils.todayAsXSDDateString() ;
        return NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdate) ;
    }

}
