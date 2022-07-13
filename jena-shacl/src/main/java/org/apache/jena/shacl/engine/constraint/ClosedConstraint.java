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

package org.apache.jena.shacl.engine.constraint;

import static org.apache.jena.shacl.compact.writer.CompactOut.compactArrayNodes;
import static org.apache.jena.shacl.compact.writer.CompactOut.compactUnquotedString;
import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.*;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.event.ConstraintEvaluatedOnSinglePathNodeEvent;
import org.apache.jena.shacl.validation.event.ConstraintEvaluatedOnFocusNodeEvent;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.util.graph.GNode;
import org.apache.jena.sparql.util.graph.GraphList;

/** sh:closed */
public class ClosedConstraint implements Constraint {

    private final Set<Node> expected;
    //private final Set<Node> ignoredProperties;
    // Retain written order in SHACLC
    private final List<Node> ignoredProperties;
    private final boolean active;

    public ClosedConstraint(Graph shapeGraph, Node shapeNode, boolean active) {
        expected = shapeProperties(shapeGraph, shapeNode);
        this.active = active;
        List<Node> ignored = ignoredProperties(shapeGraph, shapeNode);
        ignoredProperties = (ignored == null) ? Collections.emptyList() : ignored;
    }

    public Set<Node> getExpected(){
        return Collections.unmodifiableSet(expected);
    }

    public List<Node> getIgnoredProperties() {
        return Collections.unmodifiableList(ignoredProperties);
    }

    public boolean isActive() {
        return active;
    }

    private static List<Node> ignoredProperties(Graph shapesGraph, Node shNode) {
        List<Node> ignoredProperties = null;
        if ( G.contains(shapesGraph, shNode, SHACL.ignoredProperties, null) ) {
            Node ignored = G.getOneSP(shapesGraph, shNode, SHACL.ignoredProperties);
            if ( ignored != null ) {
                ignoredProperties = GraphList.members(GNode.create(shapesGraph, ignored)) ;
                ignoredProperties.forEach(p->{
                    if ( ! p.isURI() )
                        throw new ShaclParseException("Only URIs allowed in sh:ignoredProperties at "+displayStr(shNode));
                });
            }
        }
        return ignoredProperties;
    }

    @Override
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        validate(vCxt, data, shape, focusNode);
    }

    @Override
    public void validatePropertyShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> valueNodes) {
        validate(vCxt, data, shape, focusNode);
    }

    private void validate(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        if ( ! active )
            return;

        // My reading of:
        // 4.8.1 sh:closed, sh:ignoredProperties
        //
        // "a predicate that is not explicitly enumerated as a _value_ of sh:path"
        //
        // means the predicate is the object of the sh:path triple,
        // and path other than a single predicate has a value that
        // is a bnode (usually).

        Set<Node> actual = properties(data,  focusNode);
        boolean passed = true;
        for ( Node p : actual ) {
            if ( ! expected.contains(p) && ! ignoredProperties.contains(p) ) {
                Path path = PathFactory.pathLink(p);
                passed = false;
                G.listSP(data, focusNode, p).forEach(o-> {
                    String msg = toString()+" Property = "+displayStr(p)+" : Object = "+displayStr(o);
                    vCxt.notifyValidationListener(() -> new ConstraintEvaluatedOnSinglePathNodeEvent(vCxt, shape, focusNode, this, path, o,false));
                    vCxt.reportEntry(msg, shape, focusNode, path, o, this);
                });
            }
        }
        if (passed) {
            vCxt.notifyValidationListener(() ->  new ConstraintEvaluatedOnFocusNodeEvent(vCxt, shape, focusNode, this, true));
        }
    }

    private Set<Node> properties(Graph data, Node focusNode) {
        return G.find(data, focusNode, Node.ANY, Node.ANY)
            .mapWith(Triple::getPredicate)
            .filterKeep(p->p.isURI())
            .toSet();
    }

    private Set<Node> shapeProperties(Graph shapeGraph, Node shapeNode) {
        List<Node> propertyShapes = G.listSP(shapeGraph, shapeNode, SHACL.property);
        Set<Node> properties = new HashSet<>();
        propertyShapes.forEach(ps->{
            // This can be a path!
            shapeGraph.find(ps, SHACL.path, null)
                .mapWith(Triple::getObject)
                .filterKeep(Node::isURI)
                .forEachRemaining(properties::add);
        });
        return properties;
    }

    @Override
    public Node getComponent() {
        return SHACL.ClosedConstraintComponent;
    }

    @Override
    public void visit(ConstraintVisitor visitor){
        visitor.visit(this);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compactUnquotedString(out, "closed", Boolean.toString(active));
        if ( ! ignoredProperties.isEmpty() ) {
            out.print(" ");
            compactArrayNodes(out, nodeFmt, "ignoredProperties", ignoredProperties);
        }
    }

    @Override
    public String toString() {
         String x = "Closed"+expected;
         if ( ! ignoredProperties.isEmpty() )
             x = x + ignoredProperties;
         return x;
    }

    @Override
    public int hashCode() {
        return Objects.hash(active, expected, ignoredProperties);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ClosedConstraint other = (ClosedConstraint)obj;
        return active == other.active && Objects.equals(expected, other.expected)
               && Objects.equals(ignoredProperties, other.ignoredProperties);
    }
}
