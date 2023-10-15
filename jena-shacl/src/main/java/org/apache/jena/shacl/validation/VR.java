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

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.sys.C;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.shacl.vocabulary.SHACLM;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.system.G;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

public class VR {

    public static boolean compare(ValidationReport report1, ValidationReport report2) {
        Model m1 = strip(report1.getResource().getModel());
        Model m2 = strip(report2.getResource().getModel());
        return m1.isIsomorphicWith(m2);
    }

    public static Model conformsModel() {
         return ModelFactory.createDefaultModel().add(conformsModel);
    }

    private static Model conformsModel = ModelFactory.createDefaultModel();
    static {
        Resource report = conformsModel.createResource(SHACLM.ValidationReport);
        report.addProperty(SHACLM.conforms, C.mTRUE);
    }

    public static void check(ValidationReport report) {
        // Internal test.
        // Also entries->model->entries but bnode, order issues.
        Model m = report.getResource().getModel();
        ValidationReport report2 = ValidationReport.fromModel(m);
        Model m2 = report2.getResource().getModel();
        boolean b = m.isIsomorphicWith(m2);
        if ( ! b ) {
            System.err.println("****");
            RDFDataMgr.write(System.err, m, Lang.TTL);
            System.err.println("++++ Round trip");
            RDFDataMgr.write(System.err, m2, Lang.TTL);
            System.err.println("----");
        }
    }

    private static String PREFIXES =  StrUtils.strjoinNL(
        "PREFIX owl:  <http://www.w3.org/2002/07/owl#>",
        "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
        "PREFIX sh:   <http://www.w3.org/ns/shacl#>",
        "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>",
        ""
        );
    private static void prefixes(PrefixMapping pmap) {
        pmap.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        pmap.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        pmap.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        pmap.setNsPrefix("sh",   "http://www.w3.org/ns/shacl#");
        pmap.setNsPrefix("xsd",  "http://www.w3.org/2001/XMLSchema#");
    }


//  rdf:type sh:ValidationReport
//  sh:result
//  sh:conforms
//  rdf:type sh:ValidationResult
//  sh:focusNode
//  sh:resultPath (including depending blank node structures)
//  sh:resultSeverity
//  sh:sourceConstraint
//  sh:sourceConstraintComponent
//  sh:sourceShape
//  sh:value



    private static String qs = StrUtils.strjoinNL
        (PREFIXES
            ,"CONSTRUCT {"
            , "    ?X a sh:ValidationReport ;"
            , "        sh:conforms ?conforms ;"
            , "        sh:result ?R"
            , "    ."
            , "    ?R"
            , "       sh:focusNode ?focusNode ;"
            , "       sh:resultMessage ?message ;"
            , "       sh:resultSeverity  ?severity ; "
            , "       sh:sourceConstraint ?constraint ;"
            , "       sh:sourceConstraintComponent ?component ;"
            , "       sh:sourceShape ?sourceShape ;"
            , "       sh:resultPath  ?path ;"
            , "       sh:value ?value ;"
            , "."
            ,"}"
            ," WHERE {"
            , "    ?X a sh:ValidationReport ;"
            , "       sh:result ?R"
            , "    OPTIONAL { ?X sh:conforms ?conforms }"
            , "    ?R"
            , "       sh:focusNode ?focusNode ;"
            , "       sh:resultSeverity  ?severity ; "
            , "       ."
            , "    OPTIONAL { ?R sh:resultMessage ?message }"
            , "    OPTIONAL { ?R sh:sourceConstraintComponent ?component }"
            , "    OPTIONAL { ?R sh:sourceConstraint ?constraint }"
            , "    OPTIONAL { ?R sh:sourceShape ?sourceShape }"
            , "    OPTIONAL { ?R sh:resultPath  ?path }"
            , "    OPTIONAL { ?R sh:value ?value }"
            ,"}");
    private static Query query = QueryFactory.create(qs);

    public static Model strip(Model model) {
        ExtendedIterator<Statement> iter = model.listStatements().filterKeep(stmt->keep(stmt));
        Model m = ModelFactory.createDefaultModel();
        iter.forEachRemaining(stmt->m.add(stmt));
        prefixes(m);

        Graph graph = model.getGraph();
        Graph g = m.getGraph();

        G.find(graph, null, SHACL.resultPath, null).toList().forEach(t->{
            Node obj = t.getObject();
            Node pn = ShaclPaths.copyPath(graph, g, obj);
            g.add(Triple.create(t.getSubject(), t.getPredicate(), pn));
        });
        return m;
    }

    static Set<Property> properties = new HashSet<>();
    static {
        // Report
        properties.add(SHACLM.result);
        properties.add(SHACLM.conforms);
        // Entries
        properties.add(SHACLM.focusNode);
        properties.add(SHACLM.resultSeverity);
        properties.add(SHACLM.sourceConstraintComponent);

        properties.add(SHACLM.value);
        // Do specially. properties.add(SHACLM.resultPath);

//        properties.add(SHACLM.sourceConstraint);
//        properties.add(SHACLM.sourceShape);
    }

    private static boolean keep(Statement stmt) {
        Property p = stmt.getPredicate();
        if ( properties.contains(p))
            return true;
        return p.equals(RDF.type) && SHACLM.ValidationReport.equals(stmt.getObject());
    }


}
