/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.test;

import java.io.IOException ;
import java.net.ConnectException ;
import java.net.NoRouteToHostException ;
import java.net.UnknownHostException ;

import org.junit.Assert ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.NoReaderForLangException ;

public class TestReaders extends AbstractModelTestBase
{
    public TestReaders(final TestingModelFactory modelFactory, final String name) {
        super(modelFactory, name) ;
    }
	
    public TestReaders() {
        this(new TestPackage.PlainModelFactory(), "TestReaders") ;
    }

    /**
     * Test to ensure that the reader is set.
     */
    public void testGetNTripleReader() {
        final RDFReader reader = model.getReader("N-TRIPLE") ;
        Assert.assertNotNull(reader) ;
    }
	
    public void testMissingReader() {
        model.setReaderClassName("foobar", "") ;
        try {
            model.getReader("foobar") ;
            Assert.fail("Should have thrown NoReaderForLangException") ;
        }
        catch (final NoReaderForLangException expected) {
            // that's what we expect
        }

        try {
            model.setReaderClassName("foobar", com.hp.hpl.jena.rdfxml.xmlinput.JenaReader.class.getName()) ;
            final RDFReader reader = model.getReader("foobar") ;
            Assert.assertTrue("Wrong reader type", reader instanceof com.hp.hpl.jena.rdfxml.xmlinput.JenaReader) ;
        }
        finally {
            // unset the reader
            model.setReaderClassName("foobar", "") ;
        }
	}

    public void testReadLocalNTriple() {
        model.read(getInputStream("TestReaders.nt"), "", "N-TRIPLE") ;
        Assert.assertEquals("Wrong size model", 5, model.size()) ;
        final StmtIterator iter = model.listStatements(null, null, "foo\"\\\n\r\tbar") ;
        Assert.assertTrue("No next statement found", iter.hasNext()) ;
	}

    public void testReadLocalRDF() {
        model.read(getInputStream("TestReaders.rdf"), "http://example.org/") ;
    }

    public void testReadRemoteNTriple() {
        try {
            model.read("http://www.w3.org/2000/10/rdf-tests/rdfcore/" + "rdf-containers-syntax-vs-schema/test001.nt",
                       "N-TRIPLE") ;
        }
        catch (final JenaException jx) {
            if ( (jx.getCause() instanceof NoRouteToHostException) || (jx.getCause() instanceof UnknownHostException)
                 || (jx.getCause() instanceof ConnectException) || (jx.getCause() instanceof IOException) ) {
                noPublicInternet() ;
            } else {
                throw jx ;
            }
        }
    }

    public void testReadRemoteRDF() {
        try {
            model.read("http://www.w3.org/2000/10/rdf-tests/rdfcore/" + "rdf-containers-syntax-vs-schema/test001.rdf") ;
        }
        catch (final JenaException jx) {
            if ( (jx.getCause() instanceof NoRouteToHostException) || (jx.getCause() instanceof UnknownHostException)
                 || (jx.getCause() instanceof ConnectException) || (jx.getCause() instanceof IOException) ) {
                noPublicInternet() ;
            } else {
                throw jx ;
            }
        }
    }

    private void noPublicInternet() {
        LoggerFactory.getLogger(this.getClass()).warn("Cannot access public internet - part of test not executed");
	}
}
