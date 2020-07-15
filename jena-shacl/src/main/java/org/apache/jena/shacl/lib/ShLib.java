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

package org.apache.jena.shacl.lib;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.TargetType;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class ShLib {

    private static String PREFIXES =  StrUtils.strjoinNL(
        "PREFIX owl:  <http://www.w3.org/2002/07/owl#>",
        "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
        "PREFIX sh:   <http://www.w3.org/ns/shacl#>",
        "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>",
        ""
        );

    private static PrefixMap displayPrefixMap = PrefixMapFactory.createForOutput();
    static {
        displayPrefixMap.add("owl",  OWL.getURI());
        displayPrefixMap.add("rdf",  RDF.getURI());
        displayPrefixMap.add("rdfs", RDFS.getURI());
        displayPrefixMap.add("sh",   SHACL.getURI());
        displayPrefixMap.add("xsd",  XSD.getURI());
    }

    public static NodeFormatter nodeFmtAbbrev = new NodeFormatterTTL(null, displayPrefixMap);

    public static void printShapes(Graph shapeGraph) {
        printShapes(Shapes.parse(shapeGraph));
    }

    public static void printShapes(Shapes shapes) {
        printShapes(IndentedWriter.stdout, shapes);
        IndentedWriter.stdout.flush();
    }

    public static void printShapes(IndentedWriter out, Shapes shapes) {
        NodeFormatter nodeFmt = ShLib.nodeFormatter(shapes);
        printImports(out, nodeFmt, shapes);
        printShapes(out, nodeFmt, shapes);
        out.flush();
    }

    private static void printImports(IndentedWriter out, NodeFormatter nodeFmt, Shapes shapes) {
        shapes.getImports().forEach(impt->{
            out.print("Import: ");
            nodeFmt.format(out, impt);
            out.println();
        });
    }

    public static void printShapes(IndentedWriter out, NodeFormatter nodeFmt, Shapes shapes) {
        int indent = out.getAbsoluteIndent();
        shapes.iteratorAll().forEachRemaining(shape->shape.print(out, nodeFmt));
        out.setAbsoluteIndent(indent);
    }

    public static void print(Collection<Node> nodes) {
        nodes.stream().forEach(n->{
            if ( n.isURI() )
                System.out.println(n.getLocalName());
            else
                System.out.println(ShLib.displayStr(n));
        });
    }

    public static void printReport(ValidationReport report) {
        printReport(System.out, report);
    }

    public static void printReport(OutputStream output , ValidationReport report) {
        PrintStream out = output instanceof PrintStream ? (PrintStream)output : new PrintStream(output);
        if ( report.conforms() ) {
            out.println("Conforms");
            out.flush();
            return;
        }
        try {
            report.getEntries().forEach(e->{
                out.printf("Node=%s\n",displayStr(e.focusNode()));
                if ( e.resultPath() != null )
                    out.printf("  Path=%s\n", e.resultPath());
                if ( e.value() != null )
                    out.printf("  Value: %s\n", displayStr(e.value()));
                if ( e.message() != null )
                    out.printf("  Message: %s\n", e.message());
            });
        } finally { out.flush(); }
    }

    public static void printReport(Resource report) {
        //report.getModel().write(System.out, FileUtils.langTurtle);

        String qs = StrUtils.strjoinNL
            (PREFIXES
                ,"SELECT * {"
                //, "    [ a sh:ValidationReport ; sh:result ?R ]
                , "    [] sh:result ?R ."
                , "    ?R"
                , "       sh:focusNode ?focusNode ;"
                , "       sh:resultMessage ?message ;"
                , "       sh:resultSeverity  ?severity ; "
                , "       ."
                , "    OPTIONAL { ?R sh:sourceConstraintComponent ?component }"
                , "    OPTIONAL { ?R sh:sourceShape ?sourceShape }"
                , "    OPTIONAL { ?R sh:resultPath    ?path}"
                ,"}");
        try ( QueryExecution qExec = QueryExecutionFactory.create(qs, report.getModel()) ) {
            ResultSet rs = qExec.execSelect();
            if ( ! rs.hasNext() ) {
                System.out.println("No violations");
            } else {
                rs.forEachRemaining(row->{
                    RDFNode focusNode = row.get("focusNode");
                    String msg = row.getLiteral("message").getLexicalForm();
                    RDFNode pathNode = row.get("path");
                    if ( pathNode != null )
                        System.out.printf("Node=%s   Path=%s\n", displayStr(focusNode), pathNode);
                    else
                        System.out.printf("Node=%s\n",displayStr(focusNode));
                    System.out.printf("  %s\n", msg);

                    Path path = null;
                    if ( pathNode != null )
                        path = ShaclPaths.parsePath(report.getModel().getGraph(), pathNode.asNode());

                    // Better (?) to build a report entry.
//                    ReportEntry e = ReportEntry.create()
//                        .focusNode(focusNode.asNode())
//                        .resultPath(path)
//                        .message(msg)
//                        .severity(Severity.Violation)
//                        .source(shape.getShapeNode())
//                        .sourceConstraint(constraint)
//                        //.detail(null)
//                        .value(focusNode)
//                        .triple(triple)
//                        ;
                });
            }
        }
    }

    static String displayStr(RDFNode n) {
        return displayStr(n.asNode(), nodeFmtAbbrev);
    }

    public static String displayStr(Node n) {
        return displayStr(n, nodeFmtAbbrev);
    }

    public static String displayStr(Node n, NodeFormatter nodeFmt) {
        IndentedLineBuffer sw = new IndentedLineBuffer() ;
        nodeFmt.format(sw, n);
        return sw.toString() ;
    }

    public static boolean isImmediate(Target target) {
        TargetType targetType = target.getTargetType();
        return targetType.equals(TargetType.targetObjectsOf) || targetType.equals(TargetType.targetSubjectsOf);
    }

    static Set<String> rdfDatatypes = new HashSet<>();
    static {
        rdfDatatypes.add(RDF.dtLangString.getURI());
        rdfDatatypes.add(RDF.dtRDFHTML.getURI());
        rdfDatatypes.add(RDF.dtRDFJSON.getURI());
        rdfDatatypes.add(RDF.dtXMLLiteral.getURI());
    }



    /** Test whether the IRI is a datatype that can be written in compact short form (no datatype=)
     * This test us used by the SHACL compact parser in {@code ShaclCompactParser.rPropertyType}
     * and SHACL compact writer.
     */
    public static boolean isDatatype(String iriStr) {
        return iriStr.startsWith(XSD.getURI()) || rdfDatatypes.contains(iriStr);
    }


    public static Node focusNode(Triple triple, Target target) {
        switch(target.getTargetType()) {
            //case targetClass :
            case targetNode :
                return target.getObject();
            case targetObjectsOf :
                return triple.getObject();
            case targetSubjectsOf :
                return triple.getSubject();
            default :
        }
        return null;
    }

    public static NodeFormatter nodeFormatter(Shapes shapes) {
        PrefixMap pmap = PrefixMapFactory.create(shapes.getGraph().getPrefixMapping());
        return new NodeFormatterTTL(null, pmap);
    }
}
