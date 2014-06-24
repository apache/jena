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

package org.apache.jena.riot.process.normalize;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.riot.web.LangTag ;


import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class CanonicalizeLiteral implements NodeTransform    
{
    private static final CanonicalizeLiteral singleton = new CanonicalizeLiteral(); 

    public static CanonicalizeLiteral get() { return singleton ; }

    private CanonicalizeLiteral() {}
    
    @Override
    public Node convert(Node node)
    {
        if ( ! node.isLiteral() )
            return node ;
            
        RDFDatatype dt = node.getLiteralDatatype() ;
        Node n2 ;
        if ( dt == null )
        {
            if ( node.getLiteralLanguage().equals("") )
                //n2 = NormalizeValue.dtSimpleLiteral.handle(node, node.getLiteralLexicalForm(), null) ;
                return node ;
            else
                n2 = canonicalLangtag(node, node.getLiteralLexicalForm(), node.getLiteralLanguage()) ; 
        }
        else
        {
            // Valid?  Yes - assumes checking has been done.
            // May integrate later
            DatatypeHandler handler = dispatch.get(dt) ;
            if ( handler == null )
                return node ;
    
            n2 = handler.handle(node, node.getLiteralLexicalForm(), dt) ;
        }
        if ( n2 == null )
            return node ;
        return n2 ;
    }
    
    private static Node canonicalLangtag(Node node, String lexicalForm, String langTag)
    {
        String langTag2 = LangTag.canonical(langTag) ;
        if ( langTag2.equals(langTag) )
            return null ;
        return NodeFactory.createLiteral(lexicalForm, langTag2, null) ;
    }
    
    private static final RDFDatatype dtPlainLiteral = NodeFactory.getType(RDF.getURI()+"PlainLiteral") ;
    
    private final static Map<RDFDatatype, DatatypeHandler> dispatch = new HashMap<>() ;

    // MUST be after the handler definitions as these assign to statics, so it's code lexcial order.
    // or use static class to force touching that, initializing and then getting the values. 
    static {
        dispatch.put(XSDDatatype.XSDinteger,                NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDdecimal,                NormalizeValue.dtDecimal) ;

        // Subtypes. Changes the datatype.
        dispatch.put(XSDDatatype.XSDint,                    NormalizeValue.dtInteger) ;        
        dispatch.put(XSDDatatype.XSDlong,                   NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDshort,                  NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDbyte,                   NormalizeValue.dtInteger) ;

        dispatch.put(XSDDatatype.XSDunsignedInt,            NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDunsignedLong,           NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDunsignedShort,          NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDunsignedByte,           NormalizeValue.dtInteger) ;

        dispatch.put(XSDDatatype.XSDnonPositiveInteger,     NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDnonNegativeInteger,     NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDpositiveInteger,        NormalizeValue.dtInteger) ;
        dispatch.put(XSDDatatype.XSDnegativeInteger,        NormalizeValue.dtInteger) ;

        dispatch.put(XSDDatatype.XSDfloat,      NormalizeValue.dtFloat ) ;
        dispatch.put(XSDDatatype.XSDdouble,     NormalizeValue.dtDouble ) ;

        // Only fractional seconds part can vary for the same value. 
        dispatch.put(XSDDatatype.XSDdateTime,   NormalizeValue.dtDateTime) ;
        
        // These are fixed format 
//        dispatch.put(XSDDatatype.XSDdate,       null) ;
//        dispatch.put(XSDDatatype.XSDtime,       null) ;
//        dispatch.put(XSDDatatype.XSDgYear,      null) ;
//        dispatch.put(XSDDatatype.XSDgYearMonth, null) ;
//        dispatch.put(XSDDatatype.XSDgMonth,     null) ;
//        dispatch.put(XSDDatatype.XSDgMonthDay,  null) ;
//        dispatch.put(XSDDatatype.XSDgDay,       null) ;

        dispatch.put(XSDDatatype.XSDduration,   null) ;
        dispatch.put(XSDDatatype.XSDboolean,    NormalizeValue.dtBoolean) ;
        
        dispatch.put(XSDDatatype.XSDstring,     NormalizeValue.dtXSDString) ;

        
        dispatch.put(dtPlainLiteral,            NormalizeValue.dtPlainLiteral) ;
        
    }
}
