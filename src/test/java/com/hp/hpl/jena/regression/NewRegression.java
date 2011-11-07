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

package com.hp.hpl.jena.regression;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class NewRegression extends ModelTestBase
    {
    public NewRegression( String name )
        { super( name ); }

    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite( NewRegression.class ); 
        result.addTest( NewRegressionLiterals.suite() );
        result.addTest( NewRegressionResources.suite() );
        result.addTest( NewRegressionStatements.suite() );
        result.addTest( NewRegressionContainers.suite() );
        result.addTest( NewRegressionAddAndContains.suite() );
        result.addTest( NewRegressionGet.suite() );
        result.addTest( NewRegressionObjects.suite() );
        result.addTest( NewRegressionStatements.suite() );
        result.addTest( NewRegressionAddModel.suite() );
        result.addTest( NewRegressionListSubjects.suite() );
        result.addTest( NewRegressionSelector.suite() );
        result.addTest( NewRegressionSeq.suite() );
        result.addTest( NewRegressionSet.suite() );
        result.addTest( NewRegressionResourceMethods.suite() );
        result.addTest( NewRegressionStatementMethods.suite() );
        result.addTest( NewRegressionBagMethods.suite() );
        result.addTest( NewRegressionAltMethods.suite() );
        result.addTest( NewRegressionSeqMethods.suite() );
        return result;
        }
    
    public void testNothing()
        {}    

    }




//    /** test load from xml file
//     * @param m the model implementation under test
//     */
//    public void test18(Model m) {
//        String  test = "Test18";
//        if (test.equals( test )) return;
//        String  testURI = "http://aldabaran.hpl.hp.com/rdftest/test18/";
//        String  subject1 = testURI + "1";
//        String  object1 =
//   "<foo bar=\"bar\"><bar>abc<foobar/>def&lt;&gt;&apos;&quot;&amp;</bar></foo>";
//        String RDFSchemaURI = "http://lists.w3.org/Archives/Public/www-archive/"
//                            + "2001Sep/att-0064/00-rdfschema.rdf";
//        int     n = 0;
//
//        try {
//            System.out.println("Beginning " + test);
//            m.read(ResourceReader.getInputStream("modules/rdf/rdfschema.html"),
//                                      RDFSchemaURI);
//            n++; if (m.size() != 124) error(test, n);
//   //         n++; m.write(new PrintWriter(System.out));
//
//            StmtIterator iter = m.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                iter.remove();
//            }
//
//            m.read(ResourceReader.getInputStream("modules/rdf/embeddedxml.xml"), "");
//            n++;
// /* I'd like to test for the exactly correct value here, but can't since the
//  * exactly correct value is not defined.
//            if (! m.contains(m.createResource(subject1),
//                             RDF.value, object1)) error(test, n++);
//  * So instead lets do some rough checks its right */
//            String xml = m.getResource(subject1)
//                          .getRequiredProperty(RDF.value)
//                          .getString();
//            n++; if ( xml.indexOf("&lt;") == -1) error(test, n);
//            n++; if ( xml.indexOf("&gt;") == -1) error(test, n);
//            n++; if ( xml.indexOf("&amp;") == -1) error(test, n);
//            n++; if ((xml.indexOf("'bar'") == -1) &&
//                     (xml.indexOf("\"bar\"") == -1)) error(test, n);
//
//            m.createResource()
//             .addProperty(RDF.value, "can't loose");
//  //          m.write(new PrintWriter(System.out));
//
//            iter = m.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                iter.remove();
//            }
//            n++;
//            m.read(ResourceReader.getInputStream("modules/rdf/testcollection.rdf"), "");
//            if (m.size() != 24) error(test, (int) m.size());
//
//            iter = m.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                iter.remove();
//            }
//
//            try {
//                m.read(System.getProperty("com.hp.hpl.jena.regression.testURL",
//                                          RDFSchemaURI));
// //               n++; m.write(new PrintWriter(System.out));
//                n++; if ((m.size() != 124) && (m.size() != 125)) {
//                    System.out.println("size = " + m.size());
//                      error(test, n);
//                }
//                if (! m.contains(RDF.Property, RDF.type, RDFS.Class))
//                    error(test, n);
//            } catch (JenaException rdfx) {
//                Throwable th = rdfx.getCause();
//                if ( th instanceof NoRouteToHostException
//                 || th instanceof UnknownHostException
//                 || th instanceof IOException
//                 || th instanceof ConnectException) {
//                    logger.warn( "Cannot access public internet- part of test not executed" );
//                } else {
//                    throw rdfx;
//                }
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test moving things between models
//     * @param m the model implementation under test
//     */
//    public void test19(Model m1, Model m2) {
//        String  test = "Test19";
//        int     n = 0;
//
//        try {
//            Statement stmt;
//            StmtIterator sIter;
////            System.out.println("Beginning " + test);
//
//            try {
//                n=100;
//                Resource r11 = m1.createResource();
//                Resource r12 = m2.createResource(new ResTestObjF());
//                long size1 = m1.size();
//                long size2 = m2.size();
//
//                r11.addProperty(RDF.value, 1);
//                n++; if (! (m1.size() == ++size1)) error(test, n);
//                n++; if (! (m2.size() == size2)) error(test,n);
//
//                stmt = m2.createStatement(r11, RDF.value, r12);
//                n++; if (! (stmt.getSubject().getModel() == m2)) error(test,n);
//                n++; if (! (stmt.getResource().getModel() == m2)) error(test,n);
//
//                m1.add(stmt);
//                n++; if (! (m1.size() == ++size1)) error(test, n);
//                n++; if (! (m2.size() == size2)) error(test,n);
//
//                sIter = m1.listStatements(
//                                    new SimpleSelector(r11, RDF.value, r12));
//                n++; if (! sIter.hasNext()) error(test, n);
//                n++; stmt = sIter.nextStatement();
//                n++; if (! (stmt.getSubject().getModel() == m1)) error(test,n);
//                n++; if (! (stmt.getResource().getModel() == m1)) error(test,n);
//                sIter.close();
//
//
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//   /** Empty the passed in model
//     * @param m the model implementation under test
//     */
//    public void test20(Model m) {
//        String  test = "Test20";
//        int     n = 0;
//
//        try {
////            System.out.println("Beginning " + test);
//            Statement s1 = null;
//            Statement s2 = null;
//
//            try {
//                n=100;
//                n++; s1 = m.createStatement(m.createResource(),
//                                            RDF.type,
//                                            RDFS.Class);
//                n++; if (s1.isReified()) error(test,n);
//                n++; m.add(s1);
//                n++; if (s1.isReified()) error(test,n);
//                n++; s2 = m.createStatement(m.createResource(),
//                                            RDF.type,
//                                            RDFS.Class);
//                n++; if (s2.isReified()) error(test,n);
//                n++; m.add(s2);
//                n++; if (s2.isReified()) error(test,n);
///*
//                n++; m.add(s1, RDF.value, new LiteralImpl("foo"));
//                n++; if (!s1.isReified()) error(test,n);
//
//                n++; m.add(s1, RDF.value, s2);
//                n++; if (!s2.isReified()) error(test,n);
// */
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** Testing for miscellaneous bugs
//     * @param m the model implementation under test
//     */
//    public void test97(Model m) {
//        String  test = "Test97";
//        int     n = 0;
//
//        try {
//
////            System.out.println("Beginning " + test);
//
//                /*
//                    the _null_ argument to LiteralImpl was preserved only for backward
//                    compatability. It was be logged and has now become an exception.
//                    (Brian and Chris had a discussion about this and agreed).
//                */
//                // Node.nullLiteralsGenerateWarnings();
//                try
//                    {
//                   n=100; m.query(new SimpleSelector(null,
//                                                   null,
//                                                   new LiteralImpl( Node.createLiteral( null, "", false ), (ModelCom) m)));
//                    error( test, n );
//                    }
//                catch (NullPointerException e)
//                        {}
//                try
//                    {
//                   n=101; m.query(new SimpleSelector(null,
//                                                   null,
//                                                   new LiteralImpl( Node.createLiteral( null, "en", false ), (ModelCom) m)));
//                    error( test, n );
//                    }
//                catch (NullPointerException e)
//                    {}
//                // end of nullLiteralsGenerateWarnings code
//
//               n=102;
//               StmtIterator iter
//                            = m.listStatements(new SimpleSelector(null,
//                                                                null,
//                                                                (String) null));
//               while (iter.hasNext()) {
//                   RDFNode o = iter.nextStatement().getObject();
//               }
//
//               n=103;
//               iter = m.listStatements(new SimpleSelector(null,
//                                                        null,
//                                                        (Object) null));
//               while (iter.hasNext()) {
//                   RDFNode o = iter.nextStatement().getObject();
//               }
//
//            } catch (Exception e) {
//                error(test, n, e);
//            }
////        System.out.println("End of " + test);
//    }
//
//    /** Empty the passed in model
//     * @param m the model implementation under test
//     */
//    public void test99(Model m) {
//        String  test = "Test5";
//        int     n = 0;
//
//        try {
//            StmtIterator iter;
////            System.out.println("Beginning " + test);
//
//            try {
//                n=100;
//                n++; iter = m.listStatements();
//                while (iter.hasNext()) {
//                    iter.nextStatement();
//                    n++;    iter.remove();
//                }
//                n++; iter.close();
//                n++; if (! (m.size()==0)) error(test,999);
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
