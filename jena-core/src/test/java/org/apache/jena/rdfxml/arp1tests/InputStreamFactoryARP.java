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

package org.apache.jena.rdfxml.arp1tests;

import java.io.*;

import org.apache.jena.irix.IRIx;

/** In support of the RDF 2004 Working Group tests.
 *
 * This class provides input streams that:
 * 1: can be from a URL or from a zip
 * 2: do not actually open until the first read
 */
public class InputStreamFactoryARP {

    // Jena6 :: Now greatly simplified.
    // URLs are remapped to load as files from testing/
	final String rootURL;
	final private String rootDir;

	public InputStreamFactoryARP(String rootURL, String rootDir) {
		this.rootURL = rootURL;
		this.rootDir = rootDir.endsWith("/") ? rootDir : rootDir + "/";
	}

	public String getBase() {
		return rootURL;
	}

	public InputStream fullyOpen(String str) throws IOException {
	    return openForInput(str);
	}

	private InputStream openForInput(String uri) {
	    // Maps URLs starting rootURL to local files as given by baseURL

	    IRIx base2 = IRIx.create(rootURL);
	    IRIx uri2 = IRIx.create(uri);
	    IRIx relative = base2.relativize(uri2);

		//IRI relative = uri.isAbsolute() ? base.relativize(uri, IRIRelativize.CHILD) : uri;

		if (relative.isAbsolute())
			throw new IllegalArgumentException(
				"This  TestInputStreamFactory only knows about '" + rootURL + "'.");

		String relPath = relative.toString();
		if ( relPath.length() - relPath.lastIndexOf('.') > 5 ) {
			relPath = relPath + ".rdf";
			relative = IRIx.create(relPath);
		}

		return getInputStream(rootDir + relPath );
	}

	private static InputStream getInputStream(String prop) {
	    try {
            return new FileInputStream("testing/" + prop);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Resource: " + prop + " not found on class path.");
        }
	}
}
