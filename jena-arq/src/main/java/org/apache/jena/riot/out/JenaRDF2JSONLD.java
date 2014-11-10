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

package org.apache.jena.riot.out;

import java.util.Iterator ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.SyntaxLabels ;

import com.github.jsonldjava.core.JsonLdError ;
import com.github.jsonldjava.core.RDFDataset ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Convert from JSON-LD syntax to JSON-LD internal representation of a dataset, using Jena Quads/Nodes etc */
class JenaRDF2JSONLD implements com.github.jsonldjava.core.RDFParser {
    NodeToLabel labels = SyntaxLabels.createNodeToLabel() ;

    @Override
    public RDFDataset parse(Object object) throws JsonLdError {
        RDFDataset result = new RDFDataset() ;
        if ( object instanceof DatasetGraph )
        {
            DatasetGraph dsg = (DatasetGraph)object ;

            Iterator<Quad> iter = dsg.find() ;
            for ( ; iter.hasNext() ; )
            {
                Quad q = iter.next() ;
                Node s = q.getSubject() ;
                Node p = q.getPredicate() ;
                Node o = q.getObject() ;
                Node g = q.getGraph() ;
                
                String gq = null ;
                if ( g != null && ! Quad.isDefaultGraph(g) ) {
                    gq = blankNodeOrIRIString(g) ;
                    if ( gq == null )
                        throw new RiotException("Graph node is not a URI or a blank node") ;
                }
                
                String sq = blankNodeOrIRIString(s) ;
                if ( sq == null )
                    throw new RiotException("Subject node is not a URI or a blank node") ;
                
                String pq = p.getURI() ;
                if ( o.isLiteral() )
                {
                    String lex = o.getLiteralLexicalForm() ; 
                    String lang = o.getLiteralLanguage() ;
                    String dt = o.getLiteralDatatypeURI() ;
                    if (lang != null && lang.length() == 0)
                    {
                        lang = null ;
                        //dt = RDF.getURI()+"langString" ;
                    }
                    if (dt == null )
                        dt = XSDDatatype.XSDstring.getURI() ;

                    result.addQuad(sq, pq, lex, dt, lang, gq) ;
                }
                else
                {
                    String oq = blankNodeOrIRIString(o) ;
                    result.addQuad(sq, pq, oq, gq) ;
                }
            }
        }                
        else
            Log.warn(JenaRDF2JSONLD.class, "unknown") ;
        return result ;
    }

    private String blankNodeOrIRIString(Node x)
    {
        if ( x.isURI() ) return x.getURI() ;
        if ( x.isBlank() )
            return labels.get(null,  x) ;
        return null ;
    }
}    
