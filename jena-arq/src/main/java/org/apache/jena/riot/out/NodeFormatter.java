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

package org.apache.jena.riot.out;

import java.io.Writer ;

import com.hp.hpl.jena.graph.Node ;

public interface NodeFormatter
{
    public void format(Writer w, Node n) ;

    /** Node is guaranteed to be a URI node */
    public void formatURI(Writer w, Node n) ;
    public void formatURI(Writer w, String uriStr) ;
    
    public void formatVar(Writer w, Node n) ;
    public void formatVar(Writer w, String name) ;
    
    /** Node is guaranteed to be a blank node */
    public void formatBNode(Writer w, Node n) ;
    public void formatBNode(Writer w, String label) ;
    
    /** Node is guaranteed to be a literal */
    public void formatLiteral(Writer w, Node n) ;
    
    /** Plain string / xsd:string (RDF 1.1) */
    public void formatLitString(Writer w, String lex) ;
    
    /** String with language tag */
    public void formatLitLang(Writer w, String lex, String langTag) ;

    /** Literal with datatype, not a simple literal, not an xsd:string (RDF 1.1), no language tag. */
    public void formatLitDT(Writer w, String lex, String datatypeURI) ;
    
}
