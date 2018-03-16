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

package org.apache.jena.sparql.lib;

import org.apache.jena.atlas.json.*;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.js.NV;

/**
 * General converting of {@link Node}s to JSON.
 * <p>
 * This is a one way, lossy, conversion from an RDF Term to the best available JSON value.
 * <p> 
 * This is not SPARQL results in JSON ({@locde application/result-sets+json}).
 * 
 * <table>
 * <tr><th>RDF Term</th><th>JSON value</th></tr>
 * <tr><td>URI</td><td>String</td></tr>
 * <tr><td>xsd:string</td><td>String</td></tr>
 * <tr><td>rdf:langString</td><td>String (no {@literal @}lang)</td></tr>
 * <tr><td>XSD numeric</td><td>JSON number</td></tr>
 * <tr><td>xsd:boolean</td><td>boolean</td></tr>
 * <tr><td>Literal, other datatype</td><td>String, no ^^</td></tr>
 * <tr><td>Unbound</td><td>JSON null</td></tr>
 * </table>
 * 
 * @see NV for conversion to and from {@link NodeValue}s for function argument and results. 
 */

public class RDFTerm2Json {
    //  Six data types that are primitives in JavaScript:
    //           Boolean
    //           Null
    //           Undefined
    //           Number
    //           String
    //           Symbol (new in ECMAScript 6; not in Nashorn/Java8).
    //       and Object
    
    public static JsonValue fromNode(Node node) {
        if ( node == null )
            return JsonNull.instance;
        if ( node.isURI() )
            return new JsonString(node.getURI());
        if ( node.isBlank() ) {
            Node node2 = RiotLib.blankNodeToIri(node);
            return new JsonString(node2.getURI());
        }
        if ( node.isVariable() ) 
            return new JsonString("?"+node.getName());

        // Literals.
        if ( Util.isSimpleString(node) || Util.isLangString(node) ) 
            return new JsonString(node.getLiteralLexicalForm());

        // Includes well-formed testing.
        RDFDatatype dt = node.getLiteralDatatype();
        
        NodeValue nv = NodeValue.makeNode(node);
        if ( nv.isNumber() ) {
            // Order matters here to find the narrowest choice.
            if ( nv.isInteger() ) {
                try { 
                    return JsonNumber.value(nv.getInteger().longValueExact());
                } catch (ArithmeticException ex) {
                    // Bad in some way : scale maybe.
                    // Try as decimal.
                }
            }
            if ( nv.isDecimal() )
                return JsonNumber.value(nv.getDecimal());
            if ( nv.isDouble() )
                return JsonNumber.value(nv.getDouble());
            if ( nv.isFloat() )
                return JsonNumber.value(nv.getFloat());
            // Drop through.
        } else if ( nv.isBoolean()  ) {
            return new JsonBoolean(nv.getBoolean());
        }

        // else
        return new JsonString(node.getLiteralLexicalForm());
    }
}
