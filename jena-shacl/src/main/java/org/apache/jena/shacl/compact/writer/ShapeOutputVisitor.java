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

package org.apache.jena.shacl.compact.writer;

import static org.apache.jena.shacl.compact.writer.CompactOut.compact;
import static org.apache.jena.shacl.compact.writer.CompactOut.compactUnquotedString;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.constraint.MaxCount;
import org.apache.jena.shacl.engine.constraint.MinCount;
import org.apache.jena.shacl.parser.*;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.Path;

public class ShapeOutputVisitor implements ShapeVisitor {

    private final IndentedWriter out;
    private final NodeFormatter nodeFmt;
    // For the SPARQL path syntax writer.
    private final org.apache.jena.sparql.core.Prologue prologue;

    ShapeOutputVisitor(PrefixMapping pmap, NodeFormatter nodeFmt, IndentedWriter out) {
        this(nodeFmt, out, new org.apache.jena.sparql.core.Prologue(pmap));
    }

    private ShapeOutputVisitor(NodeFormatter nodeFmt, IndentedWriter out, org.apache.jena.sparql.core.Prologue prologue) {
        this.nodeFmt = nodeFmt;
        this.out = out;
        this.prologue = prologue;
    }

    /** New ShapeOutputVisitor, using the same setup */
    public ShapeOutputVisitor fork(IndentedWriter out) {
        return new ShapeOutputVisitor(this.nodeFmt, out, this.prologue);
    }

    @Override
    public void visit(NodeShape nodeShape) {
        boolean printingStarted = printTargets(nodeShape);
        // nodeParams, not constraints : 'deactivated' | 'severity' | 'message' | (name) | (description)
        printShapeParams(nodeShape, true, printingStarted);
        nodeShape.getConstraints().forEach(c->nodeConstraint(c));
        nodeShape.getPropertyShapes().forEach(this::outputPropertyShape);
    }

    @Override
    public void visit(PropertyShape propertyShape) {
        boolean outputStarted = false;
        // Any nodeParam constraint?
        Path path = propertyShape.getPath();
        ShaclPaths.write(out, path, prologue);
        outputStarted = true;

        printShapeParams(propertyShape, false, outputStarted);

        int minCount = -1 ;
        int maxCount = -1;

        // Find min and max ready for printing.
        for ( Constraint c : propertyShape.getConstraints()) {
            if ( SHACL.MinCountConstraintComponent.equals(c.getComponent()) ) {
                minCount = ((MinCount)c).getMinCount();
                continue;
            }
            if ( SHACL.MaxCountConstraintComponent.equals(c.getComponent()) ){
                maxCount = ((MaxCount)c).getMaxCount();
                continue;
            }
        }

        boolean minMaxCountDone = false;
        for ( Constraint c : propertyShape.getConstraints()) {
            // Min/Max is a special case.
            if ( SHACL.MinCountConstraintComponent.equals(c.getComponent()) ||
                 SHACL.MaxCountConstraintComponent.equals(c.getComponent()) ) {
                // Print on encounter. Preserves the look of the input.
                if ( ! minMaxCountDone ) {
                    // Special!
                    if ( minCount != -1 || maxCount!= -1 ) {
                        String minStr = ( minCount != -1 ) ? Integer.toString(minCount) : "0";
                        String maxStr = ( maxCount != -1 ) ? Integer.toString(maxCount) : "*";
                        out.print(" ["+minStr+".."+maxStr+"]");
                    }
                }
                minMaxCountDone = true;
            } else {
                out.print(" ");
                propertyConstraint(c);
            }
        }

        out.println(" .");

        propertyShape.getPropertyShapes().forEach(this::outputPropertyShape);
    }


    private void paramPrinter(boolean forNodeShape, boolean outputStarted, Runnable action) {
        if ( outputStarted )
            out.println(" ");
        action.run();
        if ( forNodeShape )
            out.println(" .");
    }

    private boolean printShapeParams(Shape shape, boolean forNodeShape, boolean outputStarted) {
        if ( shape.deactivated() ) {
            paramPrinter(forNodeShape, outputStarted,
                         ()->compactUnquotedString(out, "deactivated", "true") );
            outputStarted = true;
        }
        if ( shape.getSeverity() != null && ! SHACL.Violation.equals(shape.getSeverity().level())) {
            paramPrinter(forNodeShape, outputStarted,
                         ()->compact(out, nodeFmt, "severity", shape.getSeverity().level()) );
            outputStarted = true;
        }

        if ( shape.getMessages() != null ) {
            boolean space = outputStarted;
            for ( Node msg : shape.getMessages() ) {
                if ( space )
                    out.print(" ");
                compact(out, nodeFmt, "message", msg);
                if ( forNodeShape )
                    out.println(" .");
                space = true;
            }
            outputStarted = space;
        }
        return outputStarted;
    }

    private boolean printTargets(Shape shape) {
        boolean havePrinted = false;
        for ( Target target : shape.getTargets() ) {
            switch ( target.getTargetType() ) {
                case implicitClass :
                    // Different syntax. Already printed.
                    continue;
                case targetClass :
                    // Different syntax. Already printed.
                    continue;
                case targetNode :
                    break;
                case targetObjectsOf :
                    break;
                case targetSubjectsOf :
                    break;
                case targetExtension :
                    throw new ShaclNotCompactException("sh:target not supported in compact syntax");
                default :
                    break;
            }
            out.print(target.getTargetType().compact);
            out.print(" = ");
            nodeFmt.format(out, target.getObject());
            out.println(" .");
            havePrinted = true;
        }
        return havePrinted;
    }

    private void outputPropertyShape(PropertyShape ps) {
        ps.visit(this);
    }

    void nodeConstraint(Constraint c) {
        constraint(c);
        out.println(" .");
    }

    void propertyConstraint(Constraint c) {
        constraint(c);
    }

    void constraint(Constraint c) {
        c.printCompact(out, nodeFmt);
    }
}
