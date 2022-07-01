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

package org.apache.jena.shex.runner;

import static org.junit.Assert.assertEquals;

import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shex.*;
import org.apache.jena.shex.sys.ShexLib;

/** A Shex validation test. Created by {@link RunnerShexValidation}.  */
public class ShexValidationTest implements Runnable {

    private final ManifestEntry entry;
    private final TestType testType;
    private final String schema;
    private final Node shape;
    private final Resource data;
    private final Node focus;
    private final ShexSchema shapes;
    private final String shapeMapURI;
    private final ShexMap shapeMap;
    private final boolean positiveTest;
    private final boolean verbose = false;

    enum TestType{ ShapeFocus, StartFocus, ShapeMap }

    // Expected: ShexT.schema, ShexT.shape, ShexT.data, ShexT.focus
    static Runnable testShexValidationShapeFocus(ManifestEntry entry) {
        Resource action = entry.getAction();
        Resource schema = action.getProperty(ShexT.schema).getResource();
        Resource shape = action.getProperty(ShexT.shape).getResource();
        Resource data = action.getProperty(ShexT.data).getResource();
        // URI or literal.
        RDFNode focus = action.getProperty(ShexT.focus).getObject();
        return new ShexValidationTest(entry,
                                      data, schema, shape, (String)null, focus,
                                      TestType.ShapeFocus);
    }
    // Expected: ShexT.schema (with start), ShexT.data, ShexT.focus
    static Runnable testShexValidationStartFocus(ManifestEntry entry) {
        Resource action = entry.getAction();
        Resource schema = action.getProperty(ShexT.schema).getResource();
        Resource data = action.getProperty(ShexT.data).getResource();
     // URI or literal.
        RDFNode focus = action.getProperty(ShexT.focus).getObject();
        return new ShexValidationTest(entry,
                                      data, schema, (Resource)null, (String)null, focus,
                                      TestType.StartFocus);
    }

    // Expected: ShexT.schema (with start), ShexT.map, ShexT.data
    static Runnable testShexValidationMap(ManifestEntry entry) {
        Resource action = entry.getAction();
        Resource schema = action.getProperty(ShexT.schema).getResource();
        Resource map = action.getProperty(ShexT.map).getResource();
        Resource data = action.getProperty(ShexT.data).getResource();
        return new ShexValidationTest(entry,
                                      data, schema, (Resource)null, map.getURI(), null,
                                      TestType.ShapeMap);
    }

    public ShexValidationTest(ManifestEntry entry,
                              Resource data, Resource schema, Resource shape, String shapeMapRef, RDFNode focus,
                              TestType testType) {
        // For reading data and schema with the same base
        String base = null;
        if ( entry.getEntry().isURIResource() ) {
            String fn = IRILib.IRIToFilename(entry.getEntry().getURI());
            int idx = fn.lastIndexOf('/');
            if ( idx > 0 )
                base = fn.substring(0,idx+1);
            base = IRILib.filenameToIRI(base);
        }

        this.entry = entry;
        this.testType = testType;
        this.schema = schema.getURI();
        this.data  = data;
        this.shape = (shape!=null) ? shape.asNode() : null;
        this.focus = (focus == null) ? null : focus.asNode();
        this.shapeMapURI = shapeMapRef;
        this.shapeMap = (shapeMapRef == null)
                ? null
                : Shex.readShapeMapJson(shapeMapRef);
        this.shapes = Shex.readSchema(schema.getURI(), base);
        this.positiveTest = entry.getTestType().equals(ShexT.cValidationTest);
    }

    @Override
    public void run() {
        Graph graph = RDFDataMgr.loadGraph(data.getURI());
        try {
            if ( ShexTests.dumpTest )
                describeTest();
            ShexReport report;
            switch (this.testType) {
                case ShapeFocus :
                    report = ShexValidator.get().validate(graph, shapes, shape, focus);
                    break;
                case ShapeMap :
                    report = ShexValidator.get().validate(graph, shapes, shapeMap);
                    break;
                case StartFocus : {
                    ShexShape startShape = shapes.getStart();
                    report = ShexValidator.get().validate(graph, shapes, startShape, focus);
                    break;
                }
                default:
                    throw new InternalErrorException("No test type");
            }

            boolean b = (positiveTest == report.conforms());
            if ( !b ) {
                if ( ! ShexTests.dumpTest )
                    describeTest();
            }
            assertEquals(entry.getName(), positiveTest, report.conforms());
        } catch (java.lang.AssertionError ex) {
            throw ex;
        } catch (Throwable ex) {
            describeTest();
            System.out.println("Exception: "+ex.getMessage());
            if ( ! ( ex instanceof Error ) )
                ex.printStackTrace(System.out);
            else
                System.out.println(ex.getClass().getName());
            Shex.printSchema(shapes);
            throw ex;
        }
    }

    private void describeTest() {
        System.out.println("** "+ShexTests.fragment(entry));
        System.out.println("Schema:   "+schema);
        System.out.println("Data:     "+data);

        if ( shape != null )
            System.out.println("Shape:    "+ShexLib.displayStr(shape));
        if ( focus != null )
            System.out.println("Focus:    "+ShexLib.displayStr(focus));
        if ( shapeMapURI != null )
            System.out.println("Map:      "+shapeMapURI);

        System.out.println("Positive: "+positiveTest);
        {
            String fn = IRILib.IRIToFilename(schema);
            String s = IO.readWholeFileAsUTF8(fn);
            System.out.println("-- Schema:");
            System.out.print(s);
            if ( ! s.endsWith("\n") )
                System.out.println();
        }
        if ( shapeMapURI != null ) {
            String fn = IRILib.IRIToFilename(shapeMapURI);
            String s = IO.readWholeFileAsUTF8(fn);
            System.out.println("-- Shape map:");
            System.out.print(s);
            if ( ! s.endsWith("\n") )
                System.out.println();
        }
        {
            String dfn = IRILib.IRIToFilename(data.getURI());
            String s = IO.readWholeFileAsUTF8(dfn);
            System.out.println("-- Data:");
            System.out.print(s);
            if ( ! s.endsWith("\n") )
                System.out.println();
            System.out.println("-- --");
        }
        Shex.printSchema(shapes);

        System.out.println("-- --");
    }
}
