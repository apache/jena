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

package org.apache.jena.iri ;

import java.net.MalformedURLException;

import junit.framework.JUnit4TestAdapter;

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.junit.Assert;
import org.junit.Test;


public class Additional
{
    // Test added in response to reports and bugs from 2009.
    
    static public junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(Additional.class) ;
    }
    
    @Test public void relDotSlash1() throws MalformedURLException
    {
       IRIFactory f = IRIFactory.iriImplementation() ;
       IRI iri = f.construct("http://a/b/c/dddd;pppp?qqqqq") ;
       IRI iri2 = iri.resolve("./") ;
       test(iri2, "http://a/b/c/") ;
    }
    
    @Test public void relDotSlash2() throws MalformedURLException
    {
       IRIFactory f = IRIFactory.iriImplementation() ;
       IRI iri = f.construct("http://a/b/c/dddd;pppp?qqqqq") ;
       IRI iri2 = iri.resolve("./foo") ;
       test(iri2, "http://a/b/c/foo") ;
    }

    // RFC 8141 permits "/" "?" and "~".
    // "?" is still excluded in jena-iri because it is used in URN resolution algorithms for r-component and q-component.
    
    // JENA-1647
    @Test public void urn_rfc8141_slash() {
        IRIFactory f = IRIFactory.iriImplementation() ;
        IRI iri = f.construct("urn:nid:abc/def");
    }
    
    // JENA-1647
    @Test public void urn_rfc8141_frag() {
        IRIFactory f = IRIFactory.iriImplementation() ;
        IRI iri = f.construct("urn:nid:abc#frag");
    }
    
    // JENA-1647
    @Test public void urn_rfc8141_tilda() {
        IRIFactory f = IRIFactory.iriImplementation() ;
        IRI iri = f.construct("urn:nid:abc~xyz");
    }
    
    // JENA-1647 - example from RFC 8141
    @Test public void urn_rfc8141_query_1() {
        IRIFactory f = IRIFactory.iriImplementation() ;
        IRI iri = f.construct("urn:example:foo-bar-baz-qux?+CCResolve:cc=uk");
    }
    
    // JENA-1647 - example from RFC 8141
    @Test public void urn_rfc8141_query_2() {
        IRIFactory f = IRIFactory.iriImplementation() ;
        IRI iri = f.construct("urn:example:weather?=op=map&lat=39.56&lon=-104.85&datetime=1969-07-21T02:56:15Z");
    }

    private static void test(IRI iri, String iriStr) throws MalformedURLException
    {
        Assert.assertEquals(iriStr, iri.toASCIIString()) ;
    }
}
