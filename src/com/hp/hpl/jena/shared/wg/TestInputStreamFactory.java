
/*
 *  (c) Copyright 2002  Hewlett-Packard Development Company, LP
 * See end of file.
 */
package com.hp.hpl.jena.shared.wg;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.util.zip.*;

import com.hp.hpl.jena.shared.*;

/**
 * This class provides input streams that:
 * 1: can be from a URL or from a zip
 * 2: do not actually open until the first read
 * @author Jeremy Carroll
 *
 * 
 */
public class TestInputStreamFactory {

	final private URI base;
	final private URI mapBase;
	final private ZipFile zip;
	final private String property;
    private String createMe = "error";

	/** @param baseDir A prefix of all URLs accessed through this factory.
	 *  @param getBaseDir Replace the baseDir into getBaseDir before opening any URL.
	 */
	public TestInputStreamFactory(URI baseDir, URI getBaseDir) {
		base = baseDir;
		mapBase = getBaseDir;
		zip = null;
		property = null;
	}
	/** @param baseDir A prefix of all URLs accessed through this factory.
	 *  @param zip To open a URL remove the baseDir from the URL and get the named file from the zip.
	 */
	public TestInputStreamFactory(URI baseDir, ZipFile zip) {
		base = baseDir;
		mapBase = null;
		this.zip = zip;
		property = null;
	}

	/** @param baseDir A prefix of all URLs accessed through this factory.
	 *  @param zip To open a URL remove the baseDir from the URL and get the named file from the zip.
	 */
	public TestInputStreamFactory(URI baseDir, String propDir) {
        createMe = "new ARPTestInputStreamFactory(URI.create(\""
        +baseDir.toString()
        +"\"),\""+propDir+"\")";
		base = baseDir;
		mapBase = null;
		this.zip = null;
		property = propDir.endsWith("/") ? propDir : propDir + "/";
	}

	public URI getBase() {
		return base;
	}
	/**
	 * A lazy open. The I/O only starts, and resources
	 * are only allocated on first read.
	 * @param str The URI to open
	 */
	public InputStream open(String str) {
		return open(URI.create(str));
	}
	/**
	 * opens the file, and really does it - not a delayed
	 * lazy opening.
	 * @param str the URI to open
	 * @return null on some failures
	 * @throws IOException
	 */
	public InputStream fullyOpen(String str) throws IOException {
		InputStream in = open(URI.create(str));
		if (in instanceof LazyInputStream
						&& !((LazyInputStream) in).connect())
						return null;
		return in;
	}
	/**
	 * A lazy open. The I/O only starts, and resources
	 * are only allocated on first read.
	 * @param uri to be opened.
	 * @return
	 */
	public InputStream open(URI uri) {
		return (InputStream) open(uri, true);

	}
	public boolean savable() {
		return mapBase != null && mapBase.getScheme().equalsIgnoreCase("file");

	}
	public OutputStream openOutput(String str) {
		OutputStream foo = (OutputStream) open(URI.create(str), false);
	//	System.out.println(foo.toString());
		return foo;
	}

    public String getCreationJava() {
    	return createMe;
    }
	private Object open(URI uri, boolean in) {
		URI relative = uri.isAbsolute() ? base.relativize(uri) : uri;
		if (relative.isAbsolute())
			throw new IllegalArgumentException(
				"This  ARPTestInputStreamFactory only knows about '" + base + "'.");
		if (mapBase != null) {
			//System.out.println("LazyURL: " + relative + " " + mapBase);
			try {
				URL url = mapBase.resolve(relative).toURL();
				if (!in) {
					if (url.getProtocol().equalsIgnoreCase("file"))
						return new FileOutputStream(url.getFile());
					throw new IllegalArgumentException("Can only save to file: scheme");
				}
				return new LazyURLInputStream(url);
			} catch (MalformedURLException e) {
				throw new JenaException( e );
			} catch (IOException e) {
				e.printStackTrace();
				throw new JenaException( e );
			}
		}
		if (!in)
			throw new IllegalArgumentException("Can only save to URLs");

        String relPath = relative.toString();
        if ( relPath.length() - relPath.lastIndexOf('.') > 5 )
           relPath = relPath + ".rdf";
		if (zip != null)
			return new LazyZipEntryInputStream(zip,relPath );
		else
			return TestInputStreamFactory.getInputStream(property + relPath );

	}

	private static InputStream getInputStream(String prop) {
	    // System.err.println(prop);
	    ClassLoader loader = TestInputStreamFactory.class.getClassLoader();
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

/*
 *  (c) Copyright 2002  Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */