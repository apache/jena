/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.shacl.engine.constraint;

import static org.apache.jena.shacl.compact.writer.CompactOut.compact;
import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.validation.ValidationProc;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.system.GList;
import org.apache.jena.system.RDFDataException;

/** sh:memberShape */

public class ListMemberShape extends ConstraintList {

    protected final Node shape;

    public ListMemberShape(Node node) {
        this.shape = node;
    }

    @Override
    public void visit(ConstraintVisitor visitor){
        visitor.visit(this);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        compact(out, nodeFmt, "memberShape", shape);
    }

    @Override
    protected ReportItem validateList(ValidationContext vCxt, Graph data, Node listNode) {
        Shape memberShape = vCxt.getShapes().getShape(shape);
        if ( memberShape == null ) {
            // XXX
            //vCxt.reportEntry(, shape, focusNode, path, valueNode, constraint);
            // No shape. Error?
            return null;
        }
        // Checks for valid lists.
        List<Node> members;
        try {
            members = GList.members(data, listNode);
        } catch (RDFDataException ex) {
            String errMsg = ex.getMessage();
            return new ReportItem(errMsg);
        }

        // DRY : execute in isolated context e.g. sh:xone (etc), sh:node, QualifiedValueShape,
        ValidationContext vCxt2 = ValidationContext.create(vCxt);
        // XXX Isolate validation and wrap violations?
        // 1 - ensure the base focusNode
        // 2 - stop at first violation?
        members.forEach(x->{
            ValidationProc.execValidateShape(vCxt2, data, memberShape, x);
        });
        boolean innerConforms = vCxt2.generateReport().conforms();
        if ( ! innerConforms ) {
            // XXX sh:detail
            return new ReportItem("One or more members does not conform to shape "+memberShape);
        }

        return null;
    }

    @Override
    public Node getComponent() {
        return SHACL.MemberShapeConstraintComponent;
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nodeFmt) {
        out.print(toString());
    }

    @Override
    public String toString() {
        // XXX Prefixes
        return "ListMemberShape["+displayStr(shape)+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(shape);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof ListMemberShape other) )
            return false;
        return shape.sameTermAs(other.shape);
    }
}
