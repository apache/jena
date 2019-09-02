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

package org.apache.jena.shacl.validation;

import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;

import org.apache.jena.atlas.lib.CollectionUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.shacl.ShaclException;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.vocabulary.SHACLM;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDF;

/**
 * A report entry.
 */
public class ReportEntry {
    //    3.6 Validation Report
    //    3.6.1 Validation Report (sh:ValidationReport)
    //        3.6.1.1 Conforms (sh:conforms)
    //        3.6.1.2 Result (sh:result)
    //        3.6.1.3 Syntax Checking of Shapes Graph (sh:shapesGraphWellFormed)
    //    3.6.2 Validation Result (sh:ValidationResult)
    //        3.6.2.1 Focus node (sh:focusNode)
    //        3.6.2.2 Path (sh:resultPath)
    //        3.6.2.3 Value (sh:value)
    //        3.6.2.4 Source (sh:sourceShape)
    //        3.6.2.5 Constraint Component (sh:sourceConstraintComponent)
    //        3.6.2.6 Details (sh:detail)
    //        3.6.2.7 Message (sh:resultMessage)
    //        3.6.2.8 Severity (sh:resultSeverity)

    private Node focusNode = null;
    private Path resultPath;
    private Node value;
    private Node source;
    private Constraint constraint;
    private Node sourceConstraint;
    private Node sourceConstraintComponent;
    private Severity severity;
    private final Collection<Node> messages = new ArrayList<>(5);
    private Node detail;
    // Extras
    private Triple triple;

    private ReportEntry() {}

    public static ReportEntry create() {
        return new ReportEntry();
    }

//        sh:result    [ a                             sh:ValidationResult ;
//            sh:focusNode                  ex:x ;
//            sh:resultMessage              "Value does not have class http://example/ns#T" ;
//            sh:resultSeverity             sh:Violation ;
//            sh:sourceConstraintComponent  sh:ClassConstraintComponent ;
//            sh:sourceShape                shx:class_T ;
//            sh:value                      ex:x
//          ]
//    sh:result [
//               a sh:ValidationResult ;
//               sh:resultSeverity sh:Violation ;
//               sh:focusNode ex:Bob ;
//               sh:resultPath ex:age ;
//               sh:value "twenty two" ;
//               sh:resultMessage "ex:age expects a literal of datatype xsd:integer." ;
//               sh:sourceConstraintComponent sh:DatatypeConstraintComponent ;
//               sh:sourceShape ex:PersonShape-age ;
//           ]

    public Node focusNode() { return focusNode; }

    public ReportEntry focusNode(Node node) {
        this.focusNode = node;
        return this;
    }

    public Path resultPath() { return resultPath; }

    public ReportEntry resultPath(Path path) {
        this.resultPath = path;
        return this;
    }

    public Triple triple() { return triple; }

    public ReportEntry triple(Triple triple) {
        this.triple = triple;
        return this;
    }

    public Node value() { return value; }

    public ReportEntry value(Node node) {
        this.value = node;
        return this;
    }

    public Node source() { return source; }

    public ReportEntry source(Node node) {
        this.source = node;
        return this;
    }

    public Constraint constraint() { return constraint; }

    public ReportEntry constraint(Constraint constraint) {
        this.constraint = constraint;
        if ( constraint != null )
            sourceConstraintComponent(constraint.getComponent());
        return this;
    }

    public Node sourceConstraint() { return sourceConstraint; }

    public ReportEntry sourceConstraint(Node sourceConstraint) {
        this.sourceConstraint = sourceConstraint;
        return this;
    }

    public Constraint sourceConstraintComponent() { return constraint; }

    public ReportEntry sourceConstraintComponent(Node component) {
        this.sourceConstraintComponent = component;
        return this;
    }

    public Node detail() { return detail; }

    public ReportEntry detail(Node n) {
        detail = n;
        return this;
    }

    public ReportEntry message(Node node) {
        if ( node != null )
            messages.add(node);
        return this;
    }

    public ReportEntry message(String msg) {
        if ( msg != null )
            messages.add(NodeFactory.createLiteral(msg));
        return this;
    }

    public String message() {
        if ( messages() != null && messages().size() == 1 )
            return asString(CollectionUtils.oneElt(messages));
        else {
            StringJoiner sj = new StringJoiner(",", "[","]");
            for ( Node n : messages )
                sj.add(NodeFmtLib.displayStr(n));
            return sj.toString();
        }
    }

    public Collection<Node> messages() { return messages; }

    private static String asString(Node message) {
        if ( message == null )
            return null;
        if ( message.isLiteral() )
            return message.getLiteralLexicalForm();
        else if ( message.isURI() )
            return message.getURI();
        else
            throw new ShaclException("Not a literal or URI: "+message);
    }

    public Severity severity() { return severity; }

    public ReportEntry severity(Severity severity) {
        this.severity = severity;
        return this;
    }

    public ReportEntry severity(Node severity) {
        this.severity = (severity == null) ? null : Severity.create(severity);
        return this;
    }

    public void generate(Model model, Resource report) {
        Resource entry = model.createResource();
        entry.addProperty(RDF.type, SHACLM.ValidationResult);
        report.addProperty(SHACLM.result, entry);

        Function<Node, RDFNode> rdfNode = (gn)->ModelUtils.convertGraphNodeToRDFNode(gn, entry.getModel());
        Function<String, RDFNode> str = (s)->entry.getModel().createLiteral(s);

        // Section 3.6.2
        // The properties sh:focusNode, sh:resultSeverity and
        // sh:sourceConstraintComponent are the only properties that are mandatory
        // for all validation results.

        Objects.requireNonNull(focusNode,"focus node");
        //Objects.requireNonNull(message,"message");
        Objects.requireNonNull(severity,"severity");
        Objects.requireNonNull(sourceConstraintComponent, "sourceConstraintComponent");

        entry.addProperty(SHACLM.focusNode,       rdfNode.apply(focusNode));
        entry.addProperty(SHACLM.resultSeverity,  rdfNode.apply(severity.level()));
        entry.addProperty(SHACLM.sourceConstraintComponent, rdfNode.apply(sourceConstraintComponent));

        for ( Node msg : messages )
            entry.addProperty(SHACLM.resultMessage, rdfNode.apply(msg));
        if ( sourceConstraint != null )
            entry.addProperty(SHACLM.sourceConstraint, rdfNode.apply(sourceConstraint));
        if ( source != null )
            entry.addProperty(SHACLM.sourceShape, rdfNode.apply(source));
        if ( resultPath != null ) {
            Node pn = ShaclPaths.pathToRDF(model.getGraph()::add, resultPath);
            entry.addProperty(SHACLM.resultPath, rdfNode.apply(pn));
        }
        if ( value != null )
            entry.addProperty(SHACLM.value, rdfNode.apply(value));
    }

    @Override
    public String toString() {
        if ( resultPath() != null )
            return String.format("Node=%s\n  Path=%s\n  %s", displayStr(focusNode()), resultPath(), message());
        else
            return String.format("Node=%s\n  %s",displayStr(focusNode()), message());


//        if ( resultPath() != null )
//            return String.format("Node=%s : Path=%s : %s", displayStr(focusNode()), resultPath(), message());
//        else
//            return String.format("Node=%s : %s",displayStr(focusNode()), message());
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraint, detail, focusNode, messages, resultPath, severity, source, sourceConstraint,
            sourceConstraintComponent, triple, value);
    }

    // Same mandatory fields.
    public boolean same(ReportEntry obj) {
        return Objects.equals(focusNode, obj.focusNode)
            //&& Objects.equals(severity, obj.severity)
            && severity == obj.severity
            && Objects.equals(sourceConstraintComponent, obj.sourceConstraintComponent)
            && Objects.equals(resultPath, obj.resultPath);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof ReportEntry) )
            return false;
        ReportEntry other = (ReportEntry)obj;
        return Objects.equals(constraint, other.constraint)
            && Objects.equals(detail, other.detail)
            && Objects.equals(focusNode, other.focusNode)
            && Objects.equals(messages, other.messages)
            && Objects.equals(resultPath, other.resultPath)
            && severity == other.severity
            && Objects.equals(source, other.source)
            && Objects.equals(sourceConstraint, other.sourceConstraint)
            && Objects.equals(sourceConstraintComponent, other.sourceConstraintComponent)
            && Objects.equals(triple, other.triple)
            && Objects.equals(value, other.value);
    }
}