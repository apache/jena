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

import java.util.Collection;
import java.util.function.BiFunction;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.lib.ShLib;

public class Target {

    public static Target create(Node node, TargetType targetType, Node targetObj, Graph shapesGraph) {
        BiFunction<Graph, Target, Collection<Node>> focusNodes = focusNodeFunction(targetType);
        return new Target(targetType, targetObj, shapesGraph, focusNodes);
    }

    private static BiFunction<Graph, Target, Collection<Node>> focusNodeFunction(TargetType targetType) {
        switch(targetType) {
            case implicitClass :    return TargetOps::focusImplicitClass;
            case targetClass :      return TargetOps::focusTargetClass;
            case targetNode :       return TargetOps::focusTargetNode;
            case targetSubjectsOf : return TargetOps::focusSubjectsOf;
            case targetObjectsOf :  return TargetOps::focusObjectsOf;
            case targetExtension :  return TargetOps::focusTargetExt;
            default:
                throw new InternalErrorException();
        }
    }

    private TargetType targetType;
    private Node node;
    private Graph shapesGraph;
    private BiFunction<Graph, Target, Collection<Node>> focusNodes;

    public Target(TargetType decl, Node obj, Graph shapesGraph, BiFunction<Graph, Target, Collection<Node>> focusNodes) {
        this.targetType = decl;
        this.node = obj;
        this.shapesGraph = shapesGraph;
        this.focusNodes = focusNodes;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public Node getObject() {
        return node;
    }

    public Collection<Node> getFocusNodes(Graph data) {
        return focusNodes.apply(data, this);
    }

    /**
     * Some targets need access to the shape graph - for example, SPARQL targets.
     * This is null if the target type does not need access.
     */
    public Graph getShapesGraph() {
        return shapesGraph;
    }

    @Override
    public String toString() {
        return getTargetType().name()+"["+ShLib.displayStr(getObject())+"]";
    }
}
