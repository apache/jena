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
import java.util.*;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.TargetType;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.system.G;
import org.apache.jena.system.RDFDataException;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/** Misc operations used in the jena-shacl module. */
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
        if ( shapes.getImports() != null ) {
            shapes.getImports().forEach(impt->{
                out.print("Import: ");
                nodeFmt.format(out, impt);
                out.println();
            });
        }
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
        PrintStream out = (output instanceof PrintStream pStream)
                ? pStream
                : new PrintStream(output);
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

    public static String displayStr(RDFDatatype dt) {
        Node n = NodeFactory.createURI(dt.getURI());
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

    /**
     * Test whether the IRI is a datatype that can be written in compact short form
     * (no {@code datatype=} written, only the datatype URI). This test is used by
     * the SHACL compact parser in {@code ShaclCompactParser.rPropertyType} and SHACL
     * compact writer.
     */
    public static boolean isDatatype(String iriStr) {
        return iriStr.startsWith(XSD.getURI()) || rdfDatatypes.contains(iriStr);
    }

    public static NodeFormatter nodeFormatter(Shapes shapes) {
        return new NodeFormatterTTL(shapes.getBaseURI(), shapes.getPrefixMap());
    }

    /**
     * Parse a string to produce a {@link Query}.
     * All {@link Query} should go through this function to allow of inserting default prefixes.
     */
    public static Query parseQueryString(String queryString) {
        try {
            Query query = new Query();
            // The SHACL spec does not define any default prefixes.
            // But for identified practical reasons some may be added such as:
    //        query.getPrefixMapping().setNsPrefix("owl",  OWL.getURI());
    //        query.getPrefixMapping().setNsPrefix("rdf",  RDF.getURI());
    //        query.getPrefixMapping().setNsPrefix("rdfs", RDFS.getURI());
    //        query.getPrefixMapping().setNsPrefix("sh",   SHACL.getURI());
    //        query.getPrefixMapping().setNsPrefix("xsd",  XSD.getURI());
            QueryFactory.parse(query, queryString, null,  Syntax.defaultQuerySyntax);
            return query;
        } catch (QueryParseException ex) {
            throw new ShaclParseException("Bad query: "+ex.getMessage());
        }
    }

    /** Parse a SPARQL query */
    public static Query extractSPARQLQuery(Graph shapesGraph, Node sparqlNode) {
        String qs = extractSPARQLQueryString(shapesGraph, sparqlNode);
        try {
            Query query = parseQueryString(qs);
            return query;
        } catch (QueryParseException ex) {
//            Log.warn("SHACL", "SPARQL parse error: "+ex.getMessage()+"\n"+qs);
//            return new SparqlConstraint(new Query(), msg);
            throw new ShaclParseException("SPARQL parse error: "+ex.getMessage()+"\n"+qs);
        }
    }

    public static String extractSPARQLQueryString(Graph shapesGraph, Node sparqlNode) {
        // XXX Optimize prefixes acquisition in case of use from more than one place.
        String prefixes = prefixes(shapesGraph, sparqlNode);
        Node selectNode;
        try {
            selectNode = G.getOneSP(shapesGraph, sparqlNode, SHACL.select);
        } catch (RDFDataException ex) {
            throw new ShaclParseException("required - one sh:select at : "+sparqlNode);
        }
        if ( ! Util.isSimpleString(selectNode) )
            throw new ShaclParseException("Not a string for sh:select at : "+ShLib.displayStr(selectNode));
        String selectQuery = selectNode.getLiteralLexicalForm();
        String qs = prefixes+"\n"+selectQuery;
        return qs;
    }

    private static String prefixesQueryString = StrUtils.strjoinNL
            ("PREFIX owl:     <http://www.w3.org/2002/07/owl#>"
            ,"PREFIX sh:      <http://www.w3.org/ns/shacl#>"
            ,"SELECT * { ?x sh:prefixes/owl:imports*/sh:declare [ sh:prefix ?prefix ; sh:namespace ?namespace ] }"
            );
    private static Query prefixesQuery = QueryFactory.create(prefixesQueryString);
    private static Var varPrefix = Var.alloc("prefix");
    private static Var varNamespace = Var.alloc("namespace");

    public static String prefixes(Graph shapesGraph, Node sparqlNode) {
        StringJoiner prefixesSJ = new StringJoiner("\n");
        QueryExecution qExec = QueryExecutionFactory.create(prefixesQuery, DatasetGraphFactory.wrap(shapesGraph));
        ResultSet rs = qExec.execSelect();
        Map<String, String> seen = new HashMap<>();

        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            Node nPrefix = binding.get(varPrefix);
            // Strictly, namespace must be xsd:anyURI.
            // We accept any literal, and also URIs
            // which makes [ sh:prefix "ex" ; sh:namespace ex: ] work.
            Node nNamespace = binding.get(varNamespace);
            String prefix = nPrefix.getLiteralLexicalForm();
            String ns;
            if ( nNamespace.isLiteral() )
                ns = nNamespace.getLiteralLexicalForm();
            else if ( nNamespace.isURI() )
                ns = nNamespace.getURI();
            else
                throw new ShaclParseException("sh:namespace is not  a literal or URI");
            if ( seen.containsKey(prefix) ) {
                if ( seen.get(prefix).equals(ns) )
                    continue;
            }
            prefixesSJ.add("PREFIX "+prefix+": <"+ns+">");
            seen.put(prefix, ns);
        }
        return prefixesSJ.toString();
    }
}
