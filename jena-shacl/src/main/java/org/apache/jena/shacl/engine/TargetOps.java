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


import static org.apache.jena.shacl.lib.G.allNodesOfType;

import java.util.*;

import org.apache.jena.atlas.lib.CollectionUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.sys.C;
import org.apache.jena.shacl.vocabulary.SHACL;

/** Algorithms that calculate targets */
public class TargetOps {
    public static String strTargets(Collection<Target> targets, NodeFormatter nodeFmt) {
        if ( targets.size() == 1 )
            return strTarget(CollectionUtils.oneElt(targets), nodeFmt);

        StringJoiner sj = new StringJoiner(", ","(",")");
        targets.forEach(t->sj.add(strTarget(t, nodeFmt)));
        return sj.toString();
    }

    public static String strTarget(Target target, NodeFormatter nodeFmt) {
        if ( nodeFmt == null )
            nodeFmt = ShLib.nodeFmtAbbrev;

        switch(target.getTargetType()) {
            case implicitClass :
                return "T/Impl ["+ShLib.displayStr(target.getObject(), nodeFmt)+"]";
            case targetClass :
                return "T/Class [?x rdf:type "+ShLib.displayStr(target.getObject(), nodeFmt)+"]";
            case targetNode :
                return "T/Node [?x = "+ShLib.displayStr(target.getObject(), nodeFmt)+"]";
            case targetObjectsOf :
                return "T/Obj [_ "+ShLib.displayStr(target.getObject(), nodeFmt)+" ?x]";
            case targetSubjectsOf :
                return "T/Subj [?x "+ShLib.displayStr(target.getObject(), nodeFmt)+" _]";
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
        Set<Node> allClasses = allNodesOfType(shapesGraph, C.rdfsClass);
        if ( allClasses.isEmpty() )
            return Collections.emptySet();
        Set<Node> nodeShapes = allNodesOfType(shapesGraph, SHACL.NodeShape);
        Set<Node> propertyShapes = allNodesOfType(shapesGraph, SHACL.PropertyShape);
        // A = A intersection B
        nodeShapes.retainAll(allClasses);
        propertyShapes.retainAll(allClasses);
        Set<Node> acc = new HashSet<>();
        acc.addAll(nodeShapes);
        acc.addAll(propertyShapes);
        return acc ;
    }
}
