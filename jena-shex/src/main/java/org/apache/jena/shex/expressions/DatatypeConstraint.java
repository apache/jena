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

import static org.apache.jena.shex.sys.ShexLib.*;

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shex.sys.ReportItem;
import org.apache.jena.shex.sys.ValidationContext;
// ----
import org.apache.jena.vocabulary.XSD;

public class DatatypeConstraint extends NodeConstraintComponent {
    private final Node datatype;
    private final String dtURI;
    private final RDFDatatype rdfDatatype;

    public DatatypeConstraint(Node dt) {
        this( dt.isURI() ? dt.getURI() : null , Objects.requireNonNull(dt));
        if ( ! dt.isURI() )
            throw new IllegalArgumentException("Not a URI: "+dt);
    }

    public DatatypeConstraint(String dt) {
        this(Objects.requireNonNull(dt), NodeFactory.createURI(dt));
    }

    private DatatypeConstraint(String dtURI, Node dt) {
        this.datatype = dt;
        this.dtURI = dtURI;
        this.rdfDatatype = NodeFactory.getType(dtURI);
    }

    public String getDatatypeURI() {
        return dtURI;
    }

    public RDFDatatype getRDFDatatype() {
        return rdfDatatype;
    }

    @Override
    public ReportItem nodeSatisfies(ValidationContext vCxt, Node n) {
        if ( n.isLiteral() && dtURI.equals(n.getLiteralDatatypeURI()) ) {
            // Must be valid for the type
            if ( ! rdfDatatype.isValid(n.getLiteralLexicalForm()) ) {
                String errMsg = toString()+" : Not valid value : Node "+displayStr(n);
                return new ReportItem(errMsg, n);
            }
            return null;
        }

        if ( ! n.isLiteral() )
            return new ReportItem(toString()+" : Not a literal", n);
        //String dtStr = vCxt.getShapesGraph().getPrefixMapping().qnameFor(dtURI);
        Node dt = NodeFactory.createURI(n.getLiteralDatatypeURI());
        String errMsg = toString()+" -- Wrong datatype: "+strDatatype(n)+" for focus node: "+displayStr(n);
        return new ReportItem(errMsg, n);
    }

    @Override
    public void print(IndentedWriter out, NodeFormatter nFmt) {
        out.print("Datatype[");
        if ( datatype.isBlank() ) {
            out.print("<_:");
            out.print(datatype.getBlankNodeLabel());
            out.print(">");
        } else if ( datatype.isURI() && dtURI.startsWith(XSD.getURI()) ) {
            out.print("xsd:");
            out.print(datatype.getLocalName());
        } else {
            nFmt.format(out, datatype);
        }
        out.println("]");
    }

    @Override
    public void visit(NodeConstraintVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        String x;
        if ( datatype.isURI() ) {
            if ( dtURI.startsWith(XSD.getURI()) )
                x = "xsd:"+datatype.getLocalName();
            else
                x = "<"+dtURI+">";
        } else if ( datatype.isBlank() )
            x = "<_:"+datatype.getBlankNodeLabel()+">";
        else
            x = displayStr(datatype);

        return "Datatype["+x+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(datatype, dtURI);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof DatatypeConstraint) )
            return false;
        DatatypeConstraint other = (DatatypeConstraint)obj;
        return Objects.equals(datatype, other.datatype) && Objects.equals(dtURI, other.dtURI);
    }
}
