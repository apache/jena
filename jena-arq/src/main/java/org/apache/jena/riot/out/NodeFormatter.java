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



import org.apache.jena.atlas.io.AWriter ;

import com.hp.hpl.jena.graph.Node ;

public interface NodeFormatter
{
    public void format(AWriter w, Node n) ;

    /** Node is guaranteed to be a URI node */
    public void formatURI(AWriter w, Node n) ;
    public void formatURI(AWriter w, String uriStr) ;
    
    public void formatVar(AWriter w, Node n) ;
    public void formatVar(AWriter w, String name) ;
    
    /** Node is guaranteed to be a blank node */
    public void formatBNode(AWriter w, Node n) ;
    public void formatBNode(AWriter w, String label) ;
    
    /** Node is guaranteed to be a literal */
    public void formatLiteral(AWriter w, Node n) ;
    
    /** Plain string / xsd:string (RDF 1.1) */
    public void formatLitString(AWriter w, String lex) ;
    
    /** String with language tag */
    public void formatLitLang(AWriter w, String lex, String langTag) ;

    /** Literal with datatype, not a simple literal, not an xsd:string (RDF 1.1), no language tag. */
    public void formatLitDT(AWriter w, String lex, String datatypeURI) ;
    
}
