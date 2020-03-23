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

import org.apache.jena.JenaRuntime ;
import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQInternalErrorException ;

/**
 * Provide implementations of the operations of {@link NodeFormatter} in terms
 * of core operations for each node type. N-Triples/N-Quads format.
 */
public abstract class NodeFormatterBase implements NodeFormatter
{
    @Override
    public void format(AWriter w, Node n)
    {
        // Can't use a fixed visitor because of the writer?
        if ( n.isBlank() )
            formatBNode(w, n) ;
        else if ( n.isURI() )
            formatURI(w, n) ;
        else if ( n.isLiteral() )
            formatLiteral(w, n) ;
        else if ( n.isVariable() )
            formatVar(w, n) ;
        else if ( Node.ANY.equals(n) )
            w.print("ANY") ;
        else if ( n instanceof Node_Triple )
            formatNodeTriple(w, (Node_Triple)n);
//        else if ( n instanceof Node_Graph )
//            formatNodeGraph(w, (Node_Graph)n);
        else
            throw new ARQInternalErrorException("Unknown node type: "+n) ;
    }

    private void formatNodeTriple(AWriter w, Node_Triple n) {
        // XXX Improve
        Triple t = n.get();
        w.print("<< ");
        format(w, t.getSubject());
        w.print(" ");
        format(w, t.getPredicate());
        w.print(" ");
        format(w, t.getObject());
        w.print(" >>");
    }

    @Override
    public void formatURI(AWriter w, Node n)         { formatURI(w, n.getURI()) ; }

    @Override
    public void formatBNode(AWriter w, Node n)       { formatBNode(w, n.getBlankNodeLabel()) ; }

    @Override
    public void formatLiteral(AWriter w, Node n)
    {
        RDFDatatype dt = n.getLiteralDatatype() ;
        String lang = n.getLiteralLanguage() ;
        String lex = n.getLiteralLexicalForm() ;

        if ( lang != null && ! lang.equals("") ) {
            formatLitLang(w, lex, lang) ;
        } else if ( dt == null ) {
            // RDF 1.0, simple literal.
            formatLitString(w, lex) ;
        } else if ( JenaRuntime.isRDF11 && dt.equals(XSDDatatype.XSDstring) ) {
            // RDF 1.1, xsd:string - output as short string.
            formatLitString(w, lex) ;
        } else {
            // Datatype, no language tag, not short string.
            formatLitDT(w, lex, dt.getURI()) ;
        }
    }

    @Override
    public void formatVar(AWriter w, Node n)         { formatVar(w, n.getName()) ; }
}
