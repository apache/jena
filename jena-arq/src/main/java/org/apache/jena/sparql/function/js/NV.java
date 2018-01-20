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

package org.apache.jena.sparql.function.js;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * General representation of an {@link NodeValue} for JavaScript. Conversion is to native
 * types where possible, otherwise {@code NV}. {@code NV.toString} of a URI returns the
 * uri as a string so {@code NV} works naturally in Java/Nashorn. 
 * 
 * @see #fromNodeValue
 * @see #toNodeValue
 */

public class NV implements RDFJS {
    //  Six data types that are primitives in JavaScript:
    //           Boolean
    //           Null
    //           Undefined
    //           Number
    //           String
    //           Symbol (new in ECMAScript 6; not in Nashorn/Java8).
    //       and Object
    
    private NodeValue nv;
    /** 
     * Enable restoring integer from doubles.
     */
    private final static boolean narrowDoubles = true;
    /**
     * Map an ARQ {@link NodeValue} to java/Nashorn representation of a JavaScript object.
     * Native JavaScript types supported are null, string, number and boolean.
     * Otherwise a {@link NV} is returned.
     */
    public static Object fromNodeValue(NodeValue nv) {
        if ( nv == null )
            return null;
        if ( nv.isString() )
            return nv.getString();
        if ( nv.isNumber() ) {
            if ( nv.isInteger())
                return nv.getInteger();
            if ( nv.isDecimal() )
                return nv.getDecimal();
            if ( nv.isDouble() )
                return nv.getDouble();
        }
        if ( nv.isBoolean() )
            return nv.getBoolean();
        return new NV(nv);
    }
    /**
     * Map a java/Nashorn representation of a JavaScript object to an ARQ
     * {@link NodeValue}. Identified types are null, string, number and boolean and also
     * {@code NV} returned by the JavaScript code.
     */
    public static NodeValue toNodeValue(Object r) {
        if ( r == null )
            return null;
        if ( r instanceof NV )
            return ((NV)r).nv();
        if ( r instanceof NodeValue )
            return (NodeValue)r;
        // May not be a String.  String.toString is very efficient!
        // https://docs.oracle.com/javase/8/docs/technotes/guides/scripting/prog_guide/javascript.html#A1147390
        if ( r instanceof CharSequence )
            return NodeValue.makeString(((CharSequence)r).toString());
        if ( r instanceof Number )
            return number2value((Number)r);
        if ( r instanceof Boolean ) {
            return NodeValue.makeBoolean((Boolean)r); 
        }
        if ( r instanceof URI ) {
            Node n = NodeFactory.createURI(((URI)r).toString());
            return NodeValue.makeNode(n); 
        }
        throw new ExprEvalException("Can't convert '"+r+"' to a NodeValue.  r is of class "+r.getClass().getName());  
    }
    
    // Convert the numeric values that Nashorn can return.
    static NodeValue number2value(Number r) {
        if ( r instanceof Integer )
            return NodeValue.makeInteger((Integer)r);
        if ( r instanceof Long )
            return NodeValue.makeInteger((Long)r);
        if ( r instanceof Double ) {
            double d = (Double)r;
            if ( narrowDoubles && Double.isFinite(d)) {
                try {
                    long x = (long)d;
                    // Check for loss in conversion
                    if ( x == d )
                        return NodeValue.makeInteger(x);
                    // If is a very large integer (larger than long)?
                    if ( d == Math.floor(d) && Double.isFinite(d) ) {
                        BigInteger big = new BigInteger(Double.toString(x));
                        return NodeValue.makeInteger(big); 
                    }
                    if ( false ) {
                        // This always works but it is confusing decimal and double.
                        // Leave doubles as doubles.
                        return NodeValue.makeDecimal(d);
                    }
                } catch (NumberFormatException ex) {
                    // Shouldn't happen.
                    throw new ExprEvalException("Bad number format", ex);
                }
            }
            return NodeValue.makeDouble(d);
        }
        // These are carried NV.value returns.
        if ( r instanceof BigInteger )
            return NodeValue.makeInteger((BigInteger)r); 
        if ( r instanceof BigDecimal )
            return NodeValue.makeDecimal((BigDecimal)r); 
        
        throw new ExprEvalException("Unknown return type for number: "+r); 
    }
    
    // ------ Object NV
    
    // Javascript names and RDF extras.
    public boolean isURI() { return nv.isIRI(); }
    public boolean isBlank() { return nv.isBlank(); }
    public boolean isNumber() { return nv.isNumber(); }
    public boolean isLiteral() { return nv.isLiteral(); }

    // -- rdfjs
    @Override
    public String getTermType() {
        if ( isURI() )
            return "NamedNode";
        if ( isBlank() )
            return "BlankNode";
        if ( isLiteral() )
            return "Literal";
        return null; 
    }

    @Override
    public String getValue() { 
        if ( isURI() )
            return getUri();
        if ( isBlank() )
            return getLabel();
        if ( isLiteral() )
            return getLex();
        return null; 
    }
    // -- rdfjs

    public String getLabel()    { return nv.asNode().getBlankNodeLabel(); }
    public String getDT()       { return nv.getDatatypeURI(); }
    public String getDatatype() { return nv.getDatatypeURI(); }
    public String getLanguage() { return nv.getLang(); }
    public String getLang()     { return nv.getLang().toLowerCase(); }
    public String getLex()      { return nv.getNode().getLiteralLexicalForm(); }
    public String getUri()      { return nv.getNode().getURI(); }

    public NV(NodeValue nv) {
        this.nv = nv;
    }
    public NodeValue nv() {
        return nv;
    }

    @Override
    public String toString() { return nv.asUnquotedString(); }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nv == null) ? 0 : nv.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        NV other = (NV)obj;
        if ( nv == null ) {
            if ( other.nv != null )
                return false;
        } else if ( !nv.equals(other.nv) )
            return false;
        return true;
    } 
    }