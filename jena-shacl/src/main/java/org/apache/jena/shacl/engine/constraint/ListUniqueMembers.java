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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.ConstraintVisitor;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;

/** sh:memberShape */

public class ListUniqueMembers extends ConstraintList {

    public ListUniqueMembers(Node node) {}

    @Override
    public void visit(ConstraintVisitor visitor){
        visitor.visit(this);
    }

    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        //compact(out, nodeFmt, "nodeKind", getKind());
        // Property context only.
//        String s = getKind().getLocalName();
//        out.print(s);
    }

    @Override
    protected ReportItem validateList(ValidationContext vCxt, Graph data, Node headNode) {
        throw new NotImplemented();
    }

    @Override
    public Node getComponent() {
        return SHACL.UniqueMembersConstraintComponent;
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nodeFmt) {
        out.print(toString());
    }

    @Override
    public String toString() {
        return "ListMemberShape[]";
    }

    @Override
    public int hashCode() {
        throw new NotImplemented();
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof ListUniqueMembers other) )
            return false;
        throw new NotImplemented();
    }

}
