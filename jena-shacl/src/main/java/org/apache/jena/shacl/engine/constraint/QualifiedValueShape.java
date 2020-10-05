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

import java.util.*;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.compact.writer.CompactOut;
import org.apache.jena.shacl.compact.writer.CompactWriter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ValidationProc;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.path.Path;

public class QualifiedValueShape implements Constraint {

    private final Shape sub;
    private int qMin;
    private int qMax;
    private boolean qDisjoint;

    public QualifiedValueShape(Shape sub, int qMin, int qMax, boolean qDisjoint) {
        this.sub = sub;
        this.qMin = qMin;
        this.qMax = qMax;
        this.qDisjoint = qDisjoint;
    }

    @Override
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        throw new ShaclException("sh:qualifiedValueShape only valid in a property shape");
    }

    @Override
    public void validatePropertyShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> valueNodes) {
        /*
         * Let Q be a shape in shapes graph G that declares a qualified cardinality
         * constraint (by having values for sh:qualifiedValueShape and at least one of
         * sh:qualifiedMinCount or sh:qualifiedMaxCount). Let ps be the set of shapes in G
         * that have Q as a value of sh:property.
         *
         * If Q has true as a value for
         * sh:qualifiedValueShapesDisjoint then the set of sibling shapes for Q is defined as
         * the set of all values of the SPARQL property path
         * sh:property/sh:qualifiedValueShape for any shape in ps minus the value of
         * sh:qualifiedValueShape of Q itself.
         *
         * The set of sibling shapes is empty
         * otherwise.
         */
        /* TEXTUAL DEFINITION of sh:qualifiedMinCount
         *
         * Let C be the number of value nodes v where v conforms to $qualifiedValueShape and
         * where v does not conform to any of the sibling shapes for the current shape, i.e.
         * the shape that v is validated against and which has $qualifiedValueShape as its
         * value for sh:qualifiedValueShape.
         *
         * A failure MUST be produced if any of the said conformance checks produces a failure.
         * Otherwise, there is a validation result if C is less than $qualifiedMinCount.
         *
         * The constraint component for sh:qualifiedMinCount is sh:QualifiedMinCountConstraintComponent.
         */

        // XXX Siblings be calculated at parse time?
        Collection<Node> sibs = siblings(vCxt.getShapesGraph(), shape);
        Set<Node> valueNodes2;
        if ( qDisjoint ) {
            valueNodes2 = new HashSet<>();
            for ( Node v : valueNodes ) {
                // Ignore disjoint on siblings?
                if ( ! conformsSiblings(vCxt, v, sibs) ) {
                    // No sibling => candidate.
                    valueNodes2.add(v);
                }
            }
        } else {
            valueNodes2 = valueNodes;
        }

        int x = 0;
        for ( Node v : valueNodes2 ) {
            boolean b = conforms(vCxt, sub, v);
            if ( b )
                x++;
        }

        if ( qMin >= 0 && qMin > x ) {
            String msg = toString()+": Min = "+qMin+" but got "+x+" validations";
            vCxt.reportEntry(msg, shape, focusNode, path, null,
                new ReportConstraint(SHACL.QualifiedMinCountConstraintComponent));
        }
        if ( qMax >= 0 && qMax < x ) {
            String msg = toString()+": Max = "+qMax+" but got "+x+" validations";
            vCxt.reportEntry(msg, shape, focusNode, path, null,
                new ReportConstraint(SHACL.QualifiedMaxCountConstraintComponent));
        }
    }

    private boolean conformsSiblings(ValidationContext vCxt, Node v, Collection<Node> sibs) {
        for ( Node sib : sibs ) {
            Shape sibShape = vCxt.getShapes().getShape(sib);
            boolean b = conforms(vCxt, sibShape, v);
            if ( b )
                return true;
        }
        return false;
    }

    private static boolean conforms(ValidationContext vCxt, Shape shape, Node v) {
        ValidationContext vCxt2 = ValidationContext.create(vCxt);
        ValidationProc.execValidateShape(vCxt2, vCxt.getDataGraph(), shape,  v);
        ValidationReport report = vCxt2.generateReport();
        return report.conforms();
    }

    private Collection<Node> siblings(Graph shapesGraph, Shape thisShape) {
        if ( ! qDisjoint )
            return Collections.emptySet();
        Node thisShapeNode = thisShape.getShapeNode();
        Set<Node> sibs = new HashSet<>();
        List<Node> parents = G.listPO(shapesGraph, SHACL.property, thisShapeNode);
        parents.forEach(s->{
            List<Node> sibs1 = G.listSP(shapesGraph, s, SHACL.property);
            sibs.addAll(sibs1);
        });
        Set<Node> sibShapes = new HashSet<>();
        sibs.forEach(s->{
           List<Node> x = G.listSP(shapesGraph, s, SHACL.qualifiedValueShape);
           sibShapes.addAll(x);
        });
        sibShapes.remove(sub.getShapeNode());
        return sibShapes;
    }

    @Override
    public Node getComponent() {
        return SHACL.qualifiedValueShape;
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        // 'qualifiedValueShape' | 'qualifiedMinCount' | 'qualifiedMaxCount' | 'qualifiedValueShapesDisjoint'
        boolean outputDone = false;
        if ( qMin >= 0 ) {
            CompactOut.compact(out, "qualifiedMinCount", qMin);
            outputDone = true;
        }
        if ( qMax >= 0 ) {
            if ( outputDone )
                out.print(" ");
            CompactOut.compact(out, "qualifiedMaxCount", qMax);
            outputDone = true;
        }
        if ( qDisjoint ) {
            if ( outputDone )
                out.print(" ");
            CompactOut.compactUnquotedString(out, "qualifiedValueShapesDisjoint", "true");
            outputDone = true;
        }
        if ( outputDone )
            out.print(" ");
        CompactWriter.output(out, nodeFmt, sub);
    }

    @Override
    public String toString() {
        return String.format("QualifiedValueShape[%s,%s,%s]",
            (qMin<0) ? "_" : Integer.toString(qMin),
            (qMax<0) ? "_" : Integer.toString(qMax),
            qDisjoint);
    }
}
