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

package org.apache.jena.riot.system;

import java.util.Optional;

import com.apicatalog.rdf.*;
import com.apicatalog.rdf.spi.RdfProvider;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.NodeUtils;

/** Conversion to/from Titanium JSON-LD objects */
public class JenaTitanium {

    public static class JenaTitaniumException extends JenaException {
        public JenaTitaniumException(String msg) { super(msg); }
    }

    /** Translate a Jena {@link DatasetGraph} to a Titanium JSON-LD dataset */
    public static RdfDataset convert(DatasetGraph dataset) {
        RdfProvider provider = RdfProvider.provider();
        RdfDataset rdfDataset = provider.createDataset();
        NodeToLabel labelMapping = NodeToLabel.createScopeByDocument();
        dataset.find().forEachRemaining(quad->{
            RdfResource subject = resource(provider, labelMapping, quad.getSubject());
            RdfResource predicate = resource(provider, labelMapping, quad.getPredicate());
            RdfValue object = nodeToValue(provider, labelMapping, quad.getObject());

            if ( quad.isDefaultGraph() ) {
                RdfTriple t = provider.createTriple(subject, predicate, object);
                rdfDataset.add(t);
            }
            else {
                RdfNQuad q = provider.createNQuad(subject, predicate, object,
                                                  resource(provider, labelMapping, quad.getGraph()));
                rdfDataset.add(q);
            }
        });
        return rdfDataset;
    }

    /** Translate a Titanium JSON-LD dataset to a {@link DatasetGraph} */
    public static DatasetGraph convert(RdfDataset dataset) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        StreamRDF dest = StreamRDFLib.dataset(dsg);
        convert(dataset, dest);
        return dsg;
    }

    /** Translate a Titanium JSON-LD dataset to a {@link StreamRDF} */
    public static void convert(RdfDataset dataset, StreamRDF output) {
        RdfProvider provider = RdfProvider.provider();
        FactoryRDF factory = new FactoryRDFStd();

        for ( RdfNQuad rdfQuad : dataset.toList() ) {
            Optional<RdfResource> gn = rdfQuad.getGraphName();
            RdfResource subj = rdfQuad.getSubject();
            RdfResource pred = rdfQuad.getPredicate();
            RdfValue obj= rdfQuad.getObject();

            Node g = valueToNode(factory, gn);
            Node s = valueToNode(factory, subj);
            Node p = valueToNode(factory, pred);
            Node o = valueToNode(factory, obj);

            if ( g == null )
                output.triple(Triple.create(s, p, o));
            else
                output.quad(Quad.create(g, s, p, o));
        }
    }

    private static Node valueToNode(FactoryRDF factory, Optional<RdfResource> value) {
        if ( value.isEmpty() )
            return null;
        return valueToNode(factory, value.get());
    }

    private static Node valueToNode(FactoryRDF factory, RdfValue value) {
        if ( value.isBlankNode() )
            return factory.createBlankNode(value.getValue());

        if ( value.isIRI() )
            return factory.createURI(value.getValue());

        if ( value.isLiteral() ) {
            RdfLiteral literal = (RdfLiteral)value;
            String lex = literal.getValue();
            String dt = literal.getDatatype();
            RDFDatatype datatype = NodeFactory.getType(dt);
            Optional<String> lang = literal.getLanguage();
            if ( lang.isPresent() )
                return factory.createLangLiteral(lex, lang.get());
            return factory.createTypedLiteral(lex, datatype);
        }
        throw new JenaTitaniumException("Not recognized: "+value);
    }

    private static RdfResource resource(RdfProvider provider, NodeToLabel labelMapping, Node node) {
        if ( node.isBlank() ) {
            String s = labelMapping.get(null, node);
            return provider.createBlankNode(s);
        }

        if ( node.isURI() )
            return provider.createIRI(node.getURI());
        throw new JenaTitaniumException("Can not convert to an RdfResource : "+node);
    }

    private static RdfValue nodeToValue(RdfProvider provider, NodeToLabel labelMapping, Node node) {
        if ( node.isBlank() || node.isURI() )
                return resource(provider, labelMapping, node);

        if ( node.isLiteral()) {
            String lex = node.getLiteralLexicalForm();
            if ( NodeUtils.hasLang(node) )
                return provider.createLangString(lex, node.getLiteralLanguage());
            return provider.createTypedString(lex, node.getLiteralDatatypeURI());
        }
        throw new JenaTitaniumException("Can not be converted: "+node);
    }
}
