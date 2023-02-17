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

package org.apache.jena.rdfxml.xmlinput;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.jena.irix.IRIException;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

/** Rewritten WG test suite runner. */
public class TestSuiteWG_2 {

    private static String DIR = "testing/";

    private static Resource typePositive = ResourceFactory.createResource("http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#PositiveParserTest");
    private static Resource typeNegative = ResourceFactory.createResource("http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#NegativeParserTest");

    private static Property inputDocument  = ResourceFactory.createProperty("http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#inputDocument");
    private static Property outputDocument = ResourceFactory.createProperty("http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#outputDocument");

    static int countTests = 0;

    static int positiveTests = 0;
    static int positivePassTests = 0;
    static int positiveFailTests = 0;

    static int negativeTests = 0;
    static int negativeGoodTests = 0;
    static int negativeBadTests = 0;

    static abstract class ParserTest{
        protected Model model;
        protected Resource test;
        protected Resource input;
        protected Resource output;
        ParserTest(Model m, Resource test, Resource input, Resource output) {
            this.model = m;
            this.test = test;
            this.input = input;
            this.output = output;
        }

        protected abstract void runTest();

        protected String asURL(Resource r) {
            return "file:///home/afs/ASF/jena-project/jena-core/"+asFilename(r);
        }

        protected String asFilename(Resource r) {
            if ( r.getURI().startsWith("http://www.w3.org/2000/10/rdf-tests/rdfcore/") ) {
                return DIR+"wg/"+r.getURI().substring("http://www.w3.org/2000/10/rdf-tests/rdfcore/".length());
            }
            if ( r.getURI().startsWith("http://jcarroll.hpl.hp.com/arp-tests/") ) {
                return DIR+"arp/"+r.getURI().substring("http://jcarroll.hpl.hp.com/arp-tests/".length());
            }
            throw new RuntimeException("Not recognized: "+r);
        }

    }


    static class ParserTestPositive extends ParserTest {
        public ParserTestPositive(Model m, Resource test, Resource input, Resource output) {
            super(m, test, input, output);
        }

        @Override
        protected void runTest() {
            System.out.println("Postive: "+test);
            String inFile = asURL(input);
            Model m1 = ModelFactory.createDefaultModel();

            positiveTests ++;

            try {
                // Default error handler
                RDFXMLReader reader = new RDFXMLReader();
                reader.setErrorHandler(new ErrorHandler());
                InputStream inStream = new FileInputStream(asFilename(input));
                reader.read(m1, inStream, input.getURI());
            } catch (TestException ex) {
                System.out.println("== BAD "+inFile);
                System.out.println("== ** "+ex.getMessage());
                positiveFailTests++;
                return;
            } catch (IOException ex) {
                System.out.println("== IO "+inFile);
                System.out.println("== ** "+ex.getMessage());
                positiveFailTests++;
                return;
            } catch (IRIException ex) {
                System.out.println("== IRIx exception "+inFile);
                System.out.println("== ** "+ex.getMessage());
                positiveFailTests++;
                return;
            }


            Model m2 = ModelFactory.createDefaultModel();
            String outFile = asURL(output);
            String filetype = "N-TRIPLES";
            if (outFile.endsWith(".rdf") )
                filetype = "RDF/XML";
            m2.read(outFile, null, filetype);

            if ( ! m1.isIsomorphicWith(m2) ) {
                positiveFailTests++;
                System.out.println("== BAD");
                System.out.println("==  In:  "+inFile);
                System.out.println("==  Out: "+outFile);
                m1.write(System.out, "N-TRIPLES");
                System.out.println("---");
                m2.write(System.out, "N-TRIPLES");
                System.out.println("===");
            } else
                positivePassTests++;

            //assertTrue(m1.isIsomorphicWith(m2));
        }
    }

    static class ParserTestNegative extends ParserTest {
        public ParserTestNegative(Model m, Resource test, Resource input) {
            super(m, test, input, null);
        }

        @Override
        protected void runTest() {
            System.out.println("Negative: "+test);
            String inFile = asURL(input);
            Model m1 = ModelFactory.createDefaultModel();
            negativeTests++;
            try {
                // Default error handler
                RDFXMLReader reader = new RDFXMLReader();
                reader.setErrorHandler(new ErrorHandler());
                reader.read(m1, inFile);
                System.out.println("== Passed but should have failed: "+inFile);
                System.out.println("== Input:  "+inFile);
                negativeBadTests++;
            } catch (TestException ex) {
                negativeGoodTests++;
            } catch (IRIException ex) {
                System.out.println("== IRIx exception "+inFile);
                System.out.println("== ** "+ex.getMessage());
                negativeGoodTests++;
                return;
            }
        }
    }

    static class TestException extends RuntimeException {
        TestException(Exception e) {
            super(e);
        }
    }


    static class ErrorHandler implements RDFErrorHandler {
        @Override
        public void warning(Exception e) {
            throw new TestException(e);
        }

        @Override
        public void error(Exception e) {
            throw new TestException(e);
        }

        @Override
        public void fatalError(Exception e) {
            throw new TestException(e);
        }
    }

    public static void main(String[] args) {
        build();
        System.out.println();
        System.out.printf("Tests:          %3d\n", countTests);
        System.out.printf("Positive tests: %3d : %3d / %2d\n", positiveTests, positivePassTests, positiveFailTests);
        System.out.printf("Negative tests: %3d : %3d / %2d\n", negativeTests, negativeGoodTests, negativeBadTests);

    }

    static void build() {
        List<String> manifestFiles = List.of("wg/Manifest.rdf"
                                            ,"wg/Manifest-extra.rdf"
                                          //,"wg/Manifest-from-web.rdf"
                                          //,"wg/Manifest-orig.rdf"
                                            ,"arp/Manifest.rdf"
                                          //,"Manifest-wrong.rdf"
                );

        //manifestFiles = List.of("arp/Manifest.rdf");

        String onetest = null;


        for ( String manifest : manifestFiles ) {
            Model m = load(manifest);

            // Positive tests
            ResIterator rIter1 = m.listSubjectsWithProperty(RDF.type, typePositive);
            while(rIter1.hasNext() ) {
                Resource x = rIter1.next();
                // Bad format in manifest.
                if ( x.getURI().equals("http://jcarroll.hpl.hp.com/arp-tests/rdf-nnn/67_5"))
                    continue;
                if ( onetest != null ) {
                    // Choose a test
                    if ( ! x.getURI().equals(onetest))
                        continue;
                }

                countTests++;

                Resource input = x.getRequiredProperty(inputDocument).getObject().asResource();
                Resource output = x.getRequiredProperty(outputDocument).getObject().asResource();
                new ParserTestPositive(m, x, input, output).runTest();
            }

            if ( onetest != null )
                continue;
            // Negative tests
            ResIterator rIter2 = m.listSubjectsWithProperty(RDF.type, typeNegative);
            while(rIter2.hasNext() ) {
                countTests++;
                Resource x = rIter2.next();
                Resource input = x.getRequiredProperty(inputDocument).getObject().asResource();
                new ParserTestNegative(m, x, input).runTest();
            }
            System.out.println();
        }
    }

    private static Model load(String manifest) {
        Model m = ModelFactory.createDefaultModel();
        m.read("file:"+DIR+manifest);
        return m;
    }

}
