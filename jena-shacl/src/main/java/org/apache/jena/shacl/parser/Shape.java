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

package org.apache.jena.shacl.parser;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.TargetOps;
import org.apache.jena.shacl.lib.G;
import org.apache.jena.shacl.validation.Severity;
import org.apache.jena.sparql.util.FmtUtils;

public abstract class Shape {

    protected final Graph               shapeGraph;
    protected final Node                shapeNode;
    protected final boolean             deactivated;
    protected final Severity            severity;
    protected final Collection<Node>    messages;
    protected final Collection<Target>  targets;
    protected final List<Constraint>    constraints;
    protected final List<PropertyShape> propertyShapes;

    public Shape(Graph shapeGraph, Node shapeNode, boolean deactivated, Severity severity, List<Node> messages,
                 Collection<Target> targets, List<Constraint> constraints, List<PropertyShape> propertyShapes) {
        super();
        this.shapeGraph = shapeGraph;
        this.shapeNode = shapeNode;
        this.deactivated = deactivated;
        this.severity = severity;
        this.messages = messages;
        this.targets = targets;
        this.constraints = constraints;
        this.propertyShapes = propertyShapes;
    }

    public abstract void visit(ShapeVisitor visitor);

    public Graph getShapeGraph() {
        return shapeGraph;
    }

    public Node getShapeNode() {
        return shapeNode;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Collection<Node> getMessages() {
        return messages;
    }

    public Collection<Target> getTargets() {
        return targets;
    }

    public boolean hasTarget() {
        return ! targets.isEmpty();
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public List<PropertyShape> getPropertyShapes() {
        return propertyShapes;
    }

    public boolean deactivated() {
        return deactivated;
    }

    public boolean isNodeShape()        { return false ; }
    public boolean isPropertyShape()    { return false ; }

    @Override
    public abstract String toString();

    public void print(OutputStream out) {
        if ( !(out instanceof BufferedOutputStream) )
            out = new BufferedOutputStream(out, 128 * 1024);
        IndentedWriter w = new IndentedWriter(out);
        try { print(w); }
        finally { w.flush(); }
    }

    protected abstract void printHeader(IndentedWriter out);

    public void print(IndentedWriter out) {
        printHeader(out);

        boolean printNode = false;

        // Print the shape node unless it is a blank with no reuse.
        if ( ! shapeNode.isBlank()  )
            printNode = true;
        else {
            // blank node but is the shapeNode one-connected? by sh:property?
            long z = G.objectConnectiveness(shapeGraph, shapeNode);
            boolean isOneConnected = G.oneConnected(shapeGraph, shapeNode);
            if ( ! isOneConnected )
                printNode = true;
        }
        if ( printNode ) {
            out.print(" ");
            out.print("node="+FmtUtils.stringForNode(shapeNode));
        }

        if ( deactivated() )
            out.print(" deactivated");
        out.println();
        try {
            out.incIndent();
            targets.forEach(target-> out.println(TargetOps.strTarget(target)) );
//            // Compact list of targets
//            if ( !targets.isEmpty() ) {
//                out.print(TargetOps.strTargets(targets));
//                out.println();
//            }
            // Constraints
            for ( Constraint c : constraints ) {
                c.print(out);
                if ( ! out.atLineStart() )
                    out.println();
            }
            for ( PropertyShape ps : getPropertyShapes() ) {
                ps.print(out);
            }
        }
        finally {
            out.decIndent();
        }
        out.flush();
    }
}
