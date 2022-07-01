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

import static org.apache.jena.shex.sys.ShexLib.displayStr;

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.sys.ReportItem;
import org.apache.jena.shex.sys.ValidationContext;

public class NodeKindConstraint extends NodeConstraintComponent {
    private NodeKind nodeKind;

    public NodeKindConstraint(NodeKind nodeKind) {
        this.nodeKind = nodeKind;
    }

    public NodeKind getNodeKind() { return nodeKind; }

    @Override
    public ReportItem nodeSatisfies(ValidationContext vCxt, Node n) {
        switch (nodeKind) {
            case BNODE :
                if ( n.isBlank() )
                    return null;
                break;
            case IRI :
                if ( n.isURI() )
                    return null;
                break;
            case LITERAL :
                if ( n.isLiteral() )
                    return null;
                break;
            case NONLITERAL :
                if ( ! n.isLiteral() )
                    return null;
                break;
            default :
//                data.isNodeTriple()
//                data.isNodeGraph()
                break;
        }
        // Bad.
        String msg = toString()+" : Expected "+nodeKind.toString()+" for "+displayStr(n);
        return new ReportItem(msg, n);
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nFmt) {
        out.println(toString());
    }

    @Override
    public void visit(NodeConstraintVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "NodeKind: "+nodeKind.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeKind);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        NodeKindConstraint other = (NodeKindConstraint)obj;
        return nodeKind == other.nodeKind;
    }
}
