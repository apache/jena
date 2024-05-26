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

package org.apache.jena.commonsrdf;

import java.util.Optional;

import org.apache.commons.rdf.api.*;
import org.apache.jena.commonsrdf.impl.*;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.web.LangTag;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sys.JenaSystem;

/**
 * A set of utilities for moving between CommonsRDF and Jena.
 * <pre>
 * // A Jena Graph
 * org.apache.jena.graph.Graph jGraph = GraphFactory.createGraphMem();
 * // provide as a Commons RDF graph
 * org.apache.commons.rdf.api.graph = JenaCommonsRDF.fromJena(jGraph);
 * </pre>
 */
public class JenaCommonsRDF {
    static { JenaSystem.init(); }

    /** Convert a CommonsRDF RDFTerm to a Jena Node.
     * If the RDFTerm was from Jena originally, return that original object else
     * create a copy using Jena objects.
     */
    public static Node toJena(RDFTerm term) {
        if ( term == null )
            return null;

        if ( term instanceof JenaNode )
            return ((JenaNode)term).getNode();

        if ( term instanceof IRI )
            return NodeFactory.createURI(((IRI)term).getIRIString());

        if ( term instanceof Literal ) {
            Literal lit = (Literal)term;
            RDFDatatype dt = NodeFactory.getType(lit.getDatatype().getIRIString());
            String lang = lit.getLanguageTag().orElse("");
            lang = LangTag.canonical(lang);
            return NodeFactory.createLiteral(lit.getLexicalForm(), lang, dt);
        }

        if ( term instanceof BlankNode ) {
            String id = ((BlankNode)term).uniqueReference();
            return NodeFactory.createBlankNode(id);
        }
        conversionError("Not a concrete RDF Term: "+term);
        return null;
    }

    /** Convert a CommonsRDF Triple to a Jena Triple.
     * If the Triple was from Jena originally, return that original object else
     * create a copy using Jena objects.
     */
    public static org.apache.jena.graph.Triple toJena(Triple triple) {
        if ( triple instanceof JenaTriple )
            return ((JenaTriple)triple).getTriple();
        return org.apache.jena.graph.Triple.create(toJena(triple.getSubject()), toJena(triple.getPredicate()), toJena(triple.getObject()) );
    }

    /** Convert a CommonsRDF Quad to a Jena Quad.
     * If the Quad was from Jena originally, return that original object else
     * create a copy using Jena objects.
     */
    public static org.apache.jena.sparql.core.Quad toJena(Quad quad) {
        if ( quad instanceof JenaTriple )
            return ((JenaQuad)quad).getQuad();
        return org.apache.jena.sparql.core.Quad.create(toJena(quad.getGraphName()), toJena(quad.getSubject()), toJena(quad.getPredicate()), toJena(quad.getObject()) );
    }

    /** Public ?? */
    public static org.apache.jena.graph.Node toJena(Optional<BlankNodeOrIRI> graphName) {
        if ( ! graphName.isPresent() )
            return org.apache.jena.sparql.core.Quad.defaultGraphNodeGenerated;
        return toJena(graphName.get());
    }

    /** Public ?? */
    public static Node toJenaAny(RDFTerm term) {
        if ( term == null )
            return Node.ANY;
        return JenaCommonsRDF.toJena(term);
    }

    /** Public ?? */
    public static Node toJenaAny(Optional<BlankNodeOrIRI> term) {
        // Yes, this can be a null for "wildcard".
        if ( term == null )
            return Node.ANY;
        return JenaCommonsRDF.toJena(term);
    }

    /**
     * Convert a CommonsRDF Graph to a Jena Graph.
     * If the Graph was from Jena originally, return that original object else
     * create a copy using Jena objects.
     */
    public static org.apache.jena.graph.Graph toJena(Graph graph) {
        if ( graph instanceof JenaGraph )
            return ((JenaGraph)graph).getGraph();
        org.apache.jena.graph.Graph g = GraphFactory.createGraphMem();
        graph.stream().forEach(t->g.add(toJena(t)));
        return g;
    }

    /**
     * Convert a CommonsRDF Dataset to a Jena DatasetGraph.
     * If the Dataset was from Jena originally, return that original object else
     * create a copy using Jena objects.
     */
    public static org.apache.jena.sparql.core.DatasetGraph toJena(Dataset dataset) {
        if ( dataset instanceof JenaDataset )
            return ((JenaDataset)dataset).getDataset();
        org.apache.jena.sparql.core.DatasetGraph d = DatasetGraphFactory.createTxnMem();
        dataset.stream().forEach(q->d.add(toJena(q)));
        return d;
    }

    /** Adapt a CommonsRDF Syntax to a Jena {@link Lang} */
    public static Optional<Lang> toJena(RDFSyntax syntax) {
        return Optional.ofNullable(RDFLanguages.contentTypeToLang(syntax.mediaType()));
    }

    /** Adapt a Jena Lang to a CommonsRDF {@link RDFSyntax} */
    public static Optional<RDFSyntax> fromJena(final Lang lang) {
        return RDFSyntax.byMediaType(lang.getContentType().getContentTypeStr());
    }

    /** Adapt an existing Jena Node to CommonsRDF {@link RDFTerm}. */
    public static RDFTerm fromJena( Node node) {
        return JCR_Factory.fromJena(node);
    }

    /** Adapt an existing Jena Triple to CommonsRDF {@link Triple}. */
    public static Triple fromJena(org.apache.jena.graph.Triple triple) {
        return JCR_Factory.fromJena(triple);
    }

    /** Adapt an existing Jena Quad to CommonsRDF {@link Quad}. */
    public static Quad fromJena(org.apache.jena.sparql.core.Quad quad) {
        return JCR_Factory.fromJena(quad);
    }

    /**
     * Adapt an existing Jena Graph to CommonsRDF {@link Graph}.
     * This does not take a copy.
     * Changes to the CommonsRDF Graph are reflected in the jena graph.
     */
    public static Graph fromJena(org.apache.jena.graph.Graph graph) {
        return JCR_Factory.fromJena(graph);
    }

    /**
     * Adapt an existing Jena Graph to CommonsRDF {@link Graph}.
     * This does not take a copy.
     * Changes to the CommonsRDF Graph are reflected in the jena graph.
     */
    public static Dataset fromJena(org.apache.jena.sparql.core.DatasetGraph datasetGraph) {
        return JCR_Factory.fromJena(datasetGraph);
    }

    /** Convert from Jena {@link Node} to any RDFCommons implementation */
    public static RDFTerm fromJena(RDF factory, Node node) {
        if ( node.isURI() )
            return factory.createIRI(node.getURI());
        if ( node.isLiteral() ) {
            String lang = node.getLiteralLanguage();
            if ( lang != null && ! lang.isEmpty() )
                return factory.createLiteral(node.getLiteralLexicalForm(), lang);
            if ( node.getLiteralDatatype().equals(XSDDatatype.XSDstring) )
                return factory.createLiteral(node.getLiteralLexicalForm());
            IRI dt = factory.createIRI(node.getLiteralDatatype().getURI());
            return factory.createLiteral(node.getLiteralLexicalForm(), dt);
        }
        if ( node.isBlank() )
            return factory.createBlankNode(node.getBlankNodeLabel());
        throw new ConversionException("Node is not a concrete RDF Term: "+node);
    }

    /** Convert from Jena {@link org.apache.jena.graph.Triple} to any RDFCommons implementation */
   public static Triple fromJena(RDF factory, org.apache.jena.graph.Triple triple) {
        BlankNodeOrIRI subject = (BlankNodeOrIRI)(fromJena(factory, triple.getSubject()));
        IRI predicate = (IRI)(fromJena(factory, triple.getPredicate()));
        RDFTerm object = fromJena(factory, triple.getObject());
        return factory.createTriple(subject, predicate, object);
    }


   /** Convert from Jena {@link org.apache.jena.sparql.core.Quad} to any RDFCommons implementation */
   public static Quad fromJena(RDF factory, org.apache.jena.sparql.core.Quad quad) {
       BlankNodeOrIRI graph =  (BlankNodeOrIRI)(fromJena(factory, quad.getGraph()));
       BlankNodeOrIRI subject = (BlankNodeOrIRI)(fromJena(factory, quad.getSubject()));
       IRI predicate = (IRI)(fromJena(factory, quad.getPredicate()));
       RDFTerm object = fromJena(factory, quad.getObject());
       return factory.createQuad(graph, subject, predicate, object);
   }

   /** Convert from Jena to any RDFCommons implementation.
    *  This is a copy, even if the factory is a RDFJena.
    *  Use {@link #fromJena(org.apache.jena.graph.Graph)} for a wrapper.
    */
   public static Graph fromJena(RDF factory, org.apache.jena.graph.Graph graph) {
       Graph g = factory.createGraph();
       graph.find(Node.ANY, Node.ANY, Node.ANY).forEachRemaining( t-> {
           g.add(fromJena(factory, t) );
       });
       return g;
   }

   /** Create a {@link StreamRDF} that inserts into any RDFCommons implementation of Graph */
   public static StreamRDF streamJenaToCommonsRDF(RDF rft, Graph graph) {
       return new ToGraph(rft, graph);
   }

   /** Create a {@link StreamRDF} that inserts into any RDFCommons implementation of Graph */
   public static StreamRDF streamJenaToCommonsRDF(RDF rft, Dataset dataset) {
       return new ToDataset(rft, dataset);
   }

   public static void conversionError(String msg) {
        throw new ConversionException(msg);
    }
}

