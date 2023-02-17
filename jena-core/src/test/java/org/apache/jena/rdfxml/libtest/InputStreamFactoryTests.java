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

package org.apache.jena.rdfxml.libtest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;

import org.apache.jena.irix.IRIx;

/** In support of the RDF 2004 Working Group tests.
 *
 * This class provides input streams that:
 * 1: can be from a URL or from a zip
 * 2: do not actually open until the first read
 */
public class InputStreamFactoryTests {

	final String base;
	final private ZipFile zip;
	final private String property;
    private String createMe = "error";

	public InputStreamFactoryTests(String baseDir, String propDir) {
        createMe = "new TestInputStreamFactory(URI.create(\""+baseDir.toString()+"\"),\""+propDir+"\")";
		base = baseDir;
		this.zip = null;
		property = propDir.endsWith("/") ? propDir : propDir + "/";
	}

	public String getBase() {
		return base;
	}

	/**
	 * opens the file, and really does it - not a delayed
	 * lazy opening.
	 * @param str the URI to open
	 * @return null on some failures
	 * @throws IOException
	 */
	public InputStream fullyOpen(String str) throws IOException {
		InputStream in = open(str);
		if (in instanceof LazyInputStream
						&& !((LazyInputStream) in).connect())
						return null;
		return in;
	}
	/**
	 * A lazy open. The I/O only starts, and resources
	 * are only allocated on first read.
	 * @param uri to be opened.
	 * @return the opened stream
	 */
	public InputStream open(String uri) {
		return (InputStream) open(uri, true);

	}

	public OutputStream openOutput(String str) {
		OutputStream foo = (OutputStream) open(str, false);
	//	System.out.println(foo.toString());
		return foo;
	}

    public String getCreationJava() {
    	return createMe;
    }
	private Object open(String uri, boolean in) {

	    IRIx base2 = IRIx.create(base);
	    IRIx uri2 = IRIx.create(uri);
	    IRIx relative = base2.relativize(uri2);

		//IRI relative = uri.isAbsolute() ? base.relativize(uri, IRIRelativize.CHILD) : uri;

		if (relative.isAbsolute())
			throw new IllegalArgumentException(
				"This  TestInputStreamFactory only knows about '" + base + "'.");

		String relPath = relative.toString();
		if ( relPath.length() - relPath.lastIndexOf('.') > 5 ) {
			relPath = relPath + ".rdf";
			relative = IRIx.create(relPath);
		}

		if (!in)
			throw new IllegalArgumentException("Can only save to URLs");


		if (zip != null)
			return new LazyZipEntryInputStream(zip,relPath );
		else
			return InputStreamFactoryTests.getInputStream(property + relPath );

	}

	private static InputStream getInputStream(String prop) {
	    // System.err.println(prop);
	    ClassLoader loader = InputStreamFactoryTests.class.getClassLoader();
	    if (loader == null)
	        throw new SecurityException("Cannot access class loader");
	    InputStream in =
	        // loader.getResourceAsStream("com/hp/hpl/jena/rdf/arp/test/data/" + prop);
	loader.getResourceAsStream("testing/" + prop);
	    //	System.out.println(prop);
	    if (in == null) {
	        try {
	            in = new FileInputStream("testing/" + prop);
	        } catch (IOException e) {
	        }
	        if (in == null)
	            throw new IllegalArgumentException(
	                "Resource: " + prop + " not found on class path.");
	    }

	    return in;
	}
}
