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

package org.apache.jena.shacl.testing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.validation.VR;
import org.apache.jena.shacl.validation.ValidationProc;

public class ShaclTest {

//    public static void shaclTest(ShaclTestItem test) {
//        shaclTest(test, false, true);
//    }
//
    public static void shaclTest(ShaclTestItem test, boolean verbose) {
        Graph shapesGraph = RDFDataMgr.loadGraph(test.getShapesGraph().getURI());
        if ( false ) {
            // This has one error report for testing/std/core/path/path-strange-002.ttl 
            validateShapes(shapesGraph, test.origin());
        }
        
        try {
            Graph dataGraph;
            if ( test.getShapesGraph().getURI().equals(test.getDataGraph().getURI()) )
                dataGraph = shapesGraph;
            else
                dataGraph = RDFDataMgr.loadGraph(test.getDataGraph().getURI());

            boolean generalFailure = test.isGeneralFailure();
            if ( generalFailure ) {
                try {
                    ValidationReport testReport = ValidationProc.simpleValidation(shapesGraph, dataGraph, verbose);
                    if ( testReport.conforms() )
                        fail("Expect a test failure: "+test.origin());
                } catch (RuntimeException ex) {
                    // Ignore.
                }
            }

            // Fails on unimplemented.
            ValidationReport vReportExpected = ValidationReport.fromGraph(test.getResult().getModel().getGraph());
            ValidationReport vReportGot;

            try {
                vReportGot = ValidationProc.simpleValidation(shapesGraph, dataGraph, verbose);
            } catch (Throwable th) {
                System.out.println("** Test : "+test.origin());
                throw th;
            }

            boolean b1 = VR.compare(vReportGot, vReportExpected);

            if ( !b1 ) {
                // expected report != actual report
                System.out.println("<< Test : "+test.origin());
                Shapes shapes = Shapes.parse(shapesGraph);
                ShLib.printShapes(shapes);
                System.out.println("++++ Expected:");
                ShLib.printReport(vReportExpected);
                System.out.println("++++ Got");
                ShLib.printReport(vReportGot);
                System.out.println("-----");
            }

            Model modelGot = VR.strip(vReportGot.getResource().getModel());
            Model modelExpected = VR.strip(vReportExpected.getResource().getModel());
            boolean b2 = modelGot.isIsomorphicWith(modelExpected);

            if ( ! b2 ) {
                // expected model != actual model
                System.out.println("**** Expected");
                RDFDataMgr.write(System.out, modelExpected, Lang.TTL);
                System.out.println("**** Got");
                RDFDataMgr.write(System.out, modelGot, Lang.TTL);
                System.out.println(">> Test : "+test.origin());
                System.out.println("-----");
            }

            assertTrue("Reports differ : "+test.origin(), b1);
            assertTrue("Report models differ", b2);

            // Compare reports.
        //} catch (NotImplemented x) {
        } catch (RuntimeException ex) {
//            ex.printStackTrace();
//            System.err.println(ex.getMessage());
//            RDFDataMgr.write(System.out, shapesGraph, Lang.TTL);
//            System.out.println();
//            List<Shape> shapeList = Parser.parse(shapesGraph);
//            ShLib.printShapes(shapeList);
            throw ex;
        }
    }

    
    private static Shapes shapes = Shapes.parse("std/shacl-shacl.ttl");
    /** Validate the shapes graph 
     * @param origin */ 
    private static void validateShapes(Graph shapesGraph, String origin) {
        ValidationReport report = ShaclValidator.get().validate(shapes, shapesGraph);
        if ( ! report.conforms() ) {
            System.out.println("Test: "+origin);
            ShLib.printReport(report.getResource());
        }
        
    }


}
