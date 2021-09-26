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
import java.util.function.Function;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.web.LangTag ;
import org.apache.jena.sparql.util.NodeUtils ;
import org.apache.jena.vocabulary.RDF ;

/** Convert literals to canonical form. */
public class CanonicalizeLiteral implements Function<Node, Node>
{
    private static final CanonicalizeLiteral singleton = new CanonicalizeLiteral();

    public static CanonicalizeLiteral get() { return singleton ; }

    private CanonicalizeLiteral() {}

    /**
     * Canonicaize a literal, both lexical form and language tag (RFc canonical).
     */
    @Override
    public Node apply(Node node) {
        if ( ! node.isLiteral() )
            return node ;

        if ( ! node.getLiteralDatatype().isValid(node.getLiteralLexicalForm()) )
            // Invalid lexical form for the datatype - do nothing.
            return node;

        RDFDatatype dt = node.getLiteralDatatype() ;
        Node n2 ;
        if ( NodeUtils.isLangString(node) ) {
            // RDF 1.0, no datatype ; RDF 1.1 : datatype is rdf:langString
            if ( node.getLiteralLanguage().equals("") )
                //n2 = NormalizeValue.dtSimpleLiteral.handle(node, node.getLiteralLexicalForm(), null) ;
                return node ;
            else
                n2 = canonicalLangtag(node.getLiteralLexicalForm(), node.getLiteralLanguage()) ;
        } else if ( dt == null ) {
            // RDF 1.0 / no lang.
            n2 = NormalizeValue.dtSimpleLiteral.handle(node, node.getLiteralLexicalForm(), null) ;
        } else {
            // Dataype, not rdf:langString (RDF 1.1).
            DatatypeHandler handler = dispatch.get(dt) ;
            if ( handler == null )
                return node ;
            n2 = handler.handle(node, node.getLiteralLexicalForm(), dt) ;
        }

        if ( n2 == null )
            return node ;
        return n2 ;
    }

    /** Convert the lexical form to a canonical form if one of the known datatypes,
     * otherwise return the node argument. (same object :: {@code ==})
     */
    public static Node canonicalValue(Node node) {
        if ( ! node.isLiteral() )
            return node ;
        // Fast-track
        if ( NodeUtils.isLangString(node) )
            return node;
        if ( NodeUtils.isSimpleString(node) )
            return node;

        if ( ! node.getLiteralDatatype().isValid(node.getLiteralLexicalForm()) )
            // Invalid lexical form for the datatype - do nothing.
            return node;

        RDFDatatype dt = node.getLiteralDatatype() ;
        // Datatype, not rdf:langString (RDF 1.1).
        DatatypeHandler handler = dispatch.get(dt) ;
        if ( handler == null )
            return node ;
        Node n2 = handler.handle(node, node.getLiteralLexicalForm(), dt) ;
        if ( n2 == null )
            return node ;
        return n2 ;
    }

    /** Convert the language tag of a lexical form to a canonical form if one of the known datatypes,
     * otherwise return the node argument. (same object; compare by {@code ==})
     */
    private static Node canonicalLangtag(String lexicalForm, String langTag) {
        String langTag2 = LangTag.canonical(langTag);
        if ( langTag2.equals(langTag) )
            return null;
        return NodeFactory.createLiteral(lexicalForm, langTag2);
    }

    private static final RDFDatatype dtPlainLiteral = NodeFactory.getType(RDF.getURI()+"PlainLiteral") ;

    private final static Map<RDFDatatype, DatatypeHandler> dispatch = new HashMap<>() ;

    // MUST be after the handler definitions as these assign to statics, so it's code lexical order,
    // or use a static class to force touching that, initializing and then getting the values.
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

        // Convert to RDF 1.1 form - no explicit datatype.
        dispatch.put(XSDDatatype.XSDstring,     NormalizeValue.dtXSDString) ;
        // Convert (illegal) rdf:PlainLiteral to a legal RDF term.
        dispatch.put(dtPlainLiteral,            NormalizeValue.dtPlainLiteral) ;
    }
}
