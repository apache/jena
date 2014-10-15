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

package org.apache.jena.iri.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.Violation ;


public class Main {

    private static final Class<?>[] noParams = new Class[0];

    private static final Object[] noObjects = new Object[0];

    private static final Class<?>[] strParams = new Class[] { String.class };

    private boolean usedASpec = false;

    private IRIFactory factory = new IRIFactory();

    private InputStream in = System.in;

    private String specs;

    public void main(String[] args) {
        int i;
        try {
            for (i = 0; i < args.length; i++)
                if (args[i].charAt(0) == '-') {
                    try {
                        Method m = Main.class.getDeclaredMethod(args[i]
                                .substring(1), noParams);
                        m.invoke(this, noObjects);
                    } catch (NoSuchMethodException e) {

                        Method m;
                        try {
                            m = this.getClass().getDeclaredMethod(
                                    args[i].substring(1), strParams);
                        } catch (NoSuchMethodException e1) {
                            System.err.println("Unknown option: " + args[i]);
                            help();
                            return;
                        }
                        m.invoke(this, (Object[])new String[] { args[i + 1] });
                        i++;

                    }
                }

            if (!usedASpec)
                iri();
            factory.useSchemeSpecificRules("*",true);
            if (i < args.length)
                for (; i < args.length; i++)
                    check(args[i]);
            else {
                LineNumberReader rdr = new LineNumberReader(
                        new InputStreamReader(in));
                while (true) {
                    String line = rdr.readLine();
                    if (line == null)
                        return;
                    check(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void check(String string) {
        IRI iri = factory.create(string);
        if (iri.hasViolation(true)) {
            System.out.println("n: " + string);
            Iterator<Violation> it = iri.violations(true);
            while (it.hasNext()) {
                Violation v = it.next();
                System.out.println(v.getLongMessage());
            }
        } else {
            System.out.println("y: " + string);
        }
    }

    private void help() {

    }

    private void iri() {
        used("IRI", 0);
        factory.useSpecificationIRI(true);
    }

    private void uri() {
        used("URI", 0);
        factory.useSpecificationURI(true);

    }

    private void xml() {
        used("XML - systemID", 0);
        factory.useSpecificationXMLSystemID(true);

    }

    private void schema() {
        used("XML Schema - anyURI", 0);
        factory.useSpecificationXMLSchema(true);

    }

    private void xlink() {
        used("XLink - href attribute", 0);
        factory.useSpecificationXLink(true);
    }

    /**
     * 
     * @param string
     * @param i
     *            Simply to change signature, see {@link #main}
     */
    private void used(String string, int i) {
        usedASpec = true;
        if (specs == null)
            specs = string;
        else
            specs = specs + ", " + string;

    }

    private void rdf() {
        used("RDF URI Reference", 0);
        factory.useSpecificationRDF(true);
    }

    private void f(String file) {

    }
}
