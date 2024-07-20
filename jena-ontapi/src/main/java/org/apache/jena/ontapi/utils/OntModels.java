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

package org.apache.jena.ontapi.utils;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.common.OntEnhGraph;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.impl.objects.OntIndividualImpl;
import org.apache.jena.ontapi.impl.objects.OntListImpl;
import org.apache.jena.ontapi.impl.objects.OntObjectImpl;
import org.apache.jena.ontapi.impl.objects.OntStatementImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntList;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntNegativeAssertion;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntSWRL;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.model.RDFNodeList;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A collection of utilities for working with {@link OntModel OWL Model} and all related objects:
 * {@link OntObject Ontology Object},
 * {@link OntEntity Ontology Entity},
 * {@link RDFNodeList Node List},
 * {@link OntStatement Ontology Statement}.
 */
public class OntModels {

    /**
     * Determines the actual ontology object type.
     *
     * @param object instance of {@code O}
     * @param <O>    any subtype of {@link OntObject}
     * @return {@link Class}-type of {@code O}
     */
    @SuppressWarnings("unchecked")
    public static <O extends OntObject> Class<O> getOntType(O object) {
        Class<O> res;
        if (object instanceof OntObjectImpl) {
            res = (Class<O>) object.objectType();
        } else {
            res = (Class<O>) OntObjectImpl.findActualClass(object);
        }
        return OntJenaException.notNull(res, "Can't determine the type of object " + object);
    }

    /**
     * Creates an anonymous individual for the given {@link RDFNode RDF Node}, that must be associated with a model.
     * The result anonymous individual could be true (i.e. instance of some owl class)
     * or fake (any blank node can be represented as it).
     *
     * @param inModel {@link RDFNode}, not {@code null}
     * @return {@link OntIndividual.Anonymous}
     * @throws OntJenaException if the node cannot be present as anonymous individual
     */
    @SuppressWarnings("javadoc")
    public static OntIndividual.Anonymous asAnonymousIndividual(RDFNode inModel) {
        return OntIndividualImpl.createAnonymousIndividual(inModel);
    }

    /**
     * Lists all imported models from the given one.
     *
     * @param model {@link OntModel}
     * @return a {@code ExtendedIterator} of {@link OntModel}s
     * @see OntModel#imports()
     */
    @SuppressWarnings("unchecked")
    public static ExtendedIterator<OntModel> listImports(OntModel model) {
        if (model instanceof OntGraphModelImpl m) {
            Reasoner reasoner = ((OntGraphModelImpl) model).getReasoner();
            ExtendedIterator<?> res = m.listImportModels(m.getOntPersonality(), reasoner);
            return (ExtendedIterator<OntModel>) res;
        }
        return Iterators.create(model.imports().iterator());
    }

    /**
     * Lists all ontology objects with the given {@code type} that are defined in the base graph.
     * See also {@link OntModels#listLocalStatements(OntModel, Resource, Property, RDFNode)} description.
     *
     * @param model {@link OntModel}
     * @param type  {@link Class}-type
     * @param <O>   subclass of {@link OntObject}
     * @return {@link ExtendedIterator} of ontology objects of the type {@code O} that are local to the base graph
     * @see OntModel#ontObjects(Class)
     */
    public static <O extends OntObject> ExtendedIterator<O> listLocalObjects(OntModel model, Class<? extends O> type) {
        if (model instanceof OntGraphModelImpl) {
            return ((OntGraphModelImpl) model).listLocalOntObjects(type);
        }
        Stream<O> res = model.ontObjects(type);
        return Iterators.create(res.iterator()).filterKeep(OntObject::isLocal);
    }

    /**
     * Lists all OWL entities that are defined in the base graph.
     * See also {@link OntModels#listLocalStatements(OntModel, Resource, Property, RDFNode)} description.
     *
     * @param model {@link OntModel}
     * @return {@link ExtendedIterator} of {@link OntEntity}s that are local to the base graph
     * @see OntModel#ontEntities()
     */
    public static ExtendedIterator<OntEntity> listLocalEntities(OntModel model) {
        if (model instanceof OntGraphModelImpl) {
            return ((OntGraphModelImpl) model).listLocalOntEntities();
        }
        return Iterators.create(model.ontEntities().iterator()).filterKeep(OntObject::isLocal);
    }

    /**
     * Lists all members from {@link OntList Ontology List}.
     *
     * @param list {@link RDFNodeList}
     * @param <R>  {@link RDFNode}, a type of list members
     * @return {@link ExtendedIterator} of {@code R}
     */
    public static <R extends RDFNode> ExtendedIterator<R> listMembers(RDFNodeList<R> list) {
        if (list instanceof OntListImpl) {
            return ((OntListImpl<R>) list).listMembers();
        }
        return Iterators.create(list.members().iterator());
    }

    /**
     * Lists all class-types for the given individual.
     *
     * @param i an {@link OntIndividual}, not {@code null}
     * @return an {@link ExtendedIterator} over all direct {@link OntClass class}-types
     */
    public static ExtendedIterator<OntClass> listClasses(OntIndividual i) {
        return i instanceof OntIndividualImpl ? ((OntIndividualImpl) i).listClasses() : Iterators.create(i.classes().iterator());
    }

    /**
     * Lists all model statements, which belong to the base graph, using the given SPO.
     * <p>
     * It is placed here because there is no certainty that methods for working with {@code ExtendedIterator}
     * (like {@link OntGraphModelImpl#listLocalStatements(Resource, Property, RDFNode)})
     * should be placed in the public interfaces:
     * {@code Stream}-based analogues are almost the same but more functional.
     * But the ability to work with {@code ExtendedIterator} is sometimes needed,
     * since it is more lightweight and works a bit faster than Stream-API.
     *
     * @param model {@link OntModel}, not {@code null}
     * @param s     {@link Resource}, can be {@code null} for any
     * @param p     {@link Property}, can be {@code null} for any
     * @param o     {@link RDFNode}, can be {@code null} for any
     * @return an {@link ExtendedIterator} of {@link OntStatement}s local to the base model graph
     * @see OntModel#localStatements(Resource, Property, RDFNode)
     */
    public static ExtendedIterator<OntStatement> listLocalStatements(OntModel model,
                                                                     Resource s,
                                                                     Property p,
                                                                     RDFNode o) {
        if (model instanceof OntGraphModelImpl) {
            return ((OntGraphModelImpl) model).listLocalStatements(s, p, o);
        }
        return model.getBaseGraph().find(ModelCom.asNode(s), ModelCom.asNode(p), ModelCom.asNode(p))
                .mapWith(model::asStatement);
    }

    /**
     * Returns an iterator over all direct annotations of the given ontology statement.
     *
     * @param s {@link OntStatement}
     * @return {@link ExtendedIterator} over {@link OntStatement}s
     */
    public static ExtendedIterator<OntStatement> listAnnotations(OntStatement s) {
        if (s instanceof OntStatementImpl) {
            return ((OntStatementImpl) s).listAnnotations();
        }
        return Iterators.create(s.annotations().iterator());
    }

    /**
     * Lists all direct object's annotations.
     *
     * @param o {@link OntObject}, not {@code null}
     * @return {@link ExtendedIterator} over {@link OntStatement}s
     */
    public static ExtendedIterator<OntStatement> listAnnotations(OntObject o) {
        if (o instanceof OntObjectImpl) {
            return ((OntObjectImpl) o).listAnnotations();
        }
        return Iterators.create(o.annotations().iterator());
    }

    /**
     * Recursively lists all annotations for the given {@link OntStatement Ontology Statement}
     * in the form of a flat stream.
     *
     * @param statement {@link OntStatement}, not {@code null}
     * @return a {@code Stream} of {@link OntStatement}s, each of them is annotation property assertion
     * @see #getAllAnnotations(OntStatement)
     */
    public static Stream<OntStatement> annotations(OntStatement statement) {
        return Iterators.fromSet(() -> getAllAnnotations(statement));
    }

    /**
     * For the specified {@link OntStatement Statement}
     * lists all its annotation assertions recursively including their sub-annotations.
     *
     * @param statement {@link OntStatement}, not {@code null}
     * @return an {@link ExtendedIterator} of {@link OntStatement}s
     * @see #getAllAnnotations(OntStatement)
     */
    public static ExtendedIterator<OntStatement> listAllAnnotations(OntStatement statement) {
        return Iterators.create(() -> Iterators.create(getAllAnnotations(statement)));
    }

    /**
     * For the specified {@link OntStatement Statement}
     * gets all its annotation assertions recursively including their sub-annotations.
     * <p>
     * For example, for the following snippet
     * <pre>{@code
     * [ a                      owl:Annotation ;
     *   rdfs:label             "label2" ;
     *   owl:annotatedProperty  rdfs:label ;
     *   owl:annotatedSource    [ a                      owl:Axiom ;
     *                            rdfs:label             "label1" ;
     *                            owl:annotatedProperty  rdfs:comment ;
     *                            owl:annotatedSource    [ a             owl:Ontology ;
     *                                                     rdfs:comment  "comment"
     *                                                   ] ;
     *                            owl:annotatedTarget    "comment"
     *                          ] ;
     *   owl:annotatedTarget    "label1"
     * ] .
     * }</pre>
     * there would be three annotations:
     * {@code _:b0 rdfs:comment "comment"},
     * {@code _:b1 rdfs:label "label1"},
     * {@code _:b2 rdfs:label "label2"}.
     *
     * @param statement {@link OntStatement}, not {@code null}
     * @return an {@link Set} of {@link OntStatement}s
     */
    public static Set<OntStatement> getAllAnnotations(OntStatement statement) {
        Deque<OntStatement> queue = new ArrayDeque<>();
        Set<OntStatement> res = new LinkedHashSet<>();
        queue.add(statement);
        while (!queue.isEmpty()) {
            OntStatement s = queue.removeFirst();
            if (!res.add(s)) {
                continue;
            }
            listAnnotations(s).forEach(queue::add);
        }
        res.remove(statement);
        return res;
    }

    /**
     * Returns an {@code ExtendedIterator} over all {@link OntStatement Ontology Statement}s,
     * which are obtained from splitting the given statement into several equivalent ones but with disjoint annotations.
     * Each of the returned statements is equal to the given, the difference is only in the related annotations.
     * <p>
     * This method can be used in case there are several typed b-nodes for each annotation assertions instead of a single one.
     * Such situation is not a canonical way and should not be widely used, since it is redundant.
     * So usually the result stream contains only a single element: the same {@code OntStatement} instance as the input.
     * <p>
     * The following code demonstrates that non-canonical way of writing annotations with two or more b-nodes:
     * <pre>{@code
     * s A t .
     * _:b0  a                     owl:Axiom .
     * _:b0  A1                    t1 .
     * _:b0  owl:annotatedSource   s .
     * _:b0  owl:annotatedProperty A .
     * _:b0  owl:annotatedTarget   t .
     * _:b1  a                     owl:Axiom .
     * _:b1  A2                    t2 .
     * _:b1  owl:annotatedSource   s .
     * _:b1  owl:annotatedProperty A .
     * _:b1  owl:annotatedTarget   t .
     * }</pre>
     * Here the statement {@code s A t} has two annotations,
     * but they are spread over different resources (statements {@code _:b0 A1 t1} and {@code _:b1 A2 t2}).
     * For this example, the method returns stream of two {@code OntStatement}s, and each of them has only one annotation.
     * For generality, below is an example of the correct and equivalent way to write these annotations,
     * which is the preferred since it is more compact:
     * <pre>{@code
     * s A t .
     * [ a                      owl:Axiom ;
     * A1                     t1 ;
     * A2                     t2 ;
     * owl:annotatedProperty  A ;
     * owl:annotatedSource    s ;
     * owl:annotatedTarget    t
     * ]  .
     * }</pre>
     *
     * @param statement {@link OntStatement}, not {@code null}
     * @return {@link ExtendedIterator} of {@link OntStatement}s
     */
    public static ExtendedIterator<OntStatement> listSplitStatements(OntStatement statement) {
        return ((OntStatementImpl) statement).listSplitStatements();
    }

    /**
     * Answers an {@link OntStatement Ontology Statement} in the specified {@code model}
     * that wraps the given {@code triple}.
     * This method differs from the method {@link OntModel#asStatement(Triple)}
     * in that it provides {@link OntObject#getMainStatement() main statement} if it is possible.
     *
     * @param triple {@link Triple SPO}, not {@code null}
     * @param model  {@link OntModel}, not {@code null}
     * @return {@link OntStatement}
     * @see OntModel#asStatement(Triple)
     * @see OntObject#getMainStatement()
     */
    public static OntStatement toOntStatement(Triple triple, OntModel model) {
        OntStatement res = model.asStatement(triple);
        Resource subj = res.getSubject();
        return Stream.of(OntEntity.class
                        , OntClass.class
                        , OntDataRange.class
                        , OntDisjoint.class
                        , OntObjectProperty.class
                        , OntNegativeAssertion.class
                        , OntSWRL.class)
                .filter(subj::canAs).map(subj::as)
                .map(OntObject::getMainStatement).filter(res::equals)
                .findFirst()
                .orElse(res);
    }

    /**
     * Gets model's config.
     *
     * @param m {@link OntModel}
     * @return {@link OntConfig} or {@code null} if model is not {@link OntEnhGraph}
     */
    public static OntConfig config(OntModel m) {
        return (m instanceof OntEnhGraph) ? ((OntEnhGraph) m).getOntPersonality().getConfig() : null;
    }

    /**
     * Answers a stream of the named hierarchy roots of a given OntModel.
     * This will be similar to the results of {@code OntModel.hierarchyRoot()},
     * with the added constraint that every member of the returned stream will be a named class,
     * not an anonymous class expression.
     * The named root classes are calculated from the root classes
     * by recursively replacing every anonymous class with its direct subclasses.
     * Thus, it can be seen
     * that the values in the stream consist of the shallowest fringe of named classes in the hierarchy.
     *
     * @param m {@link OntModel}
     * @return a {@code Stream} of {@link org.apache.jena.ontapi.model.OntClass.Named}
     * @see OntModel#hierarchyRoots()
     */
    public static Stream<OntClass.Named> namedHierarchyRoots(OntModel m) {
        Set<OntClass> named = new HashSet<>();
        Set<OntClass> anonymous = new HashSet<>();
        collectNamedHierarchyRoots(m.getOWLThing(), m.hierarchyRoots(), named, anonymous);
        while (!anonymous.isEmpty()) {
            OntClass anon = anonymous.iterator().next();
            anonymous.remove(anon);
            collectNamedHierarchyRoots(m.getOWLThing(), anon.subClasses(true), named, anonymous);
        }
        return named.stream().map(OntClass::asNamed);
    }

    private static void collectNamedHierarchyRoots(OntClass thing,
                                                   Stream<OntClass> classes,
                                                   Collection<OntClass> named,
                                                   Collection<OntClass> anonymous) {
        classes.forEach(clazz -> {
            if (named.contains(clazz) || anonymous.contains(clazz)) {
                return;
            }
            if (clazz.superClasses(false)
                    .allMatch(it -> it.isAnon() || it.equals(clazz) || it.equals(thing))) {
                (clazz.isAnon() ? anonymous : named).add(clazz);
            }
        });
    }

    /**
     * Answers the lowest common ancestor of two classes.
     * This is the class that is farthest from the root concept
     * (defaulting to {@code owl:Thing} which is a superclass of both {@code u} and {@code v}).
     * The algorithm is based on
     * <a href="http://en.wikipedia.org/wiki/Tarjan's_off-line_least_common_ancestors_algorithm">Tarjan's off-line LCA</a>.
     * The current implementation expects that the given model:
     * </p>
     * <ul>
     * <li>is transitively closed over the {@code subClassOf} relation</li>
     * <li>can cheaply determine <em>direct sub-class</em> relations</li>
     * </ul>
     * <p>Both of these conditions are true of the built-in Jena OWL reasoners,
     * such as {@link org.apache.jena.ontapi.OntSpecification#OWL2_FULL_MEM_MICRO_RULES_INF},
     * and external DL reasoners such as Pellet.</p>
     *
     * @param u {@link OntClass}
     * @param v {@link OntClass}
     * @return the LCA of {@code u} and {@code v}
     * @throws JenaException if the language profile of the given model does not define a top concept {@code owl:Thing}
     */
    public static OntClass getLCA(OntClass u, OntClass v) {
        OntClass root = OntJenaException.notNull(u.getModel().getOWLThing());
        return getLCA(root, u, v);
    }

    /**
     * Answers the lowest common ancestor of two classes, assuming that the given
     * class is the root concept to start searching from.
     * See {@link #getLCA(OntClass, OntClass)} for details.
     *
     * @param root {@link OntClass}, the root concept, which will be the starting point for the algorithm
     * @param u    {@link OntClass}, an ontology class
     * @param v    {@link OntClass}, an ontology class
     * @return the LCA of {@code u} and {@code v}
     */
    public static OntClass getLCA(OntClass root, OntClass u, OntClass v) {
        // check some common cases first
        if (u.equals(root) || v.equals(root)) {
            return root;
        }

        if (u.hasSubClass(v, false)) {
            return u;
        }

        if (v.hasSubClass(u, false)) {
            return v;
        }

        // not a common case, so apply Tarjan's LCA algorithm
        LCAIndex index = new LCAIndex();
        lca(root, u, v, index);
        return (OntClass) index.getLCA(u, v);
    }

    /**
     * Computes the LCA disjoint set at {@code cls},
     * noting that we are searching for the LCA of {@code uCls} and {@code vCls}.
     *
     * @param cls   The class we are testing (this is 'u' in the Wiki article)
     * @param uCls  One of the two classes we are searching for the LCA of.
     *              We have simplified the set P of pairs to the unity set {uCls, vCls}
     * @param vCls  One of the two classes we are searching for the LCA of.
     *              We have simplified the set P of pairs to the unity set {uCls, vCls}
     * @param index A data structure mapping resources to disjoint sets
     *              (since we can't side effect Jena resources),
     *              and which is used to record the LCA pairs
     */
    private static DisjointSet lca(OntClass cls, OntClass uCls, OntClass vCls, LCAIndex index) {
        DisjointSet clsSet = index.getSet(cls);
        if (clsSet.black) {
            // already visited
            return clsSet;
        }

        // not visited yet
        clsSet.ancestor = clsSet;

        // for each child of cls
        try (Stream<OntClass> subclasses = cls.subClasses(true)) {
            subclasses.forEach(child -> {
                if (child.equals(cls) || child.equals(cls.getModel().getOWLNothing())) {
                    // we ignore the reflexive case and bottom
                    return;
                }

                // compute the LCA of the subtree
                DisjointSet v = lca(child, uCls, vCls, index);

                // union the two disjoint sets together
                clsSet.union(v);

                // propagate the distinguished member
                clsSet.find().ancestor = clsSet;
            });
        }

        // this node is done
        clsSet.black = true;

        // are we inspecting one of the elements we're interested in?
        if (cls.equals(uCls)) {
            checkSolution(uCls, vCls, index);
        } else if (cls.equals(vCls)) {
            checkSolution(vCls, uCls, index);
        }

        return clsSet;
    }

    /**
     * Checks to see if we have found a solution to the problem.
     * Here, since we've assumed that P is the unity set.
     */
    private static void checkSolution(OntClass uCls, OntClass vCls, LCAIndex index) {
        DisjointSet vSet = index.getSet(vCls);
        DisjointSet uSet = index.getSet(uCls);

        if (vSet != null && vSet.black && !vSet.used && uSet != null && uSet.black && !uSet.used) {
            vSet.used = true;
            uSet.used = true;
            OntClass lca = (OntClass) vSet.find().ancestor.node;
            index.setLCA(uCls, vCls, lca);
        }
    }

    /**
     * A simple representation of disjoint sets.
     */
    private static class DisjointSet {
        /**
         * The resource this set represents
         */
        private final Resource node;

        /**
         * The parent set in a union
         */
        private DisjointSet parent;

        /**
         * Heuristic used to build balanced unions
         */
        private int rank;

        /**
         * The link to the distinguished member set
         */
        private DisjointSet ancestor;

        /**
         * Set to true when the node has been processed
         */
        private boolean black = false;

        /**
         * Set to true when we've inspected a black set, since the result is only
         * correct just after both of the sets for u and v have been marked black
         */
        private boolean used = false;

        DisjointSet(Resource node) {
            this.node = node;
            rank = 0;
            parent = this;
        }

        /**
         * The find operation collapses the pointer to the root parent, which is
         * one of Tarjan's standard optimisations.
         *
         * @return The representative of the union containing this set
         */
        DisjointSet find() {
            DisjointSet root;
            if (parent == this) {
                // the representative of the set
                root = this;
            } else {
                // otherwise, seek the representative of my parent and save it
                root = parent.find();
                this.parent = root;
            }
            return root;
        }

        /**
         * The union of two sets
         */
        void union(DisjointSet y) {
            DisjointSet xRoot = find();
            DisjointSet yRoot = y.find();

            if (xRoot.rank > yRoot.rank) {
                yRoot.parent = xRoot;
            } else if (yRoot.rank > xRoot.rank) {
                xRoot.parent = yRoot;
            } else if (xRoot != yRoot) {
                yRoot.parent = xRoot;
                xRoot.rank++;
            }
        }
    }

    /**
     * Simple data structure mapping RDF nodes to disjoint sets, and
     * pairs of resources to their LCA.
     */
    private static class LCAIndex {
        private final Map<Resource, DisjointSet> setIndex = new HashMap<>();
        private final Map<Resource, Map<Resource, Resource>> lcaIndex = new HashMap<>();

        Resource getLCA(Resource u, Resource v) {
            Map<Resource, Resource> map = lcaIndex.get(u);
            Resource lca = map == null ? null : map.get(v);
            if (lca == null) {
                map = lcaIndex.get(v);
                lca = (map == null) ? null : map.get(u);
            }
            return lca;
        }

        void setLCA(Resource u, Resource v, Resource lca) {
            Map<Resource, Resource> uMap = lcaIndex.computeIfAbsent(u, k -> new HashMap<>());
            uMap.put(v, lca);
        }

        DisjointSet getSet(Resource r) {
            return setIndex.computeIfAbsent(r, DisjointSet::new);
        }
    }
}
