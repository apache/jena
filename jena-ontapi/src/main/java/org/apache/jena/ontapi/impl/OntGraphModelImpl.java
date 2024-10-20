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

package org.apache.jena.ontapi.impl;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.PersonalityConfigException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.UnionGraph;
import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.common.OntEnhGraph;
import org.apache.jena.ontapi.common.OntEnhNodeFactories;
import org.apache.jena.ontapi.common.OntPersonalities;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.impl.objects.OntClassImpl;
import org.apache.jena.ontapi.impl.objects.OntDataRangeImpl;
import org.apache.jena.ontapi.impl.objects.OntDisjointImpl;
import org.apache.jena.ontapi.impl.objects.OntFacetRestrictionImpl;
import org.apache.jena.ontapi.impl.objects.OntIndividualImpl;
import org.apache.jena.ontapi.impl.objects.OntListImpl;
import org.apache.jena.ontapi.impl.objects.OntObjectImpl;
import org.apache.jena.ontapi.impl.objects.OntSWRLImpl;
import org.apache.jena.ontapi.impl.objects.OntStatementImpl;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntFacetRestriction;
import org.apache.jena.ontapi.model.OntID;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntList;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntSWRL;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.ontapi.utils.OntModels;
import org.apache.jena.ontapi.utils.StdModels;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.IteratorFactory;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of a model that can process general ontologies in OWL and similar languages.
 * Class {@link OntPersonality} is responsible for the configuration of the model.
 * Also see {@link OntModelControls} - a set of settings,
 * that can be accessed through {@link OntPersonality#getConfig()}.
 *
 * @see UnionGraph
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class OntGraphModelImpl extends ModelCom implements OntModel, OntEnhGraph, InfModel {

    // the model's types mapper
    protected final Map<String, RDFDatatype> dtTypes = new HashMap<>();
    // to control RDF recursion while casting a node to an RDF view, see #fetchNodeAs(Node, Class)
    private final ThreadLocal<Set<Node>> visited = ThreadLocal.withInitial(HashSet::new);
    // Cached deductions model
    private Model deductionsModel = null;
    // collection of entity types, used when list entities
    private final Set<Class<? extends OntEntity>> supportedEntityTypes;
    // a cache with values of arbitrary nature, which can be used for various purposes,
    // e.g., as a storage of reserved nodes when construct OntObjects
    public final Map<String, Object> propertyStore = new HashMap<>();

    public OntGraphModelImpl(UnionGraph graph, OntPersonality personality) {
        this((Graph) graph, personality);
    }

    public OntGraphModelImpl(InfGraph graph, OntPersonality personality) {
        this((Graph) graph, personality);
    }

    /**
     * This {@link OntModel} implementation wraps
     * only {@link UnionGraph} or {@link InfGraph} which in turn wraps {@link UnionGraph}.
     *
     * @param graph       {@link Graph}
     * @param personality {@link OntPersonality}
     */
    protected OntGraphModelImpl(Graph graph, OntPersonality personality) {
        super(makeGraph(graph), OntPersonality.asJenaPersonality(personality));
        this.supportedEntityTypes = OntEntity.TYPES.stream().filter(personality::supports).collect(Collectors.toSet());
    }

    protected static Graph makeGraph(Graph given) {
        Objects.requireNonNull(given);
        if (given instanceof InfGraph) {
            Graph raw = ((InfGraph) given).getRawGraph();
            if (raw instanceof UnionGraph) {
                return given;
            }
            throw new IllegalArgumentException(
                    "The specified InfGraph does not wrap UnionGraph, instead it wraps " + raw.getClass().getSimpleName()
            );
        }
        if (given instanceof UnionGraph) {
            return given;
        }
        throw new IllegalArgumentException(
                "The specified graph is not UnionGraph or InfGraph: " + given.getClass().getSimpleName()
        );
    }

    /**
     * Lists all {@code OntObject}s for the given {@code OntGraphModelImpl}.
     *
     * @param m    {@link OntGraphModelImpl} the impl to cache
     * @param type {@link Class} the type of {@link OntObject}, not null
     * @param <M>  a subtype of {@link EnhGraph} and {@link OntEnhGraph}
     * @param <O>  subtype of {@link OntObject}
     * @return an {@link ExtendedIterator Extended Iterator} of {@link OntObject}s
     */
    @SuppressWarnings("unchecked")
    public static <M extends EnhGraph & OntEnhGraph, O extends OntObject> ExtendedIterator<O> listOntObjects(
            M m,
            Class<? extends O> type) {
        OntPersonality p = m.getOntPersonality();
        if (p.supports(type)) {
            ExtendedIterator<?> res = p.getObjectFactory(type).iterator(m);
            return (ExtendedIterator<O>) res;
        } else {
            return NullIterator.instance();
        }
    }

    /**
     * Filters {@code OntIndividual}s from the specified {@code ExtendedIterator}.
     *
     * @param model      {@code M}, not {@code null}
     * @param reserved   a {@code Set} of forbidden URIs,
     *                   that cannot be treated as {@link OntClass Ontology Class}es, not {@code null}
     * @param assertions {@link ExtendedIterator} of {@link Triple}s
     *                   with the {@link RDF#type rdf:type} as predicate, not {@code null}
     * @param <M>        a subtype of {@link OntModel} and {@link OntEnhGraph}
     * @return {@link ExtendedIterator} of {@link OntIndividual}s that are attached to the {@code model}
     */
    public static <M extends OntModel & OntEnhGraph> ExtendedIterator<OntIndividual> listIndividuals(M model,
                                                                                                     Set<String> reserved,
                                                                                                     ExtendedIterator<Triple> assertions) {
        Set<Triple> seen = new HashSet<>();
        boolean useSimplifiedClassChecking = model.getOntPersonality()
                .getConfig().getBoolean(OntModelControls.USE_SIMPLIFIED_TYPE_CHECKING_WHILE_LIST_INDIVIDUALS);
        boolean isRDFS = OntPersonalities.isRDFS(model.getOntPersonality());

        return assertions
                .mapWith(t -> {
                    // to speed up the process,
                    // the investigation (that includes TTO, PS, HP, GALEN, FAMILY and PIZZA ontologies),
                    // shows that the profit exists, and it is significant sometimes:
                    if (t.getObject().isURI() && reserved.contains(t.getObject().getURI())) {
                        return null;
                    }

                    // skip duplicates (an individual may have several class-assertions):
                    if (seen.remove(t)) {
                        return null;
                    }
                    if (!testIsClass(model, t.getObject(), useSimplifiedClassChecking, isRDFS)) {
                        return null;
                    }
                    return model.asStatement(t);
                })
                .filterKeep(s -> {
                    if (s == null) return false;
                    // an individual may have a factory with punning restrictions,
                    // so need to check its type also.
                    // this time does not cache in a model
                    OntIndividual i = s.getSubject().getAs(OntIndividual.class);
                    if (i == null) return false;

                    // update the set with duplicates to ensure the stream is distinct
                    ((OntIndividualImpl) i).listClasses()
                            .forEachRemaining(x -> {
                                if (s.getObject().equals(x)) {
                                    // skip this statement, otherwise all individuals fall into memory
                                    return;
                                }
                                seen.add(Triple.create(i.asNode(), RDF.Nodes.type, x.asNode()));
                            });
                    return true;
                })
                .mapWith(s -> s.getSubject(OntIndividual.class));
    }

    private static <M extends OntModel & OntEnhGraph> boolean testIsClass(
            M model,
            Node candidate,
            boolean simpleCheck,
            boolean isRDFS
    ) {
        if (simpleCheck) {
            if (isRDFS) {
                return testIsRDFSClass(model, candidate);
            }
            return testIsOWLClass(model, candidate);
        }
        OntClass clazz = model.safeFindNodeAs(candidate, OntClass.class);
        return clazz != null && clazz.canAsAssertionClass();
    }

    private static <M extends OntModel & OntEnhGraph> boolean testIsOWLClass(M model, Node candidate) {
        if (model.getOntPersonality().getPunnings().getNamedClasses().contains(candidate)) {
            return false;
        }
        if (model.getOntPersonality().getBuiltins().getNamedClasses().contains(candidate)) {
            return true;
        }
        return Graphs.hasOneOfType(candidate, model.getGraph(), Set.of(OWL2.Class.asNode(), OWL2.Restriction.asNode()));
    }

    private static boolean testIsRDFSClass(Model model, Node candidate) {
        return model.getGraph().contains(candidate, RDF.type.asNode(), RDFS.Class.asNode());
    }

    /**
     * Creates a {@code Stream} for a graph.
     *
     * @param graph    {@link Graph} to test
     * @param it       {@code ExtendedIterator} obtained from the {@code graph}
     * @param withSize if {@code true} attempts to include graph size as an estimated size of a future {@code Stream}
     * @param <X>      type of stream items
     * @return {@code Stream} of {@code X}s
     */
    private static <X> Stream<X> asStream(Graph graph,
                                          ExtendedIterator<X> it,
                                          boolean withSize) {
        int characteristics = Graphs.getSpliteratorCharacteristics(graph);
        long size = -1;
        if (withSize && Graphs.isSized(graph)) {
            size = Graphs.size(graph);
            characteristics = characteristics | Spliterator.SIZED;
        }
        return Iterators.asStream(it, size, characteristics);
    }

    public static void checkFeature(OntModel m, OntModelControls setting, String featureName) {
        OntJenaException.checkSupported(configValue(m, setting),
                "Feature " + featureName + " is disabled. " +
                        "Profile " + OntEnhGraph.asPersonalityModel(m).getOntPersonality().getName());
    }

    public static boolean configValue(OntModel m, OntModelControls setting) {
        OntConfig config = OntModels.config(m);
        return config != null && config.getBoolean(setting);
    }

    @Override
    public OntPersonality getOntPersonality() {
        return (OntPersonality) super.getPersonality();
    }

    /**
     * Returns {@code UnionGraph}.
     * This implementation requires that the underlying graph is union-graph or inf-graph.
     *
     * @return {@link UnionGraph}
     */
    public UnionGraph getUnionGraph() {
        Graph graph = super.getGraph();
        if (graph instanceof InfGraph) {
            Graph raw = ((InfGraph) graph).getRawGraph();
            if (raw instanceof UnionGraph) {
                return ((UnionGraph) raw);
            }
            throw new IllegalStateException(
                    "The encapsulated InfGraph does not wrap UnionGraph, instead it wraps " + raw.getClass().getSimpleName()
            );
        }
        if (graph instanceof UnionGraph) {
            return ((UnionGraph) graph);
        }
        throw new IllegalStateException(
                "The model wraps " + graph.getClass().getSimpleName() + ", that is illegal"
        );
    }

    @Override
    public Graph getBaseGraph() {
        return getUnionGraph().getBaseGraph();
    }

    @Override
    public Model getBaseModel() {
        return new ModelCom(getBaseGraph());
    }

    @Override
    public OntID getID() {
        checkType(OntID.class);
        Optional<OntID> id = id();
        if (id.isEmpty() && !configValue(this, OntModelControls.USE_GENERATE_ONTOLOGY_HEADER_IF_ABSENT_STRATEGY)) {
            throw new OntJenaException.IllegalState("No ontology header found, use OntModel#setID method instead");
        }
        return id.orElseGet(() -> setID(null));
    }

    @Override
    public OntIndividual createIndividual(String uri, OntClass type) {
        if (uri == null) {
            checkType(OntIndividual.Anonymous.class);
        }
        return OntModel.super.createIndividual(uri, type);
    }

    @Override
    public Optional<OntID> id() {
        checkType(OntID.class);
        return Graphs.ontologyNode(
                getBaseGraph(), configValue(this, OntModelControls.USE_CHOOSE_MOST_SUITABLE_ONTOLOGY_HEADER_STRATEGY)
        ).map(x -> getNodeAs(x, OntID.class));
    }

    @Override
    public OntID setID(String uri) {
        checkType(OntID.class);
        UnionGraph ug = getUnionGraph();
        UnionGraph.EventManager em = ug.getEventManager();
        em.notifyEvent(ug, OntModelEvent.startChangeIDEvent());
        Node id = Graphs.createOntologyHeaderNode(getBaseGraph(), uri);
        OntID res = getNodeAs(id, OntID.class);
        em.notifyEvent(ug, OntModelEvent.finishChangeIDEvent());
        return res;
    }

    @Override
    public OntGraphModelImpl addImport(OntModel m) {
        checkType(OntID.class);
        if (Objects.requireNonNull(m, "Null model specified.").getID().isAnon()) {
            throw new OntJenaException.IllegalArgument("Anonymous sub models are not allowed.");
        }
        String importsURI = m.id().map(OntID::getImportsIRI)
                .orElseThrow(() -> new IllegalArgumentException("Attempt to import unnamed ontology"));
        if (importsURI.equals(getID().getURI())) {
            throw new OntJenaException.IllegalArgument("Attempt to import ontology with the same name: " + importsURI);
        }
        if (hasImport(importsURI)) {
            throw new OntJenaException.IllegalArgument("Ontology <" + importsURI + "> is already in imports.");
        }
        Graph g = m.getGraph();
        if (g instanceof InfGraph) {
            g = ((InfGraph) g).getRawGraph();
        }
        if (g instanceof UnionGraph && !Graphs.isOntUnionGraph((UnionGraph) g,
                configValue(this, OntModelControls.USE_CHOOSE_MOST_SUITABLE_ONTOLOGY_HEADER_STRATEGY))) {
            throw new OntJenaException.IllegalArgument("Ontology <" + importsURI + "> has wrong structure.");
        }
        addImportModel(g, importsURI);
        return this;
    }

    @Override
    public boolean hasImport(OntModel m) {
        Objects.requireNonNull(m);
        return findImportAsRawModel(x -> Graphs.isSameBase(x.getGraph(), m.getGraph())).isPresent();
    }

    @Override
    public boolean hasImport(String uri) {
        return findImportAsRawModel(x -> Objects.equals(x.getID().getImportsIRI(), uri)).isPresent();
    }

    @Override
    public OntGraphModelImpl removeImport(OntModel m) {
        Objects.requireNonNull(m);
        Graph g = m.getGraph();
        Graph data;
        if (g instanceof InfGraph) {
            data = ((InfGraph) g).getRawGraph();
        } else {
            data = g;
        }
        findImportAsRawModel(it -> Graphs.isSameBase(it.getGraph(), data))
                .ifPresent(it -> removeImportModel(it.getGraph(), it.getID().getImportsIRI()));
        return this;
    }

    @Override
    public OntGraphModelImpl removeImport(String uri) {
        findImportAsRawModel(x -> Objects.equals(uri, x.getID().getImportsIRI()))
                .ifPresent(x -> removeImportModel(x.getGraph(), x.getID().getImportsIRI()));
        return this;
    }

    @Override
    public Stream<OntModel> imports() {
        return imports(getOntPersonality());
    }

    /**
     * Lists all top-level sub-models built with the given {@code personality}.
     *
     * @param personality {@link OntPersonality}, not {@code null}
     * @return {@code Stream} of {@link OntModel}s
     */
    public Stream<OntModel> imports(OntPersonality personality) {
        return Iterators.asStream(listImportModels(personality, getReasoner()));
    }

    /**
     * Finds a model impl from the internals using the given {@code filter}.
     * The returned model has no reasoner attached.
     *
     * @param filter {@code Predicate} to filter {@link OntGraphModelImpl}s
     * @return {@code Optional} around {@link OntGraphModelImpl}
     */
    protected Optional<OntGraphModelImpl> findImportAsRawModel(Predicate<OntGraphModelImpl> filter) {
        return Iterators.findFirst(listImportModels(getOntPersonality(), null).filterKeep(filter));
    }

    /**
     * Adds the graph-uri pair into the internals.
     *
     * @param graph {@link Graph}, not {@code null}
     * @param uri   String, not {@code null}
     */
    protected void addImportModel(Graph graph, String uri) {
        getUnionGraph().addSubGraph(graph);
        getID().addImport(uri);
        rebind();
    }

    /**
     * Removes the graph-uri pair from the internals.
     *
     * @param graph {@link Graph}, not {@code null}
     * @param uri   String, not {@code null}
     */
    protected void removeImportModel(Graph graph, String uri) {
        getUnionGraph().removeSubGraph(graph);
        getID().removeImport(uri);
        rebind();
    }

    /**
     * Lists {@link OntGraphModelImpl model impl}s with the specified {@code personality}
     * from the top tier of the imports' hierarchy.
     *
     * @param personality {@link OntPersonality}, not {@code null}
     * @param reasoner    {@link Reasoner}, can be {@code null}
     * @return <b>non-distinct</b> {@code ExtendedIterator} of {@link OntGraphModelImpl}s
     */
    public final ExtendedIterator<OntGraphModelImpl> listImportModels(OntPersonality personality, Reasoner reasoner) {
        return listImportGraphs().mapWith(u -> {
            Graph g;
            if (reasoner != null) {
                g = reasoner.bind(u);
            } else {
                g = u;
            }
            return new OntGraphModelImpl(g, personality);
        });
    }

    /**
     * Lists all top-level {@link UnionGraph}s of the model's {@code owl:import} hierarchy.
     * This model graph is not included.
     *
     * @return {@code ExtendedIterator} of {@link UnionGraph}s
     */
    protected final ExtendedIterator<UnionGraph> listImportGraphs() {
        UnionGraph u = getUnionGraph();
        ExtendedIterator<Graph> subGraphs;
        if (u instanceof UnionGraphImpl) {
            subGraphs = ((UnionGraphImpl) u).getSubGraphs().listGraphs();
        } else {
            subGraphs = WrappedIterator.create(u.subGraphs().iterator());
        }
        Set<String> imports = Graphs.getImports(u.getBaseGraph(),
                configValue(this, OntModelControls.USE_CHOOSE_MOST_SUITABLE_ONTOLOGY_HEADER_STRATEGY));
        return subGraphs
                .filterKeep(x -> x instanceof UnionGraph)
                .mapWith(x -> (UnionGraph) x)
                .filterKeep(it -> Graphs.findOntologyNameNode(it.getBaseGraph(),
                                configValue(this, OntModelControls.USE_CHOOSE_MOST_SUITABLE_ONTOLOGY_HEADER_STRATEGY))
                        .filter(Node::isURI)
                        .map(Node::getURI)
                        .filter(imports::contains)
                        .isPresent()
                );
    }

    /**
     * Gets the top-level {@link OntGraphModelImpl Ontology Graph Model impl}.
     * The returned model may contain import declarations, but cannot contain sub-models.
     * Be warned: any listeners, attached on the {@link #getGraph()}
     *
     * @return {@link OntGraphModelImpl}
     * @see #getBaseModel()
     */
    public OntGraphModelImpl getTopModel() {
        if (independent()) {
            return this;
        }
        OntPersonality personality = getOntPersonality();
        return new OntGraphModelImpl(new UnionGraphImpl(getBaseGraph(), false), personality);
    }

    /**
     * Determines whether this model is independent.
     *
     * @return {@code true} if this model is independent of others
     */
    @Override
    public boolean independent() {
        return !getUnionGraph().hasSubGraph();
    }

    /**
     * Answers {@code true} if the given entity is built-in.
     *
     * @param e   {@link OntEntity} object impl
     * @param <E> subtype of {@link OntObjectImpl} and {@link OntEntity}
     * @return boolean
     */
    public <E extends OntObjectImpl & OntEntity> boolean isBuiltIn(E e) {
        return getOntPersonality().getBuiltins()
                .get(e.objectType())
                .contains(e.asNode());
    }

    /**
     * Retrieves the stream of {@link OntObject Ontology Object}s.
     * The result object will be cached inside the model.
     * Note: this method may return non-distinct results,
     * this is determined by the reasoner and nature of the underlying graph;
     * for non-inference the standard memory graph implementation the result Stream is distinct.
     *
     * @param type {@link Class} the type of {@link OntObject}, not null
     * @param <O>  subtype of {@link OntObject}
     * @return {@code Stream} of {@link OntObject}s
     */
    @Override
    public <O extends OntObject> Stream<O> ontObjects(Class<? extends O> type) {
        return Iterators.asStream(listOntObjects(type), Graphs.getSpliteratorCharacteristics(getGraph()));
    }

    /**
     * Lists all {@link OntObject Ontology Object}s and caches them inside this model.
     *
     * @param type {@link Class} the type of {@link OntObject}, not null
     * @param <O>  subtype of {@link OntObject}
     * @return an {@link ExtendedIterator Extended Iterator} of {@link OntObject}s
     */
    public <O extends OntObject> ExtendedIterator<O> listOntObjects(Class<? extends O> type) {
        return listOntObjects(this, type);
    }

    /**
     * The same as {@link OntGraphModelImpl#listOntObjects(Class)}, but for the base graph.
     *
     * @param type {@link Class} the type of {@link OntObject}, not null
     * @param <O>  subtype of {@link OntObject}
     * @return {@link ExtendedIterator Extended Iterator} of {@link OntObject}s
     */
    public <O extends OntObject> ExtendedIterator<O> listLocalOntObjects(Class<? extends O> type) {
        return listOntObjects(getTopModel(), type);
    }

    @Override
    public Stream<OntEntity> ontEntities() {
        return supportedEntityTypes.stream().flatMap(this::ontObjects);
    }

    /**
     * Lists all Ontology Entities.
     * Built-ins are not included.
     *
     * @return {@link ExtendedIterator Extended Iterator} of {@link OntEntity}s
     * @see #listLocalOntEntities()
     */
    public ExtendedIterator<OntEntity> listOntEntities() {
        return Iterators.flatMap(Iterators.create(supportedEntityTypes), this::listOntObjects);
    }

    /**
     * The same as {@link #listOntEntities()} but for the base graph.
     *
     * @return {@link ExtendedIterator Extended Iterator} of {@link OntEntity}s
     * @see #listOntEntities()
     */
    public ExtendedIterator<OntEntity> listLocalOntEntities() {
        return Iterators.flatMap(Iterators.create(supportedEntityTypes), this::listLocalOntObjects);
    }

    /**
     * Gets 'punnings', i.e. the {@link OntEntity}s which have not only a single type.
     *
     * @param withImports if it false takes into account only the base model
     * @return {@code Stream} of {@link OntEntity}s.
     */
    public Stream<OntEntity> ambiguousEntities(boolean withImports) {
        return ontEntities().filter(e -> withImports || e.isLocal()).filter(e -> supportedEntityTypes.stream()
                .filter(view -> e.canAs(view) && (withImports || e.as(view).isLocal())).count() > 1);
    }

    @Override
    public Stream<OntIndividual> individuals() {
        return Iterators.asStream(listIndividuals(), Graphs.getSpliteratorCharacteristics(getGraph()));
    }

    /**
     * Returns an {@code ExtendedIterator} over all individuals
     * that participate in class assertion statement {@code a rdf:type C}.
     *
     * @return {@link ExtendedIterator} of {@link OntIndividual}s
     */
    public ExtendedIterator<OntIndividual> listIndividuals() {
        OntPersonality personality = getOntPersonality();
        boolean isRDFS = OntPersonalities.isRDFS(personality);
        boolean withOWLThing = OntPersonalities.supportsOWLThing(personality);
        if (!isRDFS && withOWLThing) {
            Model capabilities = getReasonerCapabilities();
            if (capabilities != null &&
                    capabilities.contains(null, ReasonerVocabulary.supportsP, ReasonerVocabulary.individualAsThingP)) {
                return listStatements(null, RDF.type, OWL2.Thing).mapWith(it -> it.getSubject().as(OntIndividual.class));
            }
        }
        return listIndividuals(this,
                personality.forbidden(OntClass.Named.class),
                getGraph().find(Node.ANY, RDF.Nodes.type, Node.ANY));
    }

    @Override
    public Stream<OntClass> hierarchyRoots() {
        return Iterators.asStream(listHierarchyRoots(), Graphs.getSpliteratorCharacteristics(getGraph()));
    }

    public ExtendedIterator<OntClass> listHierarchyRoots() {
        if (OntPersonalities.supportsOWLThing(getOntPersonality())) {
            Model capabilities = getReasonerCapabilities();
            if (capabilities != null && capabilities.contains(null, ReasonerVocabulary.supportsP, ReasonerVocabulary.directSubClassOf)) {
                return listStatements(null, ReasonerVocabulary.directSubClassOf, OWL2.Thing).mapWith(it -> it.getSubject().as(OntClass.class));
            }
        }
        Set<Node> reserved = getOntPersonality().getReserved().getAllResources();
        return listOntObjects(OntClass.class)
                .filterDrop(c -> reserved.contains(c.asNode()))
                .filterKeep(OntClass::isHierarchyRoot);
    }

    @Override
    public <E extends OntEntity> E getOntEntity(Class<E> type, String uri) {
        return findNodeAs(NodeFactory.createURI(OntJenaException.notNull(uri, "Null uri.")), type);
    }

    @Override
    public <T extends OntEntity> T createOntEntity(Class<T> type, String iri) {
        try {
            return createOntObject(type, iri);
        } catch (OntJenaException.Creation ex) {
            // illegal punning ?
            throw new OntJenaException.Creation(
                    String.format("Creation of %s <%s> is not allowed by the configuration. Profile: %s",
                            OntEnhNodeFactories.viewAsString(type), iri, getOntPersonality().getName()),
                    ex);
        }
    }

    /**
     * Creates and caches an ontology object resource by the given type and uri.
     *
     * @param type Class, object type
     * @param uri  String, URI (IRI), can be {@code null} for anonymous resource
     * @param <T>  class-type of {@link OntObject}
     * @return {@link OntObject}, new instance
     * @throws OntJenaException.Unsupported profile mismatch
     */
    public <T extends OntObject> T createOntObject(Class<T> type, String uri) {
        OntPersonality personality = getOntPersonality();
        OntJenaException.checkSupported(personality.supports(type),
                "Attempt to create resource <" + uri + ">. Profile " + personality.getName() +
                        " does not support language construct " + OntEnhNodeFactories.viewAsString(type));
        Node node = Graphs.createNode(uri);
        EnhNodeFactory factory = personality.getObjectFactory(type);
        return factory.createInGraph(node, this).as(type);
    }

    @Override
    public OntGraphModelImpl removeOntObject(OntObject obj) {
        obj.clearAnnotations().content()
                .peek(OntStatement::clearAnnotations)
                .collect(Collectors.toSet()).forEach(this::remove);
        return this;
    }

    @Override
    public OntGraphModelImpl removeOntStatement(OntStatement statement) {
        return remove(statement.clearAnnotations());
    }

    @Override
    public Stream<OntStatement> statements() {
        Graph g = getGraph();
        return asStream(g, g.find().mapWith(this::asStatement), true);
    }

    @Override
    public Stream<OntStatement> statements(Resource s, Property p, RDFNode o) {
        return asStream(getGraph(), listOntStatements(s, p, o), StdModels.isANY(s, p, o));
    }

    @Override
    public Stream<OntStatement> localStatements(Resource s, Property p, RDFNode o) {
        return asStream(getBaseGraph(), listLocalStatements(s, p, o), StdModels.isANY(s, p, o));
    }

    /**
     * {@inheritDoc}
     *
     * @param s {@link Resource} the subject sought, can be {@code null}
     * @param p {@link Property} the predicate sought, can be {@code null}
     * @param o {@link RDFNode} the object sought, can be {@code null}
     * @return {@link StmtIterator} of {@link OntStatement}s
     */
    @Override
    public StmtIterator listStatements(Resource s, Property p, RDFNode o) {
        return StdModels.createStmtIterator(getGraph().find(asNode(s), asNode(p), asNode(o)), this::asStatement);
    }

    /**
     * Returns an {@link ExtendedIterator extended iterator} over all the statements in the model that match a pattern.
     * The statements selected are those whose subject matches the {@code s} argument,
     * whose predicate matches the {@code p} argument
     * and whose object matches the {@code o} argument.
     * If an argument is {@code null} it matches anything.
     * The method is equivalent to the expression {@code listStatements(s, p, o).mapWith(OntStatement.class::cast)}.
     *
     * @param s {@link Resource} the subject sought, can be {@code null}
     * @param p {@link Property} the predicate sought, can be {@code null}
     * @param o {@link RDFNode} the object sought, can be {@code null}
     * @return {@link ExtendedIterator} of {@link OntStatement}s
     * @see #listStatements(Resource, Property, RDFNode)
     */
    public ExtendedIterator<OntStatement> listOntStatements(Resource s, Property p, RDFNode o) {
        return Iterators.create(getGraph().find(asNode(s), asNode(p), asNode(o)).mapWith(this::asStatement));
    }

    /**
     * Lists all statements in the <b>base</b> model that match a pattern
     * in the form of {@link ExtendedIterator Extended Iterator}.
     * The method is equivalent to the expression
     * {@code listStatements(s, p, o).mapWith(OntStatement.class::cast).filterKeep(OntStatement::isLocal)}.
     *
     * @param s {@link Resource} the subject sought, can be {@code null}
     * @param p {@link Property} the predicate sought, can be {@code null}
     * @param o {@link RDFNode} the object sought, can be {@code null}
     * @return {@link ExtendedIterator} of {@link OntStatement}s, which are local to the base graph
     * @see #listStatements(Resource, Property, RDFNode)
     */
    public ExtendedIterator<OntStatement> listLocalStatements(Resource s, Property p, RDFNode o) {
        return Iterators.create(getBaseGraph().find(asNode(s), asNode(p), asNode(o)).mapWith(this::asStatement));
    }

    @Override
    public OntStatementImpl createStatement(Resource s, Property p, RDFNode o) {
        return OntStatementImpl.createOntStatementImpl(s, p, o, this);
    }

    @Override
    public OntStatementImpl asStatement(Triple triple) {
        return OntStatementImpl.createOntStatementImpl(triple, this);
    }

    /**
     * Determines if the given {@code (s, p, o)} pattern is present in the base graph,
     * with {@code null} allowed to represent a wildcard match.
     *
     * @param s - {@link Resource} - the subject of the statement tested ({@code null} as wildcard)
     * @param p - {@link Property} - the predicate of the statement tested ({@code null} as wildcard)
     * @param o - {@link RDFNode} - the object of the statement tested ({@code null} as wildcard)
     * @return boolean
     * @see Model#contains(Resource, Property, RDFNode)
     */
    public boolean containsLocal(Resource s, Property p, RDFNode o) {
        return getBaseGraph().contains(asNode(s), asNode(p), asNode(o));
    }

    /**
     * Wraps the existing given {@link RDFList []-list} as {@link OntList ONT-list}.
     *
     * @param list      {@link RDFList}, not {@code null}
     * @param subject   {@link OntObject}, not {@code null}
     * @param predicate {@link Property}, not {@code null}
     * @param type      a {@code Class}-type for list element {@code E}, not {@code null}
     * @param <E>       any {@link RDFNode}
     * @return {@code OntList}
     */
    public <E extends RDFNode> OntListImpl<E> asOntList(RDFList list,
                                                        OntObject subject,
                                                        Property predicate,
                                                        Class<E> type) {
        return asOntList(list, subject, predicate, false, null, type);
    }

    /**
     * Wraps the existing given {@link RDFList []-list} as {@link OntList ONT-list}.
     *
     * @param list            {@link RDFList}, not {@code null}
     * @param subject         {@link OntObject}, not {@code null}
     * @param predicate       {@link Property}, not {@code null}
     * @param checkRecursions boolean, if {@code true} more careful and expensive checking for list content is performed
     * @param listType        an uri-{@link Resource}, used as an archaic RDF-type, usually this parameter should be {@code null}
     * @param elementType     a {@code Class}-type for list element {@code E}, not {@code null}
     * @param <E>             any {@link RDFNode}
     * @return {@code OntList}
     */
    public <E extends RDFNode> OntListImpl<E> asOntList(RDFList list,
                                                        OntObject subject,
                                                        Property predicate,
                                                        boolean checkRecursions,
                                                        Resource listType,
                                                        Class<E> elementType) {
        OntListImpl.checkRequiredInputs(subject, predicate, list, listType, elementType);
        return checkRecursions ?
                OntListImpl.asSafeOntList(list, this, subject, predicate, listType, elementType) :
                OntListImpl.asOntList(list, this, subject, predicate, listType, elementType);
    }

    /**
     * Creates ONT-List with given elements and other settings.
     *
     * @param subject   {@link OntObject}, not {@code null}
     * @param predicate {@link Property}, not {@code null}
     * @param type      a {@code Class}-type for element {@code E}, not {@code null}
     * @param elements  and {@code Iterator} of {@code E}-elements (the order is preserved), not {@code null}
     * @param <E>       any {@link RDFNode}
     * @return {@code OntList}
     */
    public <E extends RDFNode> OntListImpl<E> createOntList(OntObject subject,
                                                            Property predicate,
                                                            Class<E> type,
                                                            Iterator<E> elements) {
        return createOntList(subject, predicate, null, type, elements);
    }

    /**
     * Creates ONT-List with given elements and other settings.
     *
     * @param subject     {@link OntObject}, not {@code null}
     * @param predicate   {@link Property}, not {@code null}
     * @param listType    an uri-{@link Resource}, used as an archaic RDF-type, usually this parameter should be {@code null}
     * @param elementType a {@code Class}-type for element {@code E}, not {@code null}
     * @param elements    and {@code Iterator} of {@code E}-elements (the order is preserved), not {@code null}
     * @param <E>         any {@link RDFNode}
     * @return {@code OntList}
     */
    public <E extends RDFNode> OntListImpl<E> createOntList(OntObject subject,
                                                            Property predicate,
                                                            Resource listType,
                                                            Class<E> elementType,
                                                            Iterator<E> elements) {
        OntListImpl.checkRequiredInputs(subject, predicate, listType, elementType);
        return OntListImpl.create(this, subject, predicate, listType, elementType, Iterators.create(elements));
    }

    /**
     * Lists all (bulk) annotation anonymous resources for the given {@code rdf:type} and SPO.
     *
     * @param t {@link Resource} either {@link OWL2#Axiom owl:Axiom} or {@link OWL2#Annotation owl:Annotation}
     * @param s {@link Resource} subject
     * @param p {@link Property} predicate
     * @param o {@link RDFNode} object
     * @return {@link ExtendedIterator} of annotation {@link Resource resource}s
     */
    public ExtendedIterator<Resource> listAnnotations(Resource t, Resource s, Property p, RDFNode o) {
        return getGraph().find(Node.ANY, OWL2.annotatedSource.asNode(), s.asNode())
                .mapWith(this::asStatement)
                .filterKeep(x -> (OWL2.Axiom == t ? x.belongsToOWLAxiom() : x.belongsToOWLAnnotation())
                        && x.hasAnnotatedProperty(p) && x.hasAnnotatedTarget(o))
                .mapWith(Statement::getSubject);
    }

    /**
     * Deletes the specified {@code OntList} including its annotations.
     *
     * @param subject   {@link OntObject} the subject of the OntList root statement
     * @param predicate {@link Property} the predicate of the OntList root statement
     * @param object    {@link OntList} to be deleted
     * @return this model instance
     */
    @SuppressWarnings("UnusedReturnValue")
    public OntGraphModelImpl deleteOntList(OntObject subject, Property predicate, OntList<?> object) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(predicate);
        OntJenaException.notNull(object, "Null list for subject " + subject + " and predicate " + predicate);
        boolean hasNil = !object.isNil() && contains(subject, predicate, RDF.nil);
        object.getMainStatement().clearAnnotations();
        object.clear(); // now it is nil-list
        if (!hasNil) {
            return remove(subject, predicate, object);
        }
        return this;
    }

    @Override
    public OntDisjoint.Classes createDisjointClasses(Collection<OntClass> classes) {
        if (classes.isEmpty()) {
            throw new IllegalArgumentException("Empty list is specified");
        }
        checkType(OntDisjoint.Classes.class);
        return checkCreate(model ->
                OntDisjointImpl.createDisjointClasses(model, classes.stream()), OntDisjoint.Classes.class
        );
    }

    @Override
    public OntDisjoint.Individuals createDifferentIndividuals(Collection<OntIndividual> individuals) {
        if (individuals.isEmpty()) {
            throw new IllegalArgumentException("Empty list is specified");
        }
        checkType(OntDisjoint.Individuals.class);
        return checkCreate(model ->
                OntDisjointImpl.createDifferentIndividuals(model, individuals.stream()), OntDisjoint.Individuals.class
        );
    }

    @Override
    public OntDisjoint.ObjectProperties createDisjointObjectProperties(Collection<OntObjectProperty> properties) {
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("Empty list is specified");
        }
        checkType(OntDisjoint.ObjectProperties.class);
        return checkCreate(model ->
                OntDisjointImpl.createDisjointObjectProperties(model, properties.stream()), OntDisjoint.ObjectProperties.class
        );
    }

    @Override
    public OntDisjoint.DataProperties createDisjointDataProperties(Collection<OntDataProperty> properties) {
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("Empty list is specified");
        }
        checkType(OntDisjoint.DataProperties.class);
        return checkCreate(model ->
                OntDisjointImpl.createDisjointDataProperties(model, properties.stream()), OntDisjoint.DataProperties.class
        );
    }

    @Override
    public <T extends OntFacetRestriction> T createFacetRestriction(Class<T> view, Literal literal) {
        checkType(OntFacetRestriction.class);
        return OntFacetRestrictionImpl.create(this, view, literal);
    }

    @Override
    public OntDataRange.OneOf createDataOneOf(Collection<Literal> values) {
        checkType(OntDataRange.OneOf.class);
        return checkCreate(model -> OntDataRangeImpl.createOneOf(model, values.stream()), OntDataRange.OneOf.class);
    }

    @Override
    public OntDataRange.Restriction createDataRestriction(OntDataRange.Named datatype, Collection<OntFacetRestriction> values) {
        checkType(OntDataRange.Restriction.class);
        return OntDataRangeImpl.createRestriction(this, datatype, values.stream());
    }

    @Override
    public OntDataRange.ComplementOf createDataComplementOf(OntDataRange other) {
        checkType(OntDataRange.ComplementOf.class);
        return OntDataRangeImpl.createComplementOf(this, other);
    }

    @Override
    public OntDataRange.UnionOf createDataUnionOf(Collection<OntDataRange> values) {
        checkType(OntDataRange.UnionOf.class);
        return OntDataRangeImpl.createUnionOf(this, values.stream());
    }

    @Override
    public OntDataRange.IntersectionOf createDataIntersectionOf(Collection<OntDataRange> values) {
        checkType(OntDataRange.IntersectionOf.class);
        return OntDataRangeImpl.createIntersectionOf(this, values.stream());
    }

    @Override
    public OntClass.ObjectSomeValuesFrom createObjectSomeValuesFrom(OntObjectProperty property, OntClass ce) {
        checkType(OntClass.ObjectSomeValuesFrom.class);
        return checkCreate(model -> OntClassImpl.createComponentRestrictionCE(model,
                OntClass.ObjectSomeValuesFrom.class, property, ce, OWL2.someValuesFrom), OntClass.ObjectSomeValuesFrom.class);
    }

    @Override
    public OntClass.DataSomeValuesFrom createDataSomeValuesFrom(OntDataProperty property, OntDataRange dr) {
        checkType(OntClass.DataSomeValuesFrom.class);
        return checkCreate(model -> OntClassImpl.createComponentRestrictionCE(model,
                OntClass.DataSomeValuesFrom.class, property, dr, OWL2.someValuesFrom), OntClass.DataSomeValuesFrom.class);
    }

    @Override
    public OntClass.ObjectAllValuesFrom createObjectAllValuesFrom(OntObjectProperty property, OntClass ce) {
        checkType(OntClass.ObjectAllValuesFrom.class);
        return checkCreate(model -> OntClassImpl.createComponentRestrictionCE(model,
                OntClass.ObjectAllValuesFrom.class, property, ce, OWL2.allValuesFrom), OntClass.ObjectAllValuesFrom.class);
    }

    @Override
    public OntClass.DataAllValuesFrom createDataAllValuesFrom(OntDataProperty property, OntDataRange dr) {
        checkType(OntClass.DataAllValuesFrom.class);
        return OntClassImpl.createComponentRestrictionCE(this,
                OntClass.DataAllValuesFrom.class, property, dr, OWL2.allValuesFrom);
    }

    @Override
    public OntClass.ObjectHasValue createObjectHasValue(OntObjectProperty property, OntIndividual individual) {
        checkType(OntClass.ObjectHasValue.class);
        return OntClassImpl.createComponentRestrictionCE(this,
                OntClass.ObjectHasValue.class, property, individual, OWL2.hasValue);
    }

    @Override
    public OntClass.DataHasValue createDataHasValue(OntDataProperty property, Literal literal) {
        checkType(OntClass.DataHasValue.class);
        return OntClassImpl.createComponentRestrictionCE(this, OntClass.DataHasValue.class, property, literal, OWL2.hasValue);
    }

    @Override
    public OntClass.ObjectMinCardinality createObjectMinCardinality(OntObjectProperty property, int cardinality, OntClass ce) {
        checkType(OntClass.ObjectMinCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this,
                OntClass.ObjectMinCardinality.class, property, cardinality, ce);
    }

    @Override
    public OntClass.DataMinCardinality createDataMinCardinality(OntDataProperty property, int cardinality, OntDataRange dr) {
        checkType(OntClass.DataMinCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this,
                OntClass.DataMinCardinality.class, property, cardinality, dr);
    }

    @Override
    public OntClass.ObjectMaxCardinality createObjectMaxCardinality(OntObjectProperty property, int cardinality, OntClass ce) {
        checkType(OntClass.ObjectMaxCardinality.class);
        return checkCreate(model -> OntClassImpl.createCardinalityRestrictionCE(model,
                OntClass.ObjectMaxCardinality.class, property, cardinality, ce), OntClass.ObjectMaxCardinality.class);
    }

    @Override
    public OntClass.DataMaxCardinality createDataMaxCardinality(OntDataProperty property, int cardinality, OntDataRange dr) {
        checkType(OntClass.DataMaxCardinality.class);
        return checkCreate(model -> OntClassImpl.createCardinalityRestrictionCE(model,
                OntClass.DataMaxCardinality.class, property, cardinality, dr), OntClass.DataMaxCardinality.class);
    }

    @Override
    public OntClass.ObjectCardinality createObjectCardinality(OntObjectProperty property, int cardinality, OntClass ce) {
        checkType(OntClass.ObjectCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this,
                OntClass.ObjectCardinality.class, property, cardinality, ce);
    }

    @Override
    public OntClass.DataCardinality createDataCardinality(OntDataProperty property, int cardinality, OntDataRange dr) {
        checkType(OntClass.DataCardinality.class);
        return OntClassImpl.createCardinalityRestrictionCE(this, OntClass.DataCardinality.class, property, cardinality, dr);
    }

    @Override
    public OntClass.UnionOf createObjectUnionOf(Collection<OntClass> classes) {
        checkType(OntClass.UnionOf.class);
        return checkCreate(model ->
                        OntClassImpl.createComponentsCE(model, OntClass.UnionOf.class, OntClass.class, OWL2.unionOf, classes.stream()),
                OntClass.UnionOf.class);
    }

    @Override
    public OntClass.IntersectionOf createObjectIntersectionOf(Collection<OntClass> classes) {
        checkType(OntClass.IntersectionOf.class);
        return checkCreate(model -> OntClassImpl.createComponentsCE(model,
                OntClass.IntersectionOf.class, OntClass.class, OWL2.intersectionOf, classes.stream()), OntClass.IntersectionOf.class);
    }

    @Override
    public OntClass.OneOf createObjectOneOf(Collection<OntIndividual> individuals) {
        checkType(OntClass.OneOf.class);
        return checkCreate(model -> OntClassImpl.createComponentsCE(model,
                OntClass.OneOf.class, OntIndividual.class, OWL2.oneOf, individuals.stream()), OntClass.OneOf.class);
    }

    @Override
    public OntClass.HasSelf createHasSelf(OntObjectProperty property) {
        checkType(OntClass.HasSelf.class);
        return OntClassImpl.createHasSelf(this, property);
    }

    @Override
    public OntClass.NaryDataAllValuesFrom createDataAllValuesFrom(Collection<OntDataProperty> properties, OntDataRange dr) {
        checkType(OntClass.NaryDataAllValuesFrom.class);
        return OntClassImpl.createNaryRestrictionCE(this, OntClass.NaryDataAllValuesFrom.class, dr, properties);
    }

    @Override
    public OntClass.NaryDataSomeValuesFrom createDataSomeValuesFrom(Collection<OntDataProperty> properties, OntDataRange dr) {
        checkType(OntClass.NaryDataSomeValuesFrom.class);
        return OntClassImpl.createNaryRestrictionCE(this, OntClass.NaryDataSomeValuesFrom.class, dr, properties);
    }

    @Override
    public OntClass.ComplementOf createObjectComplementOf(OntClass ce) {
        checkType(OntClass.ComplementOf.class);
        return checkCreate(model -> OntClassImpl.createComplementOf(model, ce), OntClass.ComplementOf.class);
    }

    @Override
    public OntSWRL.Variable createSWRLVariable(String uri) {
        checkType(OntSWRL.Variable.class);
        return OntSWRLImpl.createVariable(this, uri);
    }

    @Override
    public OntSWRL.Atom.WithBuiltin createBuiltInSWRLAtom(Resource predicate, Collection<OntSWRL.DArg> arguments) {
        checkType(OntSWRL.Atom.WithBuiltin.class);
        return OntSWRLImpl.createBuiltInAtom(this, predicate, arguments);
    }

    @Override
    public OntSWRL.Atom.WithClass createClassSWRLAtom(OntClass clazz, OntSWRL.IArg arg) {
        checkType(OntSWRL.Atom.WithClass.class);
        return OntSWRLImpl.createClassAtom(this, clazz, arg);
    }

    @Override
    public OntSWRL.Atom.WithDataRange createDataRangeSWRLAtom(OntDataRange range, OntSWRL.DArg arg) {
        checkType(OntSWRL.Atom.WithDataRange.class);
        return OntSWRLImpl.createDataRangeAtom(this, range, arg);
    }

    @Override
    public OntSWRL.Atom.WithDataProperty createDataPropertySWRLAtom(OntDataProperty dataProperty,
                                                                    OntSWRL.IArg firstArg,
                                                                    OntSWRL.DArg secondArg) {
        checkType(OntSWRL.Atom.WithDataProperty.class);
        return OntSWRLImpl.createDataPropertyAtom(this, dataProperty, firstArg, secondArg);
    }

    @Override
    public OntSWRL.Atom.WithObjectProperty createObjectPropertySWRLAtom(OntObjectProperty dataProperty,
                                                                        OntSWRL.IArg firstArg,
                                                                        OntSWRL.IArg secondArg) {
        checkType(OntSWRL.Atom.WithObjectProperty.class);
        return OntSWRLImpl.createObjectPropertyAtom(this, dataProperty, firstArg, secondArg);
    }

    @Override
    public OntSWRL.Atom.WithDifferentIndividuals createDifferentIndividualsSWRLAtom(OntSWRL.IArg firstArg,
                                                                                    OntSWRL.IArg secondArg) {
        checkType(OntSWRL.Atom.WithDifferentIndividuals.class);
        return OntSWRLImpl.createDifferentIndividualsAtom(this, firstArg, secondArg);
    }

    @Override
    public OntSWRL.Atom.WithSameIndividuals createSameIndividualsSWRLAtom(OntSWRL.IArg firstArg,
                                                                          OntSWRL.IArg secondArg) {
        checkType(OntSWRL.Atom.WithSameIndividuals.class);
        return OntSWRLImpl.createSameIndividualsAtom(this, firstArg, secondArg);
    }

    @Override
    public OntSWRL.Imp createSWRLImp(Collection<OntSWRL.Atom<?>> head,
                                     Collection<OntSWRL.Atom<?>> body) {
        checkType(OntSWRL.Atom.Imp.class);
        return OntSWRLImpl.createImp(this, head, body);
    }

    /**
     * Creates an object of type {@code X} if it is possible;
     * otherwise throws {@link OntJenaException.Unsupported} exception.
     *
     * @param creator {@link Function} to create {@code X}
     * @param type    of {@code X}
     * @param <X>     {@link OntObject}
     * @return {@code X}
     * @throws OntJenaException.Unsupported if no possible to create an object
     */
    protected <X extends OntObject> X checkCreate(Function<OntGraphModelImpl, X> creator, Class<X> type) {
        Graph thisGraph = getGraph();
        Graph bg = GraphMemFactory.createDefaultGraph();
        UnionGraph ug = new UnionGraphImpl(bg) {
            @Override
            protected void addParent(Graph graph) {
            }
        };
        ug.addSubGraph(thisGraph);
        try {
            OntGraphModelImpl m = new OntGraphModelImpl(ug, getOntPersonality());
            X res = creator.apply(m);
            bg.find().forEach(thisGraph::add);
            return res.inModel(this).as(type);
        } catch (OntJenaException.Conversion e) {
            throw new OntJenaException.Unsupported(
                    "Unable to create object " + OntEnhNodeFactories.viewAsString(type) +
                            "; probably, it is disallowed by profile " + getOntPersonality().getName()
            );
        }
    }

    public RDFDatatype getRDFDatatype(String uri) {
        return dtTypes.computeIfAbsent(uri, u -> {
            RDFDatatype res = TypeMapper.getInstance().getTypeByName(u);
            return res == null ? new BaseDatatype(u) : res;
        });
    }

    public PrefixMapping getPrefixMapping() {
        return getGraph().getPrefixMapping();
    }

    @Override
    public OntGraphModelImpl setNsPrefix(String prefix, String uri) {
        getPrefixMapping().setNsPrefix(prefix, uri);
        return this;
    }

    @Override
    public OntGraphModelImpl removeNsPrefix(String prefix) {
        getPrefixMapping().removeNsPrefix(prefix);
        return this;
    }

    @Override
    public OntGraphModelImpl clearNsPrefixMap() {
        getPrefixMapping().clearNsPrefixMap();
        return this;
    }

    @Override
    public OntGraphModelImpl setNsPrefixes(PrefixMapping pm) {
        getPrefixMapping().setNsPrefixes(pm);
        return this;
    }

    @Override
    public OntGraphModelImpl setNsPrefixes(Map<String, String> map) {
        getPrefixMapping().setNsPrefixes(map);
        return this;
    }

    @Override
    public OntGraphModelImpl withDefaultMappings(PrefixMapping other) {
        getPrefixMapping().withDefaultMappings(other);
        return this;
    }

    @Override
    public OntGraphModelImpl lock() {
        getPrefixMapping().lock();
        return this;
    }

    @Override
    public OntGraphModelImpl add(Statement s) {
        super.add(s);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(Statement s) {
        super.remove(s);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Resource s, Property p, RDFNode o) {
        super.add(s, p, o);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(Resource s, Property p, RDFNode o) {
        super.remove(s, p, o);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Model data) {
        UnionGraph ug = getUnionGraph();
        UnionGraph.EventManager em = ug.getEventManager();
        em.notifyEvent(ug, OntModelEvent.startAddDataGraphEvent(data.getGraph()));
        getBaseModel().add(data);
        em.notifyEvent(ug, OntModelEvent.finishAddDataGraphEvent(data.getGraph()));
        return this;
    }

    @Override
    public OntGraphModelImpl remove(Model data) {
        UnionGraph ug = getUnionGraph();
        UnionGraph.EventManager em = ug.getEventManager();
        em.notifyEvent(ug, OntModelEvent.startDeleteDataGraphEvent(data.getGraph()));
        getBaseModel().remove(data);
        em.notifyEvent(ug, OntModelEvent.finishDeleteDataGraphEvent(data.getGraph()));
        return this;
    }

    @Override
    public OntGraphModelImpl add(StmtIterator iter) {
        super.add(iter);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(StmtIterator iter) {
        super.remove(iter);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Statement[] statements) {
        super.add(statements);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(Statement[] statements) {
        super.remove(statements);
        return this;
    }

    @Override
    public OntGraphModelImpl add(List<Statement> statements) {
        super.add(statements);
        return this;
    }

    @Override
    public OntGraphModelImpl remove(List<Statement> statements) {
        super.remove(statements);
        return this;
    }

    @Override
    public OntGraphModelImpl removeAll(Resource s, Property p, RDFNode o) {
        super.removeAll(s, p, o);
        return this;
    }

    @Override
    public OntGraphModelImpl removeAll() {
        super.removeAll();
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, boolean v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, long v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, int v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, char v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, float v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, double v) {
        super.addLiteral(s, p, v);
        return this;
    }

    @Override
    public OntGraphModelImpl addLiteral(Resource s, Property p, Literal o) {
        super.addLiteral(s, p, o);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Resource s, Property p, String lex) {
        super.add(s, p, lex);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Resource s, Property p, String lex, RDFDatatype datatype) {
        super.add(s, p, lex, datatype);
        return this;
    }

    @Override
    public OntGraphModelImpl add(Resource s, Property p, String lex, String lang) {
        super.add(s, p, lex, lang);
        return this;
    }


    @Override
    public OntGraphModelImpl read(String url) {
        return read(it -> it.read(url));
    }

    @Override
    public OntGraphModelImpl read(Reader reader, String base) {
        return read(it -> it.read(reader, base));
    }

    @Override
    public OntGraphModelImpl read(InputStream reader, String base) {
        return read(it -> it.read(reader, base));
    }

    @Override
    public OntGraphModelImpl read(String url, String lang) {
        return read(it -> it.read(url, lang));
    }

    @Override
    public OntGraphModelImpl read(String url, String base, String lang) {
        return read(it -> it.read(url, base, lang));
    }

    @Override
    public OntGraphModelImpl read(Reader reader, String base, String lang) {
        return read(it -> it.read(reader, base, lang));
    }

    @Override
    public OntGraphModelImpl read(InputStream reader, String base, String lang) {
        return read(it -> it.read(reader, base, lang));
    }

    private OntGraphModelImpl read(Consumer<Model> reader) {
        UnionGraph ug = getUnionGraph();
        UnionGraph.EventManager em = ug.getEventManager();
        em.notifyEvent(ug, OntModelEvent.startReadDataGraphEvent());
        reader.accept(getBaseModel());
        em.notifyEvent(ug, OntModelEvent.finishReadDataGraphEvent());
        rebind();
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public OntGraphModelImpl write(Writer writer) {
        getBaseModel().write(writer);
        return this;
    }

    @Override
    public OntGraphModelImpl write(Writer writer, String lang) {
        getBaseModel().write(writer, lang);
        return this;
    }

    @Override
    public OntGraphModelImpl write(Writer writer, String lang, String base) {
        getBaseModel().write(writer, lang, base);
        return this;
    }

    @Override
    public OntGraphModelImpl write(OutputStream out) {
        getBaseModel().write(out);
        return this;
    }

    @Override
    public OntGraphModelImpl write(OutputStream out, String lang) {
        getBaseModel().write(out, lang);
        return this;
    }

    @Override
    public OntGraphModelImpl write(OutputStream out, String lang, String base) {
        getBaseModel().write(out, lang, base);
        return this;
    }

    @Override
    public OntModel writeAll(Writer writer, String lang, String base) {
        super.write(writer, lang, base);
        return this;
    }

    @Override
    public OntModel writeAll(Writer writer, String lang) {
        super.write(writer, lang);
        return this;
    }

    @Override
    public OntModel writeAll(OutputStream out, String lang, String base) {
        super.write(out, lang, base);
        return this;
    }

    @Override
    public OntModel writeAll(OutputStream out, String lang) {
        super.write(out, lang);
        return this;
    }

    @Override
    public OntAnnotationProperty getRDFSComment() {
        return findNodeAs(RDFS.Nodes.comment, OntAnnotationProperty.class);
    }

    @Override
    public OntAnnotationProperty getRDFSLabel() {
        return findNodeAs(RDFS.Nodes.label, OntAnnotationProperty.class);
    }

    @Override
    public OntClass.Named getOWLThing() {
        return findNodeAs(OWL2.Thing.asNode(), OntClass.Named.class);
    }

    @Override
    public OntDataRange.Named getRDFSLiteral() {
        return findNodeAs(RDFS.Literal.asNode(), OntDataRange.Named.class);
    }

    @Override
    public OntClass.Named getOWLNothing() {
        return findNodeAs(OWL2.Nothing.asNode(), OntClass.Named.class);
    }

    @Override
    public OntObjectProperty.Named getOWLTopObjectProperty() {
        return findNodeAs(OWL2.topObjectProperty.asNode(), OntObjectProperty.Named.class);
    }

    @Override
    public OntObjectProperty.Named getOWLBottomObjectProperty() {
        return findNodeAs(OWL2.bottomObjectProperty.asNode(), OntObjectProperty.Named.class);
    }

    @Override
    public OntDataProperty getOWLTopDataProperty() {
        return findNodeAs(OWL2.topDataProperty.asNode(), OntDataProperty.class);
    }

    @Override
    public OntDataProperty getOWLBottomDataProperty() {
        return findNodeAs(OWL2.bottomDataProperty.asNode(), OntDataProperty.class);
    }

    /**
     * Returns the {@link Reasoner} which is being used to answer queries to this graph
     * or {@code null} if reasoner is not supported by the model.
     */
    @Override
    public Reasoner getReasoner() {
        InfGraph g = getInfGraph();
        return g != null ? g.getReasoner() : null;
    }

    /**
     * Switches on/off derivation logging.
     * If this option is enabled, then each time a derivation is made, that fact is recorded,
     * and the resulting record can be accessed through a later call to getDerivation.
     * This can take up a lot of space!
     */
    @Override
    public void setDerivationLogging(boolean logOn) {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            graph.setDerivationLogging(logOn);
        }
    }

    /**
     * Returns the derivation of the given statement (which should be the result of some previous list operation).
     * Not all reasoners support derivations.
     *
     * @param statement {@link Statement} to get derivation information
     * @return an iterator over {@code Derivation} records or {@code null} if there is no derivation information
     * available for this triple
     * @see Derivation
     */
    @Override
    public Iterator<Derivation> getDerivation(Statement statement) {
        return (getGraph() instanceof InfGraph) ? ((InfGraph) getGraph()).getDerivation(statement.asTriple()) : null;
    }

    /**
     * Returns {@code InfGraph} or {@code null} if no-inf model
     *
     * @return {@link InfGraph}
     */
    public InfGraph getInfGraph() {
        return graph instanceof InfGraph ? (InfGraph) graph : null;
    }

    /**
     * Causes the inference model to reconsult the underlying data to take into account changes.
     * Normally, changes are made through the InfModel's, add and remove calls are will be handled appropriately.
     * However, in some cases, changes are made behind the InfModel's back and
     * this forces a full reconsult of the changed data.
     */
    @Override
    public void rebind() {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            graph.rebind();
        }
    }

    /**
     * Performs any initial processing and caching.
     * This call is optional.
     * Most engines either have negligible set-up work or will perform an implicit "prepare" if necessary.
     * The call is provided for those occasions where substantial preparation work is possible
     * (e.g. running a forward chaining rule system)
     * and where an application might wish greater control over when
     * this preparation is done rather than just leaving to be done at first query time.
     */
    @Override
    public void prepare() {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            graph.prepare();
        }
    }

    /**
     * Resets any internal caches.
     * Some systems, such as the tabled backchainer, retain information after each query.
     * A reset will wipe this information preventing unbounded memory use at the expense of more expensive future queries.
     * A reset does not cause the raw data to be reconsulted and so is less expensive than a rebinding.
     */
    @Override
    public void reset() {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            graph.reset();
        }
    }

    /**
     * Tests the consistency of the underlying data.
     * This normally tests the validity of the bound instance data against the bound schema data.
     *
     * @return a {@link ValidityReport} structure
     */
    @Override
    public ValidityReport validate() {
        InfGraph graph = getInfGraph();
        return graph != null ? graph.validate() : null;
    }

    /**
     * Finds all the statements matching a pattern.
     * Returns an iterator over all the statements in a model that match a pattern.
     * <p>
     * The SPO terms may refer to resources which are temporarily defined in the "posit" model.
     * This allows one, for example, to query what resources are of type CE where CE is a
     * class expression rather than a named class - put CE in the posit arg.</p>
     *
     * @param subject   The subject sought
     * @param predicate The predicate sought
     * @param object    The value sought
     * @param posit     Model containing additional assertions to be considered when matching statements
     * @return an iterator over the subjects
     */
    @Override
    public StmtIterator listStatements(Resource subject, Property predicate, RDFNode object, Model posit) {
        InfGraph graph = getInfGraph();
        if (graph != null) {
            Graph gp = posit == null ? GraphMemFactory.createGraphMem() : posit.getGraph();
            Iterator<Triple> iter = graph.find(asNode(subject), asNode(predicate), asNode(object), gp);
            return IteratorFactory.asStmtIterator(iter, this);
        } else {
            return null;
        }
    }

    @Override
    public InfModel asInferenceModel() {
        if (getInfGraph() != null) {
            return this;
        } else {
            throw new OntJenaException.Unsupported("No reasoner attached. Inference is not supported");
        }
    }

    /**
     * Returns a derivations model.
     * The rule reasoners typically create a graph containing those triples added to the base graph due to rule firings.
     * In some applications, it can be useful to be able to access those deductions directly,
     * without seeing the raw data which triggered them.
     * In particular, this allows the forward rules to be used as if they were rewrite transformation rules.
     *
     * @return The derivation model, if one is defined, or else {@code null}
     */
    @Override
    public Model getDeductionsModel() {
        if (deductionsModel == null) {
            InfGraph infGraph = getInfGraph();
            if (infGraph != null) {
                Graph deductionsGraph = infGraph.getDeductionsGraph();
                if (deductionsGraph != null) {
                    deductionsModel = new ModelCom(deductionsGraph);
                }
            }
        } else {
            // ensure that the cached model sees the updated changes from the underlying reasoner graph
            Objects.requireNonNull(getInfGraph()).prepare();
        }
        return deductionsModel;
    }

    /**
     * Returns the raw RDF model being processed
     * (i.e. the argument to the {@link Reasoner#bind(Graph)} call that created this {@link InfModel}).
     */
    @Override
    public Model getRawModel() {
        return getBaseModel();
    }

    private Model getReasonerCapabilities() {
        Reasoner reasoner = getReasoner();
        return reasoner != null ? reasoner.getReasonerCapabilities() : null;
    }

    /**
     * Returns a {@link RDFNode} for the given type and, if the result is present, caches it node at the model level.
     * The method works silently: normally no exception is expected.
     *
     * @param node {@link Node}
     * @param type {@link Class}-type
     * @param <N>  any subtype of {@link RDFNode}
     * @return {@link RDFNode} or {@code null}
     * @throws RuntimeException unexpected misconfiguration (RDF recursion, wrong input, personality mismatch, etc.)
     * @see #getNodeAs(Node, Class)
     */
    @Override
    public <N extends RDFNode> N findNodeAs(Node node, Class<N> type) {
        try {
            return getNodeAs(node, type);
        } catch (OntJenaException.Conversion ignore) {
            // ignore
            return null;
        }
    }

    /**
     * Answers an enhanced node that wraps the given node and conforms to the given interface type.
     * The returned RDF node is cached at the model-level.
     *
     * @param node a node (assumed to be in this graph)
     * @param type a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node, cannot be {@code null}
     * @throws OntJenaException unable to construct new RDF view for whatever reason
     * @throws RuntimeException unexpected misconfiguration (wrong inputs, personality mismatch)
     */
    @Override
    public <N extends RDFNode> N getNodeAs(Node node, Class<N> type) {
        try {
            return getNodeAsInternal(node, type);
        } catch (OntJenaException e) {
            throw e;
        } catch (Exception e) {
            throw new OntJenaException.Conversion(String.format(
                    "Failed to convert node <%s> to <%s>. Profile <%s>",
                    node, OntEnhNodeFactories.viewAsString(type), getOntPersonality().getName()
            ), e);
        }
    }

    /**
     * Answers an enhanced node that wraps the given node and conforms to the given interface type,
     * taking into account possible graph recursions.
     * For internal usage only.
     *
     * @param node a node (assumed to be in this graph)
     * @param type a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node or {@code null} if no match found
     * @throws OntJenaException.Recursion if a graph recursion is detected
     * @throws RuntimeException           unexpected misconfiguration
     * @see #getNodeAs(Node, Class)
     */
    @Override
    public <N extends RDFNode> N safeFindNodeAs(Node node, Class<N> type) {
        Set<Node> nodes = visited.get();
        try {
            if (nodes.add(node)) {
                return getNodeAsInternal(node, type);
            }
            throw new OntJenaException.Recursion("Can't cast to " + OntEnhNodeFactories.viewAsString(type) + ": " +
                    "graph contains a recursion for node <" + node + ">");
        } catch (OntJenaException.Conversion | PersonalityConfigException r) {
            return null;
        } finally {
            nodes.remove(node);
        }
    }

    /**
     * Answers an enhanced node that wraps the given node and conforms to the given interface type.
     * The returned RDF node is cached at the model-level.
     *
     * @param node a node (assumed to be in this graph)
     * @param type a type denoting the enhanced facet desired
     * @param <N>  a subtype of {@link RDFNode}
     * @return an enhanced node
     * @throws org.apache.jena.enhanced.PersonalityConfigException if personality is misconfigured
     *                                                             or the given {@code type} is absent in it;
     *                                                             normally this should not happen
     * @throws NullPointerException                                if any input is {@code null}
     * @throws JenaException                                       unable to construct a new RDF view
     */
    protected <N extends RDFNode> N getNodeAsInternal(Node node, Class<N> type) {
        return super.getNodeAs(Objects.requireNonNull(node, "Null node"),
                Objects.requireNonNull(type, "Null class view."));
    }
}
