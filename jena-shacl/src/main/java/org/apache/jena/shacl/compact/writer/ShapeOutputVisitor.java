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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.system.PrefixMap;
import static org.apache.jena.shacl.engine.constraint.CompactOut.*;
import org.apache.jena.shacl.engine.constraint.MaxCount;
import org.apache.jena.shacl.engine.constraint.MinCount;
import org.apache.jena.shacl.parser.*;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathWriter;

class ShapeOutputVisitor implements ShapeVisitor {

    private final IndentedWriter out;
    private final NodeFormatter nodeFmt;
    private final org.apache.jena.sparql.core.Prologue prologue;

    ShapeOutputVisitor(PrefixMap pmap, NodeFormatter nodeFmt, IndentedWriter out) {
        this.nodeFmt = nodeFmt;
        this.out = out;
        this.prologue = new org.apache.jena.sparql.core.Prologue();
        if ( pmap != null )
            pmap.forEach((p,u)->this.prologue.getPrefixMapping().setNsPrefix(p, u));
    }

    @Override
    public void visit(NodeShape nodeShape) {
        printShape(nodeShape);
        printTargets(nodeShape);
        nodeShape.getConstraints().forEach(c->nodeConstraint(c));
        nodeShape.getPropertyShapes().forEach(this::outputPropertyShape);
    }

    @Override
    public void visit(PropertyShape propertyShape) {
        printShape(propertyShape);
        // Any nodeParam constraint?
        Path path = propertyShape.getPath();
        PathWriter.write(out, path, prologue);

        int minCount = -1 ;
        int maxCount = -1;

        // XXX Ugly code.
        // Find min and max.
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
            if ( SHACL.MinCountConstraintComponent.equals(c.getComponent()) ||
                 SHACL.MaxCountConstraintComponent.equals(c.getComponent()) ) {
                // Print on encounter. preserves the look of the input.
                if ( ! minMaxCountDone ) {
                    // Special!
                    if ( minCount != -1 || maxCount!= -1 ) {
                        String minStr = ( minCount != -1 ) ? Integer.toString(minCount) : "0";
                        String maxStr = ( maxCount != -1 ) ? Integer.toString(maxCount) : "*";
                        out.print(" ["+minStr+".."+maxStr+"]");
                    }
                }
                minMaxCountDone = true;
            }
            out.print(" ");
            propertyConstraint(c);
        }

        out.println(" .");

        //propertyShape.getPath();
        // Which may include sh:node.
        // propertyAtom        : propertyType | nodeKind | shapeRef | propertyValue | nodeShapeBody ;

        propertyShape.getPropertyShapes().forEach(this::outputPropertyShape);
    }

    private void printShape(Shape shape) {
        if ( shape.deactivated() ) {
            compactUnquotedString(out, "deactivated", "true");
            out.println(" .");
        }
        if ( shape.getSeverity() != null && ! SHACL.Violation.equals(shape.getSeverity().level())) {
            compact(out, nodeFmt, "severity",shape.getSeverity().level());
            out.println(" .");
        }
        if ( shape.getMessages() != null ) {
            shape.getMessages().forEach(msg->{
                compact(out, nodeFmt, "message", msg);
                out.println(" .");
            });
        }
    }

    private void printTargets(Shape shape) {
        shape.getTargets().forEach(target->{
            switch ( target.getTargetType() ) {
                case implicitClass :
                    // Different syntax. Already printed.
                    return;
                case targetClass :
                    // Different syntax. Already printed.
                    return;
                case targetNode :
                    break;
                case targetObjectsOf :
                    break;
                case targetSubjectsOf :
                    break;
            }
            out.print(target.getTargetType().compact);
            out.print(" = ");
            nodeFmt.format(out, target.getObject());
            out.println(" .");
        });
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

    /*
     Commonality of form.
nodeParam           : 'targetNode' | 'targetObjectsOf' | 'targetSubjectsOf' |
                      'deactivated' | 'severity' | 'message' |
                      'class' | 'datatype' | 'nodeKind' |
                      'minExclusive' | 'minInclusive' | 'maxExclusive' | 'maxInclusive' |
                      'minLength' | 'maxLength' | 'pattern' | 'flags' | 'languageIn' |
                      'equals' | 'disjoint' |
                      'closed' | 'ignoredProperties' | 'hasValue' | 'in' ;

propertyParam       : 'deactivated' | 'severity' | 'message' |
                      'class' | 'datatype' | 'nodeKind' |
                      'minExclusive' | 'minInclusive' | 'maxExclusive' | 'maxInclusive' |
                      'minLength' | 'maxLength' | 'pattern' | 'flags' | 'languageIn' | 'uniqueLang' |
                      'equals' | 'disjoint' | 'lessThan' | 'lessThanOrEquals' |
                      'qualifiedValueShape' | 'qualifiedMinCount' | 'qualifiedMaxCount' | 'qualifiedValueShapesDisjoint' |
                      'closed' | 'ignoredProperties' | 'hasValue' | 'in' ;


       node only      'targetNode' | 'targetObjectsOf' | 'targetSubjectsOf' |

                      'uniqueLang' |
       property only  'qualifiedValueShape' | 'qualifiedMinCount' | 'qualifiedMaxCount' | 'qualifiedValueShapesDisjoint' |
                      'lessThan' | 'lessThanOrEquals' |

     */

//    void printConstraint(Constraint c) {
//        // XXX c.compactPrint(out, nodeFmt);
//        if ( c instanceof DatatypeConstraint ) {
//            DatatypeConstraint dc = (DatatypeConstraint)c;
//            nodeFmt.format(out, dc.getDatatype());
//        } else if ( c instanceof NodeKindConstraint ) {
//            NodeKindConstraint nkc = (NodeKindConstraint)c;
//            // XXX Not ideal. ENUM?
//            String s = nkc.getKind().getLocalName();
//            out.print(s);
//        }
//        else {
//            //SHACL.DatatypeConstraintComponent;
//            // XXX Dummy.
//            c.print(out);
//        }
//        if ( false/*datatype*/) {}
//        if ( false/*sh:node IRI*/) {}
//        // nodeKind
//    }
}
