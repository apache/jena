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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

import java.net.*;
import java.io.*;

import com.hp.hpl.jena.shared.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author  bwm
 * @version $Revision: 1.1 $
 */
public class testReaderInterface extends Object {


    protected static void doTest(Model m1) {
        (new testReaderInterface()).test(m1);
    }

    protected static Logger logger = LoggerFactory.getLogger( testReaderInterface.class );
    
    void test(Model m1) {

        String  test = "testReaderInterface";
        String  filebase = "testing/regression/" + test + "/";
    //    System.out.println("Beginning " + test);
        int n = 0;
        try {
            n++; RDFReader reader = m1.getReader();

            /*
            if (! (reader instanceof com.hp.hpl.jena.rdf.arp.JenaReader ))
                 error(test, n);

            n++; reader = m1.getReader("RDF/XML");
            if (! (reader instanceof com.hp.hpl.jena.rdf.arp.JenaReader ))
                 error(test, n);
            */

            n++; reader = m1.getReader("N-TRIPLE");
                 if (! (reader instanceof NTripleReader)) error(test, n);

            n++; try {
                    m1.setReaderClassName("foobar", "");
                    reader = m1.getReader("foobar");
                    error(test, n);
                 } catch (NoReaderForLangException jx) {
                     // that's what we expect
                 }

            n++; m1.setReaderClassName("foobar",
                                       com.hp.hpl.jena.rdf.arp.JenaReader.class.getName());
                 reader = m1.getReader("foobar");
                 if (! (reader instanceof com.hp.hpl.jena.rdf.arp.JenaReader)) error(test, n);

                 try {

                n++; m1.read("http://www.w3.org/2000/10/rdf-tests/rdfcore/"
                          +  "rdf-containers-syntax-vs-schema/test001.rdf");

                n++; m1.read("http://www.w3.org/2000/10/rdf-tests/rdfcore/"
                          +  "rdf-containers-syntax-vs-schema/test001.nt",
                             "N-TRIPLE");
                } catch (JenaException jx)
                    {
                    if (jx.getCause() instanceof NoRouteToHostException
                        || jx.getCause() instanceof UnknownHostException
                        || jx.getCause() instanceof ConnectException
                        || jx.getCause() instanceof IOException
                        )
                        {logger.warn("Cannot access public internet - part of test not executed" );
                        }
                    else
                        throw jx;
                    }


            n++; m1.read(ResourceReader.getInputStream(filebase + "1.rdf"), "http://example.org/");

            n++; m1.read(
                    ResourceReader.getInputStream(filebase + "2.nt"),  "", "N-TRIPLE");


        } catch (Exception e) {
            inError = true;
            logger.error( " test " + test + "[" + n + "]", e);
        }
      //  System.out.println("End of " + test);
    }
    private boolean inError = false;

    protected void error(String test, int n) {
        System.out.println(test + ": failed test " + Integer.toString(n));
        inError = true;
    }

    public boolean getErrors() {
        return inError;
    }

}
