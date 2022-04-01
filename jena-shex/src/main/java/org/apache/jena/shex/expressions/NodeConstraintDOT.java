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

package org.apache.jena.shex.expressions;

import org.apache.jena.graph.Node;
import org.apache.jena.shex.sys.ReportItem;
import org.apache.jena.shex.sys.ValidationContext;

public class NodeConstraintDOT extends NodeConstraintComponent
{
    //See ShapeExprTrue

    public NodeConstraintDOT() {}

    @Override
    public int hashCode() {
        // Fixed random number.
        return 634032653;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        return true;
    }

    @Override
    public ReportItem nodeSatisfies(ValidationContext vCxt, Node data) {
        return null;
    }

    @Override
    public String toString() {
        return ".";
    }

    @Override
    public void visit(NodeConstraintVisitor visitor) {
        visitor.visit(this);
    }
}
