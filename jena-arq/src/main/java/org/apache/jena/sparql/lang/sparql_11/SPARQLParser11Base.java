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

package org.apache.jena.sparql.lang.sparql_11;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public abstract class SPARQLParser11Base extends Legacy11.SPARQLParserBase {

    protected Node createLiteral(String lexicalForm, String langTag, String datatypeURI) {
        Node n = null;
        // Can't have type and lang tag in parsing.
        if ( datatypeURI != null ) {
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
            n = NodeFactory.createLiteralDT(lexicalForm, dType);
        } else if ( langTag != null && !langTag.isEmpty() )
            n = NodeFactory.createLiteralLang(lexicalForm, langTag);
        else
            n = NodeFactory.createLiteralString(lexicalForm);
        return n;
    }
}
