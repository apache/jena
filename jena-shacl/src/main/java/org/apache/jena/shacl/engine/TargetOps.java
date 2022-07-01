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

package org.apache.jena.shacl.engine;

import java.util.*;

import org.apache.jena.atlas.lib.CollectionUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.engine.constraint.SparqlComponent;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.TargetExtensions;
import org.apache.jena.shacl.sys.C;
import org.apache.jena.shacl.validation.EvalSparql;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/** Algorithms concerned with targets */
public class TargetOps {
    public static String strTargets(Collection<Target> targets, NodeFormatter nodeFmt) {
        if ( targets.size() == 1 )
            return strTarget(CollectionUtils.oneElt(targets), nodeFmt);

        StringJoiner sj = new StringJoiner(", ", "(", ")");
        targets.forEach(t -> sj.add(strTarget(t, nodeFmt)));
        return sj.toString();
    }

    public static String strTarget(Target target, NodeFormatter nodeFmt) {
        if ( nodeFmt == null )
            nodeFmt = ShLib.nodeFmtAbbrev;

        switch (target.getTargetType()) {
            case implicitClass :
                return "T/Impl [" + ShLib.displayStr(target.getObject(), nodeFmt) + "]";
            case targetClass :
                return "T/Class [?x rdf:type " + ShLib.displayStr(target.getObject(), nodeFmt) + "]";
            case targetNode :
                return "T/Node [?x = " + ShLib.displayStr(target.getObject(), nodeFmt) + "]";
            case targetObjectsOf :
                return "T/Obj [_ " + ShLib.displayStr(target.getObject(), nodeFmt) + " ?x]";
            case targetSubjectsOf :
                return "T/Subj [?x " + ShLib.displayStr(target.getObject(), nodeFmt) + " _]";
            case targetExtension :
                return "T/Ext [" + ShLib.displayStr(target.getObject(), nodeFmt) + "]";
            default :
                return "** Unknown **";
        }
    }

    /* Find sh:targetClass */
    public static Set<Node> shapesTargetClass(Graph shapesGraph) {
        return shapesGraph.find(Node.ANY, SHACL.targetClass, null).mapWith(Triple::getSubject).toSet();
    }

    /* Find sh:targetNode */
    public static Set<Node> shapesTargetNode(Graph shapesGraph) {
        return shapesGraph.find(Node.ANY, SHACL.targetNode, Node.ANY).mapWith(Triple::getSubject).toSet();
    }

    /* Find sh:targetObjectsOf */
    public static Set<Node> shapesTargetObjectsOf(Graph shapesGraph) {
        return shapesGraph.find(Node.ANY, SHACL.targetObjectsOf, Node.ANY).mapWith(Triple::getSubject).toSet();
    }

    /* Find sh:targetSubjectsOf */
    public static Set<Node> shapesTargetSubjectsOf(Graph shapesGraph) {
        return shapesGraph.find(Node.ANY, SHACL.targetSubjectsOf, Node.ANY).mapWith(Triple::getSubject).toSet();
    }

    /** Implicit class targets : section 2.1.3.3 Implicit Class Targets */
    public static Set<Node> implicitClassTargets(Graph shapesGraph) {
        Set<Node> allClasses = G.allNodesOfTypeRDFS(shapesGraph, C.rdfsClass);
        if ( allClasses.isEmpty() )
            return Collections.emptySet();
        Set<Node> nodeShapes = G.allNodesOfTypeRDFS(shapesGraph, SHACL.NodeShape);
        Set<Node> propertyShapes = G.allNodesOfTypeRDFS(shapesGraph, SHACL.PropertyShape);
        // A = A intersection B
        nodeShapes.retainAll(allClasses);
        propertyShapes.retainAll(allClasses);
        Set<Node> acc = new HashSet<>();
        acc.addAll(nodeShapes);
        acc.addAll(propertyShapes);
        return acc;
    }

    /** SHACL-AF: SPARQL targets */
    // Look for ? sh:target [ ].
    public static Set<Node> shapesTargetExtension(Graph shapesGraph) {
        Set<Node> x = new HashSet<>();
        return G.allPO(shapesGraph, SHACL.target, null);
    }

    // If look for specific target extensions
    private static Set<Node> shapesTargetSPARQL(Graph shapesGraph) {
        return G.find(shapesGraph, null, SHACL.target, null)
                .filterKeep(t->G.hasOneSP(shapesGraph, t.getObject(), SHACL.select))
                .mapWith(Triple::getSubject)
                .toSet();
    }

    //Could build a registry of SPARQL-based Target types.
    private static Set<Node> shapesSPARQLTargetType(Graph shapesGraph) {
        return G.find(shapesGraph, null, SHACL.target, null)
                .filterKeep(t->G.isOfType(shapesGraph, t.getObject(), SHACL.Target))
                .mapWith(Triple::getSubject)
                .toSet();
    }


    // ---- Focus node generators

    public static Collection<Node> focusTargetClass(Graph data, Target target) {
        return G.allNodesOfTypeRDFS(data, target.getObject());
    }

    public static Collection<Node> focusImplicitClass(Graph data, Target target) {
        return focusTargetClass(data, target);
    }

    public static Collection<Node> focusTargetNode(Graph data, Target target) {
        return  Collections.singletonList(target.getObject());
    }

    public static Collection<Node> focusSubjectsOf(Graph data, Target target) {
        return G.allPO(data, target.getObject(), null);
    }

    public static Collection<Node> focusObjectsOf(Graph data, Target target) {
        return G.allSP(data, null, target.getObject());
    }

    /** Process target extensions.
     * Supported:
     * <ul>
     * <li>SPARQL-based target -- {@code sh:target [ sh:select ]}
     * <li>SPARQL-based target type -- {@code sh:target [ rdf:type ?T] } and {@code ?T rdf:type  sh:SPARQLTargetType}.
     * </ul>
     */
    public static Collection<Node> focusTargetExt(Graph data, Target target) {
        Graph shapesGraph = Objects.requireNonNull(target.getShapesGraph());
        Node targetArg = target.getObject();

        // One step extensions.
        // Is it a SPARQL-based target -- sh:target [ sh:select ] (regardless of rdf:type)
        if ( G.hasOneSP(shapesGraph, targetArg, SHACL.select) ) {
            Query query = ShLib.extractSPARQLQuery(shapesGraph, targetArg);
            if ( ! query.isSelectType() )
                throw new ShaclException("Not a SELECT query");
            DatasetGraph dsg = DatasetGraphFactory.wrap(data);
            QueryExecution qExec = QueryExecutionFactory.create(query, dsg);
            return EvalSparql.evalSparqlOneVar(qExec);
        }

        // Two step extensions by rdf:type
        // Is it a SPARQL-based target type -- sh:target [ rdf:type ?T ] and ?T rdf:type  sh:SPARQLTargetType.

        // Declared type.
        List<Node> targetExt = G.typesOfNodeAsList(shapesGraph, targetArg);
        for ( Node ext : targetExt ) {
            if ( G.isOfType(shapesGraph, ext, SHACL.SPARQLTargetType) ) {
                // Now find the type.
                Node type;
                List<Node> types = G.typesOfNodeAsList(shapesGraph, targetArg);
                if ( types.size() == 1 )
                    // It passed the G.isOfType test.
                    type = CollectionUtils.oneElt(types);
                else {
                    Set<Node> allClasses = G.subClasses(shapesGraph, SHACL.Target);
                    // Find any(first) in allClasses
                    Optional<Node> x = types.stream().filter(t->allClasses.contains(t)).findFirst();
                    type = x.orElseThrow();
                }

                try {
                    // This is also available via the Shapes object.
                    // Maybe attach to the target as it is created.
                    // But this is at the point of deciding focus nodes so called
                    // one (per target shape) not every validation of a focus node.
                    SparqlComponent sparqlComponent = TargetExtensions.sparqlTargetType(shapesGraph, type);
                    return EvalSparql.evalSparqlComponent(data, target.getObject(), sparqlComponent);
                } catch(Exception ex) {
                    throw ex;
                }
            }
        }
        throw new ShaclException("Unknown target extension");
    }
}
