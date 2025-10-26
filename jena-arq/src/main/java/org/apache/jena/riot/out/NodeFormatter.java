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

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.graph.Node;

/**
 * Processor for output of RDF terms. The operation {@link #format(AWriter, Node)}
 * formats a node and output on the {@link AWriter}.
 * <p>
 * All other operations assume their {@link Node} argument is the correct kind of
 * RDFterm for the operation.
 */
public interface NodeFormatter
{
    public void format(AWriter w, Node n);

    public void formatURI(AWriter w, Node n);
    public void formatURI(AWriter w, String uriStr);

    public void formatVar(AWriter w, Node n);
    public void formatVar(AWriter w, String name);

    public void formatBNode(AWriter w, Node n);
    public void formatBNode(AWriter w, String label);

    public void formatLiteral(AWriter w, Node n);

    /** Plain string / xsd:string (RDF 1.1) */
    public void formatLitString(AWriter w, String lex);

    /** String with language tag */
    public void formatLitLang(AWriter w, String lex, String langTag);

    /** String with language tag and base direction (RDF 1.2) */
    public void formatLitLangDir(AWriter w, String lex, String langTag, String direction);

    /**
     * Literal with datatype, not a simple literal, not an xsd:string (RDF 1.1), no
     * language tag or and base direction.
     */
    public void formatLitDT(AWriter w, String lex, String datatypeURI);

    public void formatTripleTerm(AWriter w, Node n);
    public void formatTripleTerm(AWriter w, Node subject, Node proedicate, Node object);
}
