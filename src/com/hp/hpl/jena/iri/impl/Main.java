/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;

public class Main {

    private static final Class[] noParams = new Class[0];

    private static final Object[] noObjects = new Object[0];

    private static final Class[] strParams = new Class[] { String.class };

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
                        m.invoke(this, new String[] { args[i + 1] });
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
            Iterator it = iri.violations(true);
            while (it.hasNext()) {
                Violation v = (Violation) it.next();
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

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

