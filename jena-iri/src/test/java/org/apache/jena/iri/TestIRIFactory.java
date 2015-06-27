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
package org.apache.jena.iri;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import junit.framework.JUnit4TestAdapter;
import org.junit.Test;

public class TestIRIFactory {
	static public junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestIRIFactory.class);
	}
	
	private static IRIFactory iriFactory = IRIFactory.jenaImplementation();
	
	@Test
	public void createFromURI() {
		URI uri = URI.create("http://johndoe:secret@example.com:81/page?2#hash");
		IRI iri = iriFactory.create(uri);		
		assertEquals("http", iri.getScheme());
		assertEquals("johndoe:secret", iri.getRawUserinfo());
		assertEquals("example.com", iri.getRawHost());
		assertEquals(81, iri.getPort());
		assertEquals("/page", iri.getRawPath());
		assertEquals("2", iri.getRawQuery());
		assertEquals("hash", iri.getRawFragment());
	}

	@Test
	public void createFromRelativeURI() throws Exception {
		URI relative = URI.create("page/deeper.txt?q");
		IRI relativeIri = iriFactory.create(relative);
		assertEquals("page/deeper.txt", relativeIri.getRawPath());
		
		IRI base = iriFactory.create("http://example.com/relative/path?q=somethingelse");
		IRI absolute = base.create(relative);
		assertEquals("http://example.com/relative/page/deeper.txt?q", absolute.toString());
		
		URI other = URI.create("http://other.example.net/");
		IRI otherIri = base.create(other);
		assertEquals("http://other.example.net/", otherIri.toASCIIString());
	}
	
    @Test(expected=IRIException.class)
    public void constructFromDubiousURI() throws Exception {
        // Legal URI by Java
        URI relative = URI.create("unknown:abc");
        // Create a factory and set an error rule.
        IRIFactory factory = new IRIFactory();
        factory.setIsError(ViolationCodes.UNREGISTERED_IANA_SCHEME,true);
        // Expect exception
        IRI relativeIri = IRIFactory.iriImplementation().construct(relative);
    }
	
}
