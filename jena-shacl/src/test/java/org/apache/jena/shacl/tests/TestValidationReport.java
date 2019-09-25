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

package org.apache.jena.shacl.tests;

import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.validation.VR;
import org.junit.Test;

public class TestValidationReport {

    // Round trip :: RDF -> Validation Report -> RDF
    @Test public void rtRDF_1() {
        Graph graph = RDFDataMgr.loadGraph("src/test/resources/validation-reports/report1-conforms.ttl");
        testRoundTripGraph("conforms", graph);
    }

    @Test public void rtRDF_2() {
        Graph graph = RDFDataMgr.loadGraph("src/test/resources/validation-reports/report2.ttl");
        testRoundTripGraph("report2", graph);
    }

    private void testRoundTripGraph(String message, Graph graph1) {
        ValidationReport report = ValidationReport.fromGraph(graph1);
        Resource r = report.getResource();

        Graph graph2 = r.getModel().getGraph();

        boolean b = graph1.isIsomorphicWith(graph2);
        if ( ! b ) {
            System.out.println("++++");
            RDFDataMgr.write(System.out, graph1, Lang.TTL);
            System.out.println("----");
            RDFDataMgr.write(System.out, graph2, Lang.TTL);
            System.out.flush();
        }
        assertTrue("Does not match: "+message, b);
    }

    // Round trip :: ValidationReport -> RDF -> ValidationReport
    @Test public void rtReport_1() {
        ValidationReport report = ValidationReport.reportConformsTrue();
        testRoundTripReport("conforms", report);
    }

//    @Test public void rtReport_2() {
//        // two entries
//    }

    private void testRoundTripReport(String message, ValidationReport report1) {
        Resource r = report1.getResource();
        ValidationReport report2 = ValidationReport.fromModel(r.getModel());
        boolean b = VR.compare(report1, report2);
        if ( ! b ) {
            System.out.println("****");
            ShLib.printReport(report1);
            System.out.println("----");
            ShLib.printReport(report2);
            System.out.flush();
        }
        assertTrue("Reports differ: "+message, b);
    }
}
