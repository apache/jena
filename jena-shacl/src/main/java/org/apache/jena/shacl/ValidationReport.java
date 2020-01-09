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

package org.apache.jena.shacl;

import java.util.*;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.exec.TripleValidator;
import org.apache.jena.shacl.lib.G;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.sys.C;
import org.apache.jena.shacl.validation.ReportEntry;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.shacl.vocabulary.SHACLM;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class ValidationReport {

    private static ValidationReport singletonReportConformsTrue = new ValidationReport(Collections.emptySet(), Collections.emptyList(), null);
    private final Collection<ReportEntry> entries;
    private final Resource resultResource;

    public static Builder create() {
        return new Builder();
    }

    /** Return an immutable report that records no validation errors (violations or any other level of severity) */
    public static ValidationReport reportConformsTrue() {
        return singletonReportConformsTrue;
    }

    private ValidationReport(Set<Triple> paths, Collection<ReportEntry> entries, PrefixMapping prefixes) {
        this(entries, generate(paths, entries, prefixes));
    }

    private ValidationReport(Collection<ReportEntry> entries, Resource resultResource) {
        this.entries = entries;
        this.resultResource = resultResource;
    }

    public Collection<ReportEntry> getEntries() { return entries; }

    public Resource getResource() { return resultResource; }

    public Model getModel() { return resultResource.getModel(); }

    public Graph getGraph() {
        return getModel().getGraph();
    }

    public boolean conforms() { return entries.isEmpty(); }

    //  [   a sh:ValidationReport ;
    //      sh:conforms true ;
    //  ] .
    private static Resource reportConformsTrueResource() {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("sh", SHACLM.getURI());
        Resource report = model.createResource(SHACLM.ValidationReport);
        report.addProperty(SHACLM.conforms, C.mTRUE);
        return report;
    }

    private static Resource generate(Set<Triple> paths, Collection<ReportEntry> entries, PrefixMapping prefixes) {
        if ( entries.isEmpty() )
            return reportConformsTrueResource();
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("sh", SHACLM.getURI());
        if ( prefixes != null )
            model.setNsPrefixes(prefixes);
        Resource report = model.createResource(SHACLM.ValidationReport);
        entries.forEach(e->e.generate(model, report));
        paths.forEach(model.getGraph()::add);
        report.addProperty(SHACLM.conforms, C.mFALSE);
        return report;
    }

    public static ValidationReport fromGraph(Graph graph, Node node) {
        //RDFDataMgr.write(System.err,  graph, Lang.TTL);

        boolean conforms = G.contains(graph, node, SHACL.conforms, C.TRUE);
        List<Node> results = G.listSP(graph, node, SHACL.result);
        if ( conforms )
            return ValidationReport.create().build();
        List<ReportEntry> entries = new ArrayList<>();
        Set<Triple> paths = new HashSet<>();
        for ( Node r : results ) {
            Node pn = get(graph, r, SHACL.resultPath);
            Path path = ( pn != null ) ? ShaclPaths.parsePath(graph, pn) : null ;

            ReportEntry e = ReportEntry.create()
                .focusNode(         get(graph, r, SHACL.focusNode) )
                .resultPath(        path )
                .message(           get(graph, r, SHACL.resultMessage) )
                .severity(          get(graph, r, SHACL.resultSeverity) )
                .source(            get(graph, r, SHACL.sourceShape) )
                .sourceConstraint(  get(graph, r, SHACL.sourceConstraint) )
                .sourceConstraintComponent( get(graph, r, SHACL.sourceConstraintComponent) )
                .detail(            get(graph, r, SHACL.detail) )
                .value(             get(graph, r, SHACL.value) )
                //.triple(triple)
                ;
            entries.add(e);
        }
        // Path : need parse to extract.
        return new ValidationReport(paths, entries, graph.getPrefixMapping());
    }

    private static Node get(Graph g, Node r, Node p) {
        return G.getSP(g, r, p);
    }


    public static ValidationReport fromModel(Model model) {
        return fromGraph(model.getGraph());
    }

    public static ValidationReport fromGraph(Graph graph) {
        List<Node> reports = G.listPO(graph, C.rdfType, SHACL.ValidationReport);
        if ( reports.isEmpty() )
            throw new ShaclException("No report found in graph");
        if ( reports.size() > 1 )
            throw new ShaclException("Multiple reports found in graph");
        return fromGraph(graph, reports.get(0));
    }

    public static class Builder {
        private final List<ReportEntry> entries = new ArrayList<>();
        // For path triples.
        private final Set<Triple> paths = new HashSet<>();
        private PrefixMapping prefixes = new PrefixMappingImpl();

        public Builder() { }
        
        public void addPrefixes(PrefixMapping pmap) {
            this.prefixes.setNsPrefixes(pmap);
        }

        public void addReportEntry(ReportItem item, Shape shape, Node focusNode, Path path, Constraint constraint) {
            addReportEntry(item.getMessage(), shape, focusNode, path, item.getValue(), constraint);
        }

        public void addReportEntry(String message, Shape shape, Node focusNode, Path path, Node valueNode, Constraint constraint) {
            addReportEntryW(message, shape, null, focusNode, path, valueNode, constraint);
        }

        public void addReportEntry(ReportItem item, TripleValidator validator, Triple triple) {
            addReportEntryW(item.getMessage(), validator.getShape(), triple,
                            validator.getFocusNode(triple), validator.getPath(), item.getValue(), validator.getConstraint());
        }

        private void addReportEntryW(String message, Shape shape, Triple triple, Node focusNode, Path path, Node valueNode, Constraint constraint) {
            Collection<Node> messages;

            if ( shape.getMessages() != null && ! shape.getMessages().isEmpty() )
                messages = shape.getMessages();
            else
                messages = Collections.singleton(NodeFactory.createLiteral(message));

            ReportEntry e = ReportEntry.create()
                .focusNode(focusNode)
                .resultPath(path)
                .severity(shape.getSeverity())
                .source(shape.getShapeNode())
                .constraint(constraint)
                //.detail(null)
                .value(valueNode)
                .triple(triple)
                ;
            for ( Node x : messages )
                e.message(x);
            addReportEntry(e);
        }

        public void addReportEntry(ReportEntry e) {
            entries.add(e);
        }
        
        public ValidationReport build() {
            return new ValidationReport(paths, entries, prefixes);
        }

    }
}