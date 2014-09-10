/**
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

package org.apache.jena.riot.thrift;

import static org.apache.jena.riot.thrift.TRDF.ANY ;

import java.math.BigDecimal ;
import java.math.BigInteger ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.thrift.wire.* ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;

/** Convert to and from Thrift wire objects.
 * See {@linkplain StreamRDF2Thrift} and {@linkplain Thrift2StreamRDF}
 * for ways to convert as streams (they recycle intermediate objects).
 * @see StreamRDF2Thrift
 * @see Thrift2StreamRDF
 */
public class ThriftConvert
{
    private static final PrefixMap emptyPrefixMap = PrefixMapFactory.emptyPrefixMap() ;
    
    public static Node convert(RDF_Term term) {
        return convert(term, emptyPrefixMap) ;
    }
     
    public static Node convert(RDF_Term term, PrefixMap pmap) {
        if ( term.isSetPrefixName() ) {
            String x = expand(term.getPrefixName(), pmap) ;
            if ( x != null )
                return NodeFactory.createURI(x) ;
            throw new RiotThriftException("Failed to expand "+term) ;
            //                Log.warn(BinRDF.class, "Failed to expand "+term) ;
            //                return NodeFactory.createURI(prefix+":"+localname) ; 
        }
        
        if ( term.isSetIri() )
            return NodeFactory.createURI(term.getIri().getIri()) ;
        
        if ( term.isSetBnode() )
            return NodeFactory.createAnon(new AnonId(term.getBnode().getLabel())) ;

        if ( term.isSetLiteral() ) {
            RDF_Literal lit = term.getLiteral() ;
            String lex = lit.getLex() ;
            String dtString = null ;
            if ( lit.isSetDatatype() )
                dtString = lit.getDatatype() ;
            else if ( lit.isSetDtPrefix()) {
                String x = expand(lit.getDtPrefix(), pmap) ;
                if ( x == null )
                    throw new RiotThriftException("Failed to expand datatype prefix name:"+term) ;
                dtString = x ;
            }
            RDFDatatype dt = NodeFactory.getType(dtString) ;
                
            String lang = lit.getLangtag() ;
            return NodeFactory.createLiteral(lex, lang, dt) ;
        }
        
        if ( term.isSetValInteger() ) {
            long x = term.getValInteger() ;
            String lex = Long.toString(x, 10) ;
            RDFDatatype dt = XSDDatatype.XSDinteger ;
            return NodeFactory.createLiteral(lex, null, dt) ;
        }
        
        if ( term.isSetValDouble() ) {
            double x = term.getValDouble() ;
            String lex = Double.toString(x) ;
            RDFDatatype dt = XSDDatatype.XSDdouble ;
            return NodeFactory.createLiteral(lex, null, dt) ;
        }
        
        if ( term.isSetValDecimal() ) {
            long value = term.getValDecimal().getValue() ;
            int scale =  term.getValDecimal().getScale() ;
            BigDecimal d =  BigDecimal.valueOf(value, scale) ;
            String lex = d.toPlainString() ;
            RDFDatatype dt = XSDDatatype.XSDdecimal ;
            return NodeFactory.createLiteral(lex, null, dt) ;
        }

        if ( term.isSetVariable() )
            return Var.alloc(term.getVariable().getName()) ;
        
        if ( term.isSetAny() )
            return Node.ANY ;
        
        if ( term.isSetUndefined() )
            return null;
        
        throw new RiotThriftException("No conversion to a Node: "+term.toString()) ;
    }
    
    private static String expand(RDF_PrefixName prefixName, PrefixMap pmap) {
        if ( pmap == null )
            return null ;

        String prefix = prefixName.getPrefix() ;
        String localname  = prefixName.getLocalName() ;
        String x = pmap.expand(prefix, localname) ;
        if ( x == null ) 
            throw new RiotThriftException("Failed to expand "+prefixName) ;
        return x ;
    }
    

    public static RDF_Term convert(Node node, boolean allowValues) {
        return convert(node, null, allowValues) ;
    }

    public static RDF_Term convert(Node node, PrefixMap pmap, boolean allowValues) {
        RDF_Term n = new RDF_Term() ;
        toThrift(node, pmap, n, allowValues) ;
        return n ;
    }

    static final BigInteger MAX_I = BigInteger.valueOf(Long.MAX_VALUE) ;
    static final BigInteger MIN_I = BigInteger.valueOf(Long.MIN_VALUE) ;
    
    /** Attempt to encode a node by value (integer, decimal, double) into an RDF_term.
     * @param node
     * @param term
     * @return true if the term was set, else false.
     */
    
    public static boolean toThriftValue(Node node, RDF_Term term) {
        if ( ! node.isLiteral() ) 
            return false ;
        
        // Value cases : Integer, Double, Decimal
        String lex = node.getLiteralLexicalForm() ;
        RDFDatatype rdt = node.getLiteralDatatype() ;
            
        if ( rdt == null ) 
            return false ;
        
        if ( rdt.equals(XSDDatatype.XSDdecimal) ) {
            if ( rdt.isValid(lex)) {
                BigDecimal decimal = new BigDecimal(lex.trim()) ;
                int scale = decimal.scale() ;
                BigInteger bigInt = decimal.unscaledValue() ;
                if ( bigInt.compareTo(MAX_I) <= 0 && bigInt.compareTo(MIN_I) >= 0 ) {
                    // This check makes sure that bigInt.longValue() is safe
                    RDF_Decimal d = new RDF_Decimal(bigInt.longValue(), scale) ;
                    RDF_Term literal = new RDF_Term() ;
                    term.setValDecimal(d) ;
                    return true ;
                }
            }
        } else if ( 
            rdt.equals(XSDDatatype.XSDinteger) ||  
            rdt.equals(XSDDatatype.XSDlong) ||
            rdt.equals(XSDDatatype.XSDint) ||
            rdt.equals(XSDDatatype.XSDshort) ||
            rdt.equals(XSDDatatype.XSDbyte) 
            ) {
            // and 4 unsigned equivalents 
            // and positive, negative, nonPostive nonNegativeInteger
            
            // Conservative - no derived types.
            if ( rdt.isValid(lex)) {
                try {
                    long v = ((Number)node.getLiteralValue()).longValue() ;
                    term.setValInteger(v) ;
                    return true ;
                }
                // Out of range for the type, not a long etc etc.
                catch (Throwable ex) { }
            }
        } else if ( rdt.equals(XSDDatatype.XSDdouble) ) {
            // XSDfloat??
            if ( rdt.isValid(lex)) {
                try {
                    double v = ((Double)node.getLiteralValue()).doubleValue() ;
                    term.setValDouble(v) ;
                    return true ;
                }
                // Out of range for the type, ...
                catch (Throwable ex) { }
            }
        }
        return false ;
    }
    
    public static void toThrift(Node node, PrefixMap pmap, RDF_Term term, boolean allowValues) {
        if ( node == null) {
            term.setUndefined(TRDF.UNDEF);
            return;
        }
        
        if ( node.isURI() ) {
            RDF_PrefixName prefixName = contract(node.getURI(), pmap) ;
            if ( prefixName != null ) {
                term.setPrefixName(prefixName) ;
                return ;
            }
        }
        
        if ( node.isBlank() ) {
            RDF_BNode b = new RDF_BNode(node.getBlankNodeLabel()) ;
            term.setBnode(b) ;
            return ;
        }
        
        if ( node.isURI() ) {
            RDF_IRI iri = new RDF_IRI(node.getURI()) ;
            term.setIri(iri) ;
            return ;
        }
        
        if ( node.isLiteral() ) {
            // Value cases : Integer, Double, Decimal
            if ( allowValues) {
                boolean b = toThriftValue(node, term) ;
                if ( b /* term.isSet() */ )
                    return ; 
            }
            
            String lex = node.getLiteralLexicalForm() ;
            String dt = node.getLiteralDatatypeURI() ;
            String lang = node.getLiteralLanguage() ;
            
            // General encoding.
            RDF_Literal literal = new RDF_Literal(lex) ;
            if ( dt != null ) {
                RDF_PrefixName dtPrefixName = contract(dt, pmap) ;
                if ( dtPrefixName != null )
                    literal.setDtPrefix(dtPrefixName) ;
                else
                    literal.setDatatype(dt) ;
            }
            if ( lang != null && ! lang.isEmpty() )
                literal.setLangtag(lang) ;
            term.setLiteral(literal) ;
            return ;
        }
        
        if ( node.isVariable() ) {
            RDF_VAR var = new RDF_VAR(node.getName()) ;
            term.setVariable(var) ;
            return ;
        }
        if ( Node.ANY.equals(node)) {
            term.setAny(ANY) ;
            return ;
        }
        throw new RiotThriftException("Node converstion not supported: "+node) ;
    }
    
    private static RDF_PrefixName contract(String uriStr, PrefixMap pmap) {
        if ( pmap == null )
            return null ;
        Pair<String, String> p = pmap.abbrev(uriStr) ;
        if ( p == null )
            return null ;
        return new RDF_PrefixName(p.getLeft(), p.getRight()) ;
    }

    public static Triple convert(RDF_Triple triple) {
        return convert(triple, null) ; 
    }

    public static Triple convert(RDF_Triple rt, PrefixMap pmap) {
        Node s = convert(rt.getS(), pmap) ;
        Node p = convert(rt.getP(), pmap) ;
        Node o = convert(rt.getO(), pmap) ;
        return Triple.create(s, p, o) ;
    }

    public static RDF_Triple convert(Triple triple, boolean allowValues) {
        return convert(triple, null, allowValues) ;
    }

    public static RDF_Triple convert(Triple triple, PrefixMap pmap, boolean allowValues) {
        RDF_Triple t = new RDF_Triple() ;
        RDF_Term s = convert(triple.getSubject(), pmap, allowValues) ;
        RDF_Term p = convert(triple.getPredicate(), pmap, allowValues) ;
        RDF_Term o = convert(triple.getObject(), pmap, allowValues) ;
        t.setS(s) ; 
        t.setP(p) ; 
        t.setO(o) ;
        return t ;
    }

    public static Quad convert(RDF_Quad quad) {
        return convert(quad, null) ; 
    }

    public static Quad convert(RDF_Quad rq, PrefixMap pmap) {
        Node g = (rq.isSetG() ? convert(rq.getG(), pmap) : null ) ;
        Node s = convert(rq.getS(), pmap) ;
        Node p = convert(rq.getP(), pmap) ;
        Node o = convert(rq.getO(), pmap) ;
        return Quad.create(g, s, p, o) ;
    }

    public static RDF_Quad convert(Quad quad, boolean allowValues) {
        return convert(quad, null, allowValues) ;
    }
    
    public static RDF_Quad convert(Quad quad, PrefixMap pmap, boolean allowValues) {
        RDF_Quad q = new RDF_Quad() ;
        RDF_Term g = null ;
        if ( quad.getGraph() != null )
            g = convert(quad.getGraph(), pmap, allowValues) ;
        RDF_Term s = convert(quad.getSubject(), pmap, allowValues) ;
        RDF_Term p = convert(quad.getPredicate(), pmap, allowValues) ;
        RDF_Term o = convert(quad.getObject(), pmap, allowValues) ;
        if ( g != null )
            q.setG(g) ;
        q.setS(s) ; 
        q.setP(p) ; 
        q.setO(o) ; 
        return q ;
    }

        // RDF_Tuple => RDF_row (for result sets) or List<RDFTerm>
    
//    public static Tuple<Node> convert(RDF_Tuple row) {
//        return convert(row, null) ; 
//    }
//
//    public static Tuple<Node> convert(RDF_Tuple row, PrefixMap pmap) {
//        List<RDF_Term> terms = row.getTerms() ;
//        Node[] tuple = new Node[terms.size()] ;
//        int idx = 0 ;
//        for ( RDF_Term rt : terms ) {
//            tuple[idx] = convert(rt, pmap) ; 
//            idx ++ ;
//        }    

//        return Tuple.create(tuple) ;
//    }
//
//    public static RDF_Tuple convert(Tuple<Node> tuple) {
//        return convert(tuple, null) ; 
//    }
//    
//    public static RDF_Tuple convert(Tuple<Node> tuple, PrefixMap pmap) {
//        RDF_Tuple rTuple = new RDF_Tuple() ;
//        for ( Node n : tuple ) {
//            RDF_Term rt = convert(n, pmap) ;
//            rTuple.addToTerms(rt) ;
//        }
//        return rTuple ;
//    }
}
