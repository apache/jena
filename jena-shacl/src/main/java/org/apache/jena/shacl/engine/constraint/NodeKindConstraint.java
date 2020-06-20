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

import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;

/** sh:nodeKind */

public class NodeKindConstraint extends ConstraintTerm {

    //sh:NodeKind: sh:BlankNode, sh:IRI, sh:Literal sh:BlankNodeOrIRI, sh:BlankNodeOrLiteral and sh:IRIOrLiteral.

    private final Node kind;
    private final boolean canBeIRI;
    private final boolean canBeBlankNode;
    private final boolean canBeLiteral;

    public NodeKindConstraint(Node kind) {
        Objects.requireNonNull(kind);
        if ( ! kind.isURI() )
            throw new IllegalArgumentException("NodeKindConstraint; not an IRI for the kind kind");
        this.kind = kind;
        this.canBeIRI       = kind.equals(SHACL.IRI)       || kind.equals(SHACL.BlankNodeOrIRI)     || kind.equals(SHACL.IRIOrLiteral);
        this.canBeBlankNode = kind.equals(SHACL.BlankNode) || kind.equals(SHACL.BlankNodeOrIRI)     || kind.equals(SHACL.BlankNodeOrLiteral);
        this.canBeLiteral   = kind.equals(SHACL.Literal)   || kind.equals(SHACL.BlankNodeOrLiteral) || kind.equals(SHACL.IRIOrLiteral);

        if ( ! canBeIRI && ! canBeBlankNode && ! canBeLiteral )
            throw new IllegalArgumentException(
                "NodeKind["+kind.getLocalName()+"] : "+
                "not one of sh:BlankNode, sh:IRI, sh:Literal sh:BlankNodeOrIRI, sh:BlankNodeOrLiteral and sh:IRIOrLiteral");
    }

    public  Node getKind() { return kind; } 
    
    @Override
    public void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        String s = getKind().getLocalName();
        out.print(s);
    }
    
    @Override
    public ReportItem validate(ValidationContext vCxt, Node n) {
        if ( canBeIRI && n.isURI() )          return null;
        if ( canBeBlankNode && n.isBlank() )  return null;
        if ( canBeLiteral && n.isLiteral() )  return null;
        String msg = toString()+" : Expected "+kind.getLocalName()+" for "+displayStr(n);
        return new ReportItem(msg, n);
    }

    @Override
    public Node getComponent() {
        return SHACL.NodeKindConstraintComponent;
    }

    @Override
    public void print(IndentedWriter out) {
        out.print(toString());
    }

    @Override
    public String toString() {
        return "NodeKind["+kind.getLocalName()+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(canBeBlankNode, canBeIRI, canBeLiteral, kind);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof NodeKindConstraint) )
            return false;
        NodeKindConstraint other = (NodeKindConstraint)obj;
        return canBeBlankNode == other.canBeBlankNode && canBeIRI == other.canBeIRI && canBeLiteral == other.canBeLiteral
               && Objects.equals(kind, other.kind);
    }

}
