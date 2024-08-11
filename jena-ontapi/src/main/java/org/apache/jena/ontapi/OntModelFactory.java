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

package org.apache.jena.ontapi;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontapi.common.OntConfigs;
import org.apache.jena.ontapi.common.OntObjectPersonalityBuilder;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.impl.UnionGraphImpl;
import org.apache.jena.ontapi.impl.repositories.OntUnionGraphRepository;
import org.apache.jena.ontapi.impl.repositories.PersistentGraphRepository;
import org.apache.jena.ontapi.model.OntID;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.util.Objects;

/**
 * A factory to produce {@link OntModel Ontolofy model}s.
 */
public class OntModelFactory {

    /**
     * A {@code PrefixMapping} that contains the "standard" for OWL2 prefixes we know about, viz rdf, rdfs, xsd, and owl.
     */
    public static final PrefixMapping STANDARD = PrefixMapping.Factory.create()
            .setNsPrefix("owl", OWL2.NS)
            .setNsPrefix("rdfs", RDFS.uri)
            .setNsPrefix("rdf", RDF.uri)
            .setNsPrefix("xsd", XSD.NS)
            .lock();

    static {
        init();
    }

    /**
     * Initializes Jena System.
     */
    public static void init() {
        JenaSystem.init();
    }

    /**
     * Creates default (in-memory) graph implementation.
     *
     * @return {@code Graph}
     */
    public static Graph createDefaultGraph() {
        return GraphMemFactory.createGraphMem();
    }

    /**
     * Wraps the given {@code base} graph as {@link UnionGraph}
     *
     * @param base {@code Graph}
     * @return {@link UnionGraph}
     */
    public static UnionGraph createUnionGraph(Graph base) {
        // non-distinct graph since requiring distinct is expensive;
        // usually OWL imports-closure does not contain duplicate data and
        // cyclic imports are resolved by the UnionGraph itself and do non lead to duplication data
        return new UnionGraphImpl(base, false);
    }

    /**
     * Creates default (in-memory) RDF Model implementation.
     *
     * @return {@link Model}
     * @see org.apache.jena.rdf.model.ModelFactory#createDefaultModel()
     */
    public static Model createDefaultModel() {
        return createDefaultModel(createDefaultGraph());
    }

    /**
     * Creates default RDF Model implementation wrapping the given graph.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return {@link Model}
     * @see org.apache.jena.rdf.model.ModelFactory#createDefaultModel()
     */
    public static Model createDefaultModel(Graph graph) {
        return new ModelCom(Objects.requireNonNull(graph));
    }

    /**
     * Creates a fresh in-memory Ontology RDF Model with default personalities.
     *
     * @return {@link OntModel}
     */
    public static OntModel createModel() {
        return createModel(createDefaultGraph());
    }

    /**
     * Creates an Ontology RDF Model wrapper around the given graph with default personalities.
     *
     * @param graph {@link Graph}
     * @return {@link OntModel}
     */
    public static OntModel createModel(Graph graph) {
        return createModel(graph, OntSpecification.OWL2_DL_MEM_BUILTIN_RDFS_INF).setNsPrefixes(STANDARD);
    }

    /**
     * Creates an Ontology RDF Model wrapper around the given graph with given personalities.
     *
     * @param graph       {@link Graph}
     * @param personality {@link OntPersonality}
     * @return {@link OntModel}
     */
    public static OntModel createModel(Graph graph, OntPersonality personality) {
        OntPersonality withBuiltinHierarchySupport = OntObjectPersonalityBuilder.from(personality)
                .setConfig(OntConfigs.OWL2_CONFIG.setTrue(OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT))
                .build();
        return createModel(graph, new OntSpecification(withBuiltinHierarchySupport, null)).setNsPrefixes(STANDARD);
    }

    /**
     * Creates an Ontology Model according to the specified specification.
     *
     * @param spec {@link OntSpecification}
     * @return {@link OntModel}
     */
    public static OntModel createModel(OntSpecification spec) {
        return createModel(createDefaultGraph(), spec).setNsPrefixes(STANDARD);
    }

    /**
     * Creates an Ontology Model according to the specified specification.
     *
     * @param graph {@link Graph} (base graph)
     * @param spec  {@link OntSpecification}
     * @return {@link OntModel}
     */
    public static OntModel createModel(Graph graph, OntSpecification spec) {
        Objects.requireNonNull(graph);
        ReasonerFactory reasonerFactory = Objects.requireNonNull(spec).getReasonerFactory();
        if (reasonerFactory == null) {
            return new OntGraphModelImpl(Graphs.makeOntUnionFrom(graph, OntModelFactory::createUnionGraph), spec.getPersonality());
        }
        return createModel(graph, spec.getPersonality(), reasonerFactory.create(null));
    }

    /**
     * Creates an {@link OntModel Ontology Model} which is {@link org.apache.jena.rdf.model.InfModel Inference Model}.
     * The specified graph and its subgraphs (if any) must not be {@link InfGraph}.
     *
     * @param graph       {@link Graph}
     * @param personality {@link OntPersonality}
     * @param reasoner    {@link Reasoner}
     * @return {@link OntModel}
     * @see OntModel#asInferenceModel()
     */
    public static OntModel createModel(Graph graph, OntPersonality personality, Reasoner reasoner) {
        Objects.requireNonNull(graph);
        Objects.requireNonNull(reasoner);
        Objects.requireNonNull(personality);
        if (Graphs.dataGraphs(graph).anyMatch(it -> it instanceof InfGraph)) {
            throw new IllegalArgumentException("InfGraph in the hierarchy detected");
        }
        UnionGraph unionGraph = Graphs.makeOntUnionFrom(graph, OntModelFactory::createUnionGraph);
        InfGraph infGraph = reasoner.bind(unionGraph);
        return new OntGraphModelImpl(infGraph, personality);
    }

    /**
     * Creates Ontology Model associated with {@link OntSpecification#OWL2_DL_MEM_BUILTIN_RDFS_INF} spec.
     * The {@code repository} manages all the dependencies.
     * See {@link #createModel(Graph, OntSpecification, GraphRepository)}.
     *
     * @param uri        String, subject of {@code uri rdf:type owl:Ontology} statement,
     *                   can be {@code null} for anonymous ontology
     * @param repository {@link GraphRepository}
     * @return {@link OntModel}
     */
    public static OntModel createModel(String uri, GraphRepository repository) {
        return createModel(
                createOntGraph(uri != null ? NodeFactory.createURI(uri) : NodeFactory.createBlankNode(), repository),
                OntSpecification.OWL2_DL_MEM_BUILTIN_RDFS_INF,
                repository
        ).setNsPrefixes(STANDARD);
    }

    /**
     * Creates an anonymous Ontology Model according to the specified specification.
     * The {@code repository} manages all the dependencies.
     * Note that if {@code repository} is {@link PersistentGraphRepository},
     * encapsulated {@link GraphMaker} will be used to create graphs.
     * See also {@link #createModel(Graph, OntSpecification, GraphRepository)}.
     *
     * @param spec       {@link OntSpecification}
     * @param repository {@link GraphRepository}
     * @return {@link OntModel}
     */
    public static OntModel createModel(OntSpecification spec, GraphRepository repository) {
        return createModel(null, spec, repository).setNsPrefixes(STANDARD);
    }

    /**
     * Creates an Ontology Model according to the specified specification.
     * The {@code repository} manages all the dependencies (imports closure).
     * <p>
     * Note that for consistency it is necessary to work only
     * through the {@link OntModel} or {@link UnionGraph} interfaces.
     * Working directly with the {@link UnionGraph#getBaseGraph()} or {@code repository} may break the state.
     * Imports closure control is performed via {@link UnionGraph.Listener},
     * any ontological graphs in the {@code repository} is wrapped as {@link UnionGraph},
     * {@link InfGraph} are not stored in the {@code repository}.
     * When adding subgraph using the {@link UnionGraph#addSubGraph(Graph)} method
     * a statement {@code a owl:import b} will be added.
     * In turns, adding a statement {@code a owl:import b} will cause adding a subgraph.
     * If a subgraph cannot be found in the {@code repository},
     * an empty ontology graph will be associated with the corresponding {@code owl:import}.
     * The specified graph and its subgraphs (if any) must not be {@link InfGraph}.
     * Note that the method adds ontology headers to each subgraph of the specified graph, including itself.
     * Also note that attempt to change {@link OntID OntID}
     * will cause {@link OntJenaException.IllegalArgument}
     * if the ontology is in some other ontologies' import closure.
     * <p>
     * This method can also be used to retrieve {@link OntModel} from the {@code repository}:
     * it returns a new instance of {@link OntModel} wrapping the existing {@link UnionGraph}
     * if it is present in the {@code repository}.
     *
     * @param graph      {@link Graph} or {@code null} to create anonymous Graph automatically;
     *                   the instance (new or provided) will be wrapped as
     *                   {@link UnionGraph} (if it is not already {@link UnionGraph})
     * @param spec       {@link OntSpecification}
     * @param repository {@link GraphRepository}; will contain {@link UnionGraph}s
     * @return {@link OntModel}
     * @see org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository DocumentGraphRepository
     * @see PersistentGraphRepository
     */
    public static OntModel createModel(Graph graph,
                                       OntSpecification spec,
                                       GraphRepository repository) {
        Objects.requireNonNull(spec);
        Objects.requireNonNull(repository);
        if (Graphs.dataGraphs(graph).anyMatch(it -> it instanceof InfGraph)) {
            throw new IllegalArgumentException("InfGraph in the hierarchy detected");
        }
        if (graph == null) {
            graph = createOntGraph(NodeFactory.createBlankNode(), repository);
        }
        OntUnionGraphRepository ontUnionGraphRepository = new OntUnionGraphRepository(
                repository,
                OntModelFactory::createUnionGraph,
                n -> createOntGraph(n, repository),
                /*ignoreUnresolvedImports*/ true);
        UnionGraph union;
        if (graph instanceof UnionGraph) {
            union = (UnionGraph) graph;
            Graphs.flatHierarchy((UnionGraph) graph).forEach(it -> {
                Graphs.findOntologyNameNode(it.getBaseGraph()).orElseGet(() -> Graphs.createOntologyHeaderNode(it, null));
                ontUnionGraphRepository.put(it);
            });
        } else {
            Graphs.dataGraphs(graph).forEach(it -> {
                String name = Graphs.findOntologyNameNode(it)
                        .orElseGet(() -> Graphs.createOntologyHeaderNode(it, null))
                        .toString();
                repository.put(name, it);
            });
            union = ontUnionGraphRepository.put(Graphs.getPrimary(graph));
        }
        ReasonerFactory reasonerFactory = spec.getReasonerFactory();
        if (reasonerFactory == null) {
            return new OntGraphModelImpl(union, spec.getPersonality());
        }
        InfGraph inf = reasonerFactory.create(null).bind(union);
        return new OntGraphModelImpl(inf, spec.getPersonality());
    }

    /**
     * Finds {@link OntModel} in the {@code repository},
     * returning {@code null} if there is no ontology with the specified {@code uri}.
     * The method constructs new {@link OntModel} instance, wrapping the found {@link UnionGraph}.
     *
     * @param uri        ontology name:
     *                   object from the statement {@code <ont> owl:versionIri <name>} or
     *                   subject from the statement {@code <name> rdf:type owl:Ontology};
     *                   not {@code null}
     * @param spec       {@link OntSpecification}
     * @param repository {@link GraphRepository}
     * @return {@link OntModel} or {@code null} if there is no such ontology
     */
    public static OntModel getModelOrNull(String uri, OntSpecification spec, GraphRepository repository) {
        return getModelOrNull(NodeFactory.createURI(Objects.requireNonNull(uri)), spec, repository);
    }

    /**
     * Finds {@link OntModel} in the {@code repository},
     * returning {@code null} if there is no ontology with the specified {@code name}.
     * The method constructs new {@link OntModel} instance, wrapping the found {@link UnionGraph}.
     *
     * @param name       {@link Node} ontology name, URI or blank
     * @param spec       {@link OntSpecification}
     * @param repository {@link GraphRepository}
     * @return {@link OntModel} or {@code null} if there is no such ontology
     */
    public static OntModel getModelOrNull(Node name, OntSpecification spec, GraphRepository repository) {
        Objects.requireNonNull(spec);
        Objects.requireNonNull(repository);
        Objects.requireNonNull(name);
        if (!name.isURI() && !name.isBlank()) {
            throw new IllegalArgumentException("Ontology name must be URI or blank Node");
        }
        OntUnionGraphRepository ontUnionGraphRepository = new OntUnionGraphRepository(
                repository,
                OntModelFactory::createUnionGraph,
                n -> createOntGraph(n, repository),
                /*ignoreUnresolvedImports*/ true);
        if (!ontUnionGraphRepository.contains(name)) {
            return null;
        }
        UnionGraph union = ontUnionGraphRepository.get(name);
        ReasonerFactory reasonerFactory = spec.getReasonerFactory();
        if (reasonerFactory == null) {
            return new OntGraphModelImpl(union, spec.getPersonality());
        }
        InfGraph inf = reasonerFactory.create(null).bind(union);
        return new OntGraphModelImpl(inf, spec.getPersonality());
    }

    /**
     * Creates ontology graph.
     *
     * @param name       {@link Node}, not {@code null} - ontology header.
     * @param repository {@link GraphRepository}, not {@code null}
     * @return {@link Graph}
     */
    public static Graph createOntGraph(Node name, GraphRepository repository) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(repository);
        Graph res;
        if (repository instanceof PersistentGraphRepository) {
            res = ((PersistentGraphRepository) repository).getGraphMaker().createGraph(name.toString());
        } else {
            res = createDefaultGraph();
        }
        res.add(name, RDF.type.asNode(), OWL2.Ontology.asNode());
        return res;
    }

}
