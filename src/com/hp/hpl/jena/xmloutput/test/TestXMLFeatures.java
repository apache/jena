/*
 *  (c)      Copyright Hewlett-Packard Company 2001, 2002   
 * All rights reserved.
  [See end of file]
  $Id: TestXMLFeatures.java,v 1.1.1.1 2002-12-19 19:22:09 bwm Exp $
*/

package com.hp.hpl.jena.xmloutput.test;

import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.oro.text.awk.AwkCompiler;
import org.apache.oro.text.awk.AwkMatcher;
import org.apache.oro.text.regex.MalformedPatternException;
import java.util.Properties;

import java.io.*;

/** 
 * @author bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.1.1.1 $ $Date: 2002-12-19 19:22:09 $
 */
public class TestXMLFeatures extends TestCase {
	static AwkCompiler awk = PrettyWriterTest.awk;
	static AwkMatcher matcher = PrettyWriterTest.matcher;
	static private interface Change {
		void code(RDFWriter w);
	}
	private String base1 = "http://example/foobar";
	private String base2 = "http://example/barfoo";
	private String file1 = "file:testing/abbreviated/namespaces.rdf";
	private String lang;
	TestXMLFeatures(String name, String lang) {
		super(name);
		this.lang = lang;
	}
	public String toString() {
		return getName() + " " + lang;
	}

	public static Test suite() {
		return new TestSuite(TestXMLFeatures.class);
	}

	public void testXMLBase() throws IOException, MalformedPatternException {
		check(file1, //any will do
		"xml:base=['\"]" + base2 + "['\"]", new Change() {
			public void code(RDFWriter writer) {
				String oldvalue = (String) writer.setProperty("xmlbase", base1);
				assertTrue("xmlbase valued non-null", oldvalue == null);

				oldvalue = (String) writer.setProperty("xmlbase", base2);
				assertEquals("xmlbase valued incorrect.", base1, oldvalue);
			}

		});
	}

	public void testPropertyURI() throws IOException {
		doBadPropTest(lang);
	}
	/**
	 * @param code Stuff to do to the writer.
	 * @param filename Read this file, write it out, read it in.
	 * @param regex    Written file must match this.
	 */
	private void check(String filename, String regex, Change code)
		throws IOException, MalformedPatternException {
		check(filename, regex, null, code);
	}
	private void check(
		String filename,
		String regexPresent,
		String regexAbsent,
		Change code)
		throws IOException, MalformedPatternException {
		check(filename, null, regexPresent, regexAbsent, code);
	}
	private void check(
		String filename,
		String encoding,
		String regexPresent,
		String regexAbsent,
		Change code)
		throws IOException, MalformedPatternException {
		Model m = new ModelMem();
		m.read(filename);
		Writer sw;
		ByteArrayOutputStream bos = null;
		if (encoding == null)
			sw = new StringWriter();
		else {
			bos = new ByteArrayOutputStream();
			sw = new OutputStreamWriter(bos, encoding);
		}
		Properties p = (Properties) System.getProperties().clone();
		RDFWriter writer = m.getWriter(lang);
		code.code(writer);
		writer.write(m, sw, filename);
		sw.close();

		String contents;
		if (encoding == null)
			contents = sw.toString();
		else {
			contents = bos.toString(encoding);
		}
		try {
			Model m2 = new ModelMem();
			m2.read(new StringReader(contents), filename);
			assertTrue(m.isIsomorphicWith(m2));
			if (regexPresent != null)
				assertTrue(
					"Looking for /" + regexPresent + "/",
					matcher.contains(contents, awk.compile(regexPresent)));
			if (regexAbsent != null)
				assertTrue(
					"Looking for /" + regexAbsent + "/",
					!matcher.contains(contents, awk.compile(regexAbsent)));
			contents = null;
		} finally {
			System.setProperties(p);
			if (contents != null) {
				System.err.println("===================");
				System.err.println("Offending content");
				System.err.println("===================");
				System.err.println(contents);
				System.err.println("===================");
			}
		}
	}

	void doBadPropTest(String lang) throws IOException {
		Model m = new ModelMem();
		m.add(
			m.createResource(),
			m.createProperty("http://example/", "foo#"),
			"foo");
		File file = File.createTempFile("rdf", ".xml");
		//file.deleteOnExit();

		FileOutputStream fwriter = new FileOutputStream(file);
		try {
			m.write(fwriter, lang);
			fwriter.close();
			// throw new RDFException(RDFException.INVALIDPROPERTYURI);
			fail("Writer did not detect bad property URI");
		} catch (RDFException rdfe) {
			// This loop here really shouldn't be necessary.
			// When oh when, will we
			//  - drop NestedExceptions altogther.
			while (rdfe.getErrorCode() == RDFException.NESTEDEXCEPTION
				&& rdfe.getNestedException() instanceof RDFException)
				rdfe = (RDFException) rdfe.getNestedException();
			assertEquals(
				"Inappropriate exception: " + rdfe.getMessage(),
				rdfe.getErrorCode(),
				RDFException.INVALIDPROPERTYURI);
		}
		file.delete();
	}

	public void testUseNamespace()
		throws IOException, MalformedPatternException {
		check(file1, "xmlns:eg=['\"]http://example.org/#['\"]", new Change() {
			public void code(RDFWriter writer) {
				writer.setNsPrefix("eg", "http://example.org/#");
			}
		});
	}

	public void testUseDefaultNamespace()
		throws IOException, MalformedPatternException {
		check(file1, "xmlns=['\"]http://example.org/#['\"]", new Change() {
			public void code(RDFWriter writer) {
				writer.setNsPrefix("", "http://example.org/#");
			}
		});
	}

	public void testRDFNamespace()
		throws IOException, MalformedPatternException {
		check(
			file1,
			"xmlns:r=['\"]" + RDF.getURI() + "['\"]",
			"rdf:",
			new Change() {
			public void code(RDFWriter writer) {
				writer.setNsPrefix("r", RDF.getURI());
			}
		});
	}

	public void testRDFDefaultNamespace()
		throws IOException, MalformedPatternException {
		check(
			file1,
			"xmlns=['\"]"
				+ RDF.getURI()
				+ "['\"].*"
				+ "xmlns:j.cook.up=['\"]"
				+ RDF.getURI()
				+ "['\"]",
			new Change() {
			public void code(RDFWriter writer) {
				writer.setNsPrefix("", RDF.getURI());
			}
		});
	}
	public void testBadPrefixNamespace()
		throws IOException, MalformedPatternException {
		// Trying to set the prefix should generate a warning.
		// TODO check that a warning is generated.
		check(file1, null, "xmlns:3", new Change() {
			public void code(RDFWriter writer) {
				writer.setNsPrefix("3", "http://example.org/#");
			}
		});
	}

	public void testDuplicateNamespace()
		throws IOException, MalformedPatternException {
		check(
			file1,
			"xmlns:eg[12]=['\"]http://example.org/#['\"]",
			"xmlns:eg[12]=['\"]http://example.org/#['\"].*xmlns:eg[12]=['\"]http://example.org/#['\"]",
			new Change() {
			public void code(RDFWriter writer) {
				writer.setNsPrefix("eg1", "http://example.org/#");
				writer.setNsPrefix("eg2", "http://example.org/#");
			}
		});
	}

	public void testDuplicatePrefix()
		throws IOException, MalformedPatternException {
		check(
			file1,
			"xmlns:eg=['\"]http://example.org/file[12]#['\"]",
			null,
			new Change() {
			public void code(RDFWriter writer) {
				writer.setNsPrefix("eg", "http://example.org/file1#");
				writer.setNsPrefix("eg", "http://example.org/file2#");
			}
		});
	}
	void setNsPrefixSysProp(String prefix, String uri) {
		System.setProperty(RDFWriter.NSPREFIXPROPBASE + uri, prefix);
	}
	public void testUseNamespaceSysProp()
		throws IOException, MalformedPatternException {
		check(file1, "xmlns:eg=['\"]http://example.org/#['\"]", new Change() {
			public void code(RDFWriter writer) {
				setNsPrefixSysProp("eg", "http://example.org/#");
			}
		});
	}

	public void testDefaultNamespaceSysProp()
		throws IOException, MalformedPatternException {
		check(file1, "xmlns=['\"]http://example.org/#['\"]", new Change() {
			public void code(RDFWriter writer) {
				setNsPrefixSysProp("", "http://example.org/#");
			}
		});
	}

	public void testDuplicateNamespaceSysProp()
		throws IOException, MalformedPatternException {
		check(
			file1,
			"xmlns:eg[12]=['\"]http://example.org/#['\"]",
			"xmlns:eg[12]=['\"]http://example.org/#['\"].*xmlns:eg[12]=['\"]http://example.org/#['\"]",
			new Change() {

			public void code(RDFWriter writer) {
				setNsPrefixSysProp("eg1", "http://example.org/#");
				setNsPrefixSysProp("eg2", "http://example.org/#");
			}
		});
	}

	public void testDuplicatePrefixSysProp()
		throws IOException, MalformedPatternException {
		check(
			file1,
			"xmlns:eg=['\"]http://example.org/file[12]#['\"]",
			null,
			new Change() {
			public void code(RDFWriter writer) {
				setNsPrefixSysProp("eg", "http://example.org/file1#");
				setNsPrefixSysProp("eg", "http://example.org/file2#");
			}
		});
	}

	public void testDuplicatePrefixSysPropAndExplicit()
		throws IOException, MalformedPatternException {
		check(
			file1,
			"xmlns:eg=['\"]http://example.org/file[12]#['\"]",
			null,
			new Change() {
			public void code(RDFWriter writer) {
				writer.setNsPrefix("eg", "http://example.org/file1#");
				setNsPrefixSysProp("eg", "http://example.org/file2#");
			}
		});
	}
	public void testUTF8DeclAbsent()
		throws IOException, MalformedPatternException {
		check(file1, "utf-8", null, "<\\?xml", new Change() {
			public void code(RDFWriter writer) {
			}
		});

	}

	public void testUTF16DeclAbsent()
		throws IOException, MalformedPatternException {
		check(file1, "utf-16", null, "<\\?xml", new Change() {
			public void code(RDFWriter writer) {
			}
		});
	}

	public void testUTF8DeclPresent()
		throws IOException, MalformedPatternException {
		check(file1, "utf-8", "<\\?xml", null, new Change() {
			public void code(RDFWriter writer) {
				writer.setProperty("showXmlDeclaration", Boolean.TRUE);
			}
		});
	}

	public void testUTF16DeclPresent()
		throws IOException, MalformedPatternException {
		check(file1, "utf-16", "<\\?xml", null, new Change() {
			public void code(RDFWriter writer) {
				writer.setProperty("showXmlDeclaration", Boolean.TRUE);
			}
		});
	}

	public void testISO8859_1_DeclAbsent()
		throws IOException, MalformedPatternException {
		check(file1, "iso-8859-1", null, "<\\?xml", new Change() {
			public void code(RDFWriter writer) {
				writer.setProperty("showXmlDeclaration", Boolean.FALSE);
			}
		});
	}

	public void testISO8859_1_DeclPresent()
		throws IOException, MalformedPatternException {
		check(
			file1,
			"iso-8859-1",
			"<\\?xml[^?]*ISO-8859-1",
			null,
			new Change() {
			public void code(RDFWriter writer) {
			}
		});
	}

	public void testStringDeclAbsent()
		throws IOException, MalformedPatternException {
		check(file1, null, "<\\?xml", new Change() {
			public void code(RDFWriter writer) {
			}
		});
	}

	public void testStringDeclPresent()
		throws IOException, MalformedPatternException {

		check(file1, "<\\?xml", "encoding", new Change() {
			public void code(RDFWriter writer) {
				writer.setProperty("showXmlDeclaration", Boolean.TRUE);
			}
		});
	}

}
/*
 *  (c)   Copyright Hewlett-Packard Company 2001,2002
 *    All rights reserved.
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
 *
 * $Id: TestXMLFeatures.java,v 1.1.1.1 2002-12-19 19:22:09 bwm Exp $
 */