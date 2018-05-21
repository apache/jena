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

package org.apache.jena.sparql.function.library;

import java.nio.charset.StandardCharsets ;
import java.util.Base64 ;
import java.util.Locale ;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionBase2 ;

/** XPath and XQuery Functions and Operators 3.1
 * <p> 
 * {@code fn:collation-key($key as xs:string, $collation as xs:string) as xs:base64Binary}
 */
public class FN_CollationKey extends FunctionBase2 {

    // The function2 variant
    // Function1 is default collation -> codepoint. 
    
    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {
        //fn:collation-key($key as xs:string, $collation as xs:string) as xs:base64Binary
        if ( ! v1.isString() ) {}
        if ( ! v2.isString() ) {}
        String collation = v2 == null ? "" : v2.getString().toLowerCase(Locale.ROOT); 
        
        // The irony of using the lexical form of old rdf:plainLiteral (RDF 1.1 does not
        // need rdf:plainLiteral and rdf:plainLiteral should never appear in RDF)
        String x = v1.getString()+"@"+v2.getString();
        byte[] b = x.getBytes(StandardCharsets.UTF_8);
        String s = Base64.getMimeEncoder().encodeToString(b);
        return NodeValue.makeNode(s, XSDDatatype.XSDbase64Binary);
    }
}
