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

package com.hp.hpl.jena.n3;


import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIException;
import org.apache.jena.iri.IRIFactory;
import com.hp.hpl.jena.util.FileUtils;

/** A simple class to access IRI resolution.
 * Replaced by {@code org.apache.jena.riot.system.IRIResolver}
 */

@Deprecated
public class IRIResolver {
	/**
	 * The current working directory, as a string.
	 */
	static private String globalBase = "http://localhost/LocalHostBase/" ;
	
	// Try to set the global base from the current directory.  
	// Security (e.g. Tomcat) may prevent this in which case we
	// use a common default set above.
	static {
	    try { globalBase = FileUtils.toURL("."); }
	    catch (Throwable th) {  }
	}
	    
	/**
	 * The current working directory, as an IRI.
	 */
	static final IRI cwd;

	/**
	 * An IRIFactory appropriately configuired.
	 */
	static final IRIFactory factory = new IRIFactory(IRIFactory
			.jenaImplementation());
	static {
		factory.setSameSchemeRelativeReferences("file");
	}

	static {
		
		IRI cwdx;
		try {
			cwdx = factory.construct(globalBase);
		} catch (IRIException e) {
			System.err.println("Unexpected IRIException in initializer: "
					+ e.getMessage());
			cwdx = factory.create("file:///");
		}
		cwd = cwdx;
	}


	
	/**
	 * Turn a filename into a well-formed file: URL relative to the working
	 * directory.
	 * 
	 * @param filename
	 * @return String The filename as an absolute URL
	 */
	static public String resolveFileURL(String filename) throws IRIException {
		IRI r = cwd.resolve(filename);
		if (!r.getScheme().equalsIgnoreCase("file")) {
			return resolveFileURL("./" + filename);
		}
		return r.toString();
	}

	/**
	 * Create resolve a URI against a base. If baseStr is a relative file IRI
	 * then it is first resolved against the current working directory.
	 * 
	 * @param relStr
	 * @param baseStr
	 *            Can be null if relStr is absolute
	 * @return String An absolute URI
	 * @throws JenaURIException
	 *             If result would not be legal, absolute IRI
	 */
	static public String resolve(String relStr, String baseStr)
			throws JenaURIException {
		return exceptions(resolveIRI(relStr, baseStr)).toString();
	}

	/*
	 * No exception thrown by this method.
	 */
	static private IRI resolveIRI(String relStr, String baseStr) {
		IRI i = factory.create(relStr);
		if (i.isAbsolute())
			// removes excess . segments
			return cwd.create(i);

		IRI base = factory.create(baseStr);

		if ("file".equalsIgnoreCase(base.getScheme()))
			return cwd.create(base).create(i);
		return base.create(i);
	}

	final private IRI base;

	/**
	 * Construct an IRIResolver with base as the 
	 * current working directory.
	 *
	 */
	public IRIResolver() {
		this(null);
	}

	/**
	 * Construct an IRIResolver with base determined
	 * by the argument URI. If this is relative,
	 * it is relative against the current working directory.
	 * @param baseS
	 * 
	 * @throws JenaURIException
	 *             If resulting base would not be legal, absolute IRI
	 */
	public IRIResolver(String baseS) {
		if (baseS == null)
			baseS = chooseBaseURI();
		// IRI aaa = RelURI.factory.construct(baseS);
		base = exceptions(cwd.create(baseS));
	}

	/**
	 * The base of this IRIResolver.
	 * @return String
	 */
	public String getBaseIRI() {
		return base.toString();
	}

	/**
	 * Resolve the relative URI against the base of
	 * this IRIResolver.
	 * @param relURI
	 * @return the resolved IRI
	 * @throws JenaURIException
	 *             If resulting URI would not be legal, absolute IRI
	
	 */
	public String resolve(String relURI) {
		return exceptions(base.resolve(relURI)).toString();
	}

	
	/**
	 * Throw any exceptions resulting from IRI.
	 * @param iri
	 * @return iri
	 */
	static private IRI exceptions(IRI iri) {
		if (showExceptions && iri.hasViolation(false)) {
			try {
				cwd.construct(iri);
			} catch (IRIException e) {
				throw new JenaURIException(e);
			}
		}
		return iri;
	}
	
	private static boolean showExceptions = true;

	/**
	    To allow Eyeball to bypass IRI checking (because it's doing its own)
	*/
	public static void suppressExceptions()
	{ setShowExceptions(false) ; }

	/** To allow Eyeball to bypass IRI checking (because it's doing its own) */
	public static void setShowExceptions(boolean state)
	{ showExceptions = state ; }

/**
	 * Resolve the relative URI str against the current
	 * working directory.
	 * @param str
	 * @return String
	 */
	public static String resolveGlobal(String str) {
		return exceptions(cwd.resolve(str)).toString();
	}

	/**
	 * Choose a base URI based on the current directory
	 * 
	 * @return String Absolute URI
	 */

	static public String chooseBaseURI() {
		return chooseBaseURI(null);
	}

	/**
	 * Choose a baseURI based on a suggestion
	 * 
	 * @return String URI (if relative, relative to current working directory).
	 */

	static public String chooseBaseURI(String baseURI) {
		if (baseURI == null)
			baseURI = "file:.";
		return resolveGlobal(baseURI);
	}

}
