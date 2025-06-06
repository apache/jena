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

package org.apache.jena.rdfxml.xmloutput;

import java.io.* ;
import java.nio.charset.StandardCharsets;
import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;
import java.util.regex.Pattern ;

import org.apache.jena.graph.* ;
import org.apache.jena.irix.IRIException;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.RDFReaderI ;
import org.apache.jena.rdf.model.RDFWriterI ;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.rdfxml.xmlinput1.RDFXMLReader;
import org.apache.jena.rdfxml.xmloutput.impl.BaseXMLWriter ;
import org.apache.jena.rdfxml.xmloutput.impl.SimpleLogger ;
import org.apache.jena.shared.InvalidPropertyURIException ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.vocabulary.RDF ;

public class TestXMLFeatures extends XMLOutputTestBase {
	// static protected Logger logger = LoggerFactory.getLogger( TestXMLFeatures.class );
	// static { logger.setLevel( Level.OFF ); }

	private String base1 = "http://example/foobar";

	private String base2 = "http://example/barfoo";

	protected static String file1 = "testing/abbreviated/namespaces.rdf";

	TestXMLFeatures(String name, String lang) {
		super(name, lang);
	}

	@Override
    public String toString() {
		return getName() + " " + lang;
	}

	/**
	 * Writing a model with the base URI set to null should not throw a
	 * null pointer exception.
	 */
	public void testNullBaseWithAbbrev() {
		ModelFactory.createDefaultModel().write(new StringWriter(), lang, null);
	}

	/**
	 * This test checks that using a FileWriter works. It used not to work for
	 * some encodings. The encoding used is the platform default encoding.
	 * Because this may be MacRoman, we have to suppress warning messages.
	 *
	 * @throws IOException
	 */
	public void testBug696057() throws IOException {
		File f = File.createTempFile("jena", ".rdf");
		String fileName = f.getAbsolutePath();
		Model m = createMemModel();
		m.read(new FileInputStream(
				"testing/wg/rdfms-syntax-incomplete/test001.rdf"), "");
		RDFDefaultErrorHandler.silent = true;
		Model m1 = null;
		SimpleLogger old = null;
		try {
			old = BaseXMLWriter.setLogger(new SimpleLogger(){
				@Override
                public void warn(String s) {}
				@Override
                public void warn(String s, Exception e) {}
			});
			m.write(new FileWriter(fileName), lang);
			m1 = createMemModel();
			m1.read(new FileInputStream(fileName), "");
		} finally {
			RDFDefaultErrorHandler.silent = false;
			BaseXMLWriter.setLogger(old);
		}
		assertTrue("Use of FileWriter", m.isIsomorphicWith(m1));
		f.delete();
	}

	public void testXMLBase() throws IOException {
		check(file1, // any will do
				"xml:base=['\"]" + base2 + "['\"]", new Change() {
					@Override
                    public void modify(RDFWriterI writer) {
						String oldvalue = (String) writer.setProperty(
								"xmlbase", base1);
						assertTrue("xmlbase valued non-null", oldvalue == null);

						oldvalue = (String) writer
								.setProperty("xmlbase", base2);
						assertEquals("xmlbase valued incorrect.", base1,
								oldvalue);
					}

				});
	}

	public void testPropertyURI() throws IOException {
		doBadPropTest(lang);
	}

	void doBadPropTest(String lg) throws IOException {
		Model m = createMemModel();
		m.add(m.createResource(), m.createProperty("http://example/", "foo#"),
				"foo");
		File file = File.createTempFile("rdf", ".xml");
		// file.deleteOnExit();

		FileOutputStream fwriter = new FileOutputStream(file);
		try {
			m.write(fwriter, lg);
			fwriter.close();
			fail("Writer did not detect bad property URI");
		} catch (InvalidPropertyURIException je) {
			// as required, so nowt to do.
		}
		file.delete();
	}

	public void testUseNamespace() throws IOException {
		check(file1, "xmlns:eg=['\"]http://example.org/#['\"]", Change
				.setPrefix("eg", "http://example.org/#"));
	}

	public void testSingleQuote() throws IOException {
		check(file1, "'", "\"", Change.setProperty("attributeQuoteChar", "'"));
	}

	public void testDoubleQuote() throws IOException {
		check(file1, "\"", "'", Change.setProperty("attributeQuoteChar", "\""));
	}

	public void testUseDefaultNamespace() throws IOException {
		check(file1, "xmlns=['\"]http://example.org/#['\"]", Change.setPrefix(
				"", "http://example.org/#"));
	}

	public void testUseUnusedNamespace() throws IOException {
		check(file1, "xmlns:unused=['\"]http://unused.org/#['\"]", Change
				.setPrefix("unused", "http://unused.org/#"));
	}

	public void testRDFNamespace() throws IOException {
		check(file1, "xmlns:r=['\"]" + RDF.getURI() + "['\"]", "rdf:",
				new Change() {
					@Override
                    public void modify(Model m) {
						m.removeNsPrefix("rdf");
						m.setNsPrefix("r", RDF.getURI());
					}
				});
	}

	public void testTab() throws IOException {
		check(file1, "          ", Change.setProperty("tab", "5"));
	}

	public void testNoTab() throws IOException {
		check(file1, "  ", Change.setProperty("tab", "0"));
	}

	public void testNoLiteral() throws IOException {
		check("testing/wg/rdfms-xml-literal-namespaces/test001.rdf",
				"#XMLLiteral", "[\"']Literal[\"']", Change.setProperty(
						"blockrules", "parseTypeLiteralPropertyElt"));
	}

	public void testRDFDefaultNamespace() throws IOException {
		check(file1, "xmlns=['\"]" + RDF.getURI() + "['\"].*"
				+ "xmlns:j.cook.up=['\"]" + RDF.getURI() + "['\"]", Change
				.setPrefix("", RDF.getURI()));
	}

	public void testBadPrefixNamespace() {
		// Trying to set the prefix should generate a warning.
		// check(file1, null, null, "xmlns:3", true, new Change() {
		// public void code( RDFWriter w ) {
		// w.setNsPrefix("3", "http://example.org/#");
		// }
		// });
	}

        // JENA-24
        public void testDisallowedXMLNamespace() throws IOException {
		// xml, if present, must be bound to correct namespaces

                // fine, though ill-advised
		check(file1, null, Change.setPrefix("xml", "http://www.w3.org/XML/1998/namespace"));

                // bad, but not fatal now -- we probably ought to raise an warning
                check(file1, null, Change.setPrefix("notxml", "http://www.w3.org/XML/1998/namespace"));

                // bad, will warn
                check(file1, null, null, null, true, Change.setPrefix("xml", "http://example.org/#"));
	}

        // JENA-24
        public void testDisallowedXMLNSNamespace() throws IOException {
		// xmlns, if present, must be bound to correct namespace

                // fine, though ill-advised
		check(file1, null, Change.setPrefix("xmlns", "http://www.w3.org/2000/xmlns/"));

                // bad, but not fatal now -- we probably ought to raise an warning
                check(file1, null, Change.setPrefix("notxmlns", "http://www.w3.org/2000/xmlns/"));

                // bad, will warn
                check(file1, null, null, null, true, Change.setPrefix("xmlns", "http://example.org/#"));
	}

	public void testDuplicateNamespace() throws IOException {
		check(
				file1,
				"xmlns:eg[12]=['\"]http://example.org/#['\"]",
				"xmlns:eg[12]=['\"]http://example.org/#['\"].*xmlns:eg[12]=['\"]http://example.org/#['\"]",
				new Change() {
					@Override
                    public void modify(Model m) {
						m.setNsPrefix("eg1", "http://example.org/#");
						m.setNsPrefix("eg2", "http://example.org/#");
					}
				});
	}

	public void testEntityDeclaration() throws IOException {
		check(file1, "<!DOCTYPE rdf:RDF \\[[^]]*<!ENTITY spoo *'goo:boo'>",
				"SPONGLE", Change.setProperty("showDoctypeDeclaration", true)
						.andSetPrefix("spoo", "goo:boo"));
	}

	public void testEntityUse() throws IOException {
		check(file1, "rdf:resource=\"&ex0;spoo\"", "SPONGLE", Change
				.setProperty("showDoctypeDeclaration", true));
	}

	public void testDuplicatePrefix() throws IOException {
		check(file1, "xmlns:eg=['\"]http://example.org/file[12]#['\"]", null,
				new Change() {
					@Override
                    public void modify(Model m) {
						m.setNsPrefix("eg", "http://example.org/file1#");
						m.setNsPrefix("eg", "http://example.org/file2#");
					}
				});
	}

	void setNsPrefixSysProp(String prefix, String uri) {
		System.setProperty(RDFWriterI.NSPREFIXPROPBASE + uri, prefix);
	}

	public void testUseNamespaceSysProp() throws IOException {
		check(file1, "xmlns:eg=['\"]http://example.org/#['\"]", new Change() {
			@Override
            public void modify(RDFWriterI writer) {
				setNsPrefixSysProp("eg", "http://example.org/#");
			}
		});
	}

	public void testDefaultNamespaceSysProp() throws IOException {
		check(file1, "xmlns=['\"]http://example.org/#['\"]", new Change() {
			@Override
            public void modify(RDFWriterI writer) {
				setNsPrefixSysProp("", "http://example.org/#");
			}
		});
	}

	public void testDuplicateNamespaceSysProp() throws IOException {
		check(
				file1,
				"xmlns:eg[12]=['\"]http://example.org/#['\"]",
				"xmlns:eg[12]=['\"]http://example.org/#['\"].*xmlns:eg[12]=['\"]http://example.org/#['\"]",
				new Change() {

					@Override
                    public void modify(RDFWriterI writer) {
						setNsPrefixSysProp("eg1", "http://example.org/#");
						setNsPrefixSysProp("eg2", "http://example.org/#");
					}
				});
	}

	public void testDuplicatePrefixSysProp() throws IOException {
		check(file1, "xmlns:eg=['\"]http://example.org/file[12]#['\"]", null,
				new Change() {
					@Override
                    public void modify(RDFWriterI writer) {
						setNsPrefixSysProp("eg", "http://example.org/file1#");
						setNsPrefixSysProp("eg", "http://example.org/file2#");
					}
				});
	}

	public void testDuplicatePrefixSysPropAndExplicit() throws IOException {
		check(file1, "xmlns:eg=['\"]http://example.org/file[12]#['\"]", null,
				new Change() {
					@Override
                    public void modify(Model m) {
						m.setNsPrefix("eg", "http://example.org/file1#");
						setNsPrefixSysProp("eg", "http://example.org/file2#");
					}
				});
	}

	public void testUTF8DeclAbsent() throws IOException {
		check(file1, "utf-8", null, "<\\?xml", Change.none());
	}

	public void testUTF16DeclAbsent() throws IOException {
		check(file1, "utf-16", null, "<\\?xml", false, Change.none());
	}

	public void testUTF8DeclPresent() throws IOException {
		check(file1, "utf-8", "<\\?xml", null, Change.setProperty(
				"showXmlDeclaration", true));
	}

	public void testUTF16DeclPresent() throws IOException {
		check(file1, "utf-16", "<\\?xml", null, Change.setProperty(
				"showXmlDeclaration", true));
	}

	public void testISO8859_1_DeclAbsent() throws IOException {
		check(file1, "iso-8859-1", null, "<\\?xml", Change.setProperty(
				"showXmlDeclaration", false));
	}

	public void testISO8859_1_DeclPresent() throws IOException {
		check(file1, "iso-8859-1", "<\\?xml[^?]*ISO-8859-1", null, Change
				.none());
	}

	public void testStringDeclAbsent() throws IOException {
		check(file1, null, "<\\?xml", Change.none());
	}

	public void testStringDeclPresent() throws IOException {
		check(file1, "<\\?xml", "encoding", Change.setProperty(
				"showXmlDeclaration", true));
	}

	static final int BadPropURI = 1;

	static final int NoError = 0;

	static final int ExtraTriples = 2;

	static final int BadURI = 3;

    public void checkPropURI(String s, String p, Object val, int behaviour) throws IOException {
        // create triple and graph.
        // BaseXMLWriter.dbg = true;
        // SystemOutAndErr.block();
        // TestLogger tl = new TestLogger(BaseXMLWriter.class);
        blockLogger() ;
        Node blank = NodeFactory.createBlankNode() ;
        Node prop = NodeFactory.createURI(s) ;
        Graph g = GraphMemFactory.createDefaultGraphSameValue() ;
        g.add(Triple.create(blank, prop, blank)) ;
        // create Model
        Model m = ModelFactory.createModelForGraph(g) ;
        // serialize

        @SuppressWarnings("deprecation")
        RDFWriterI rw = m.getWriter(lang) ;
        if ( p != null )
            rw.setProperty(p, val) ;
        try (StringWriter w = new StringWriter()) {
            rw.write(m, w, "http://example.org/") ;
            String f = w.toString() ;

            switch (behaviour) {
                case BadPropURI :
                    fail("Bad property URI <" + s + "> was not detected.") ;
                    return ;
                case BadURI :
                    fail("Bad URI <" + s + "> was not detected.") ;
                    return ;
            }
            // read back in
            Model m2 = createMemModel() ;
            RDFReaderI rdr = new RDFXMLReader();
            rdr.setProperty("error-mode", "lax") ;
            try (StringReader sr = new StringReader(f)) {
                rdr.read(m2, sr, "http://example.org/") ;
            }

            // check
            switch (behaviour) {
                case ExtraTriples :
                    assertTrue("Expecting Brickley behaviour.", m2.size() == 3) ;
                    break ;
                case NoError :
                    assertTrue("Comparing Model written out and read in.", m.isIsomorphicWith(m2)) ;
                    break ;
            }
        } catch (IRIException ex) {
            if ( behaviour == BadURI )
                return ;
            throw ex ;
        } catch (InvalidPropertyURIException je) {
            if ( behaviour == BadPropURI )
                return ;
            throw je ;
        } catch (JenaException e) {
            throw e ;
        } finally {
            // BaseXMLWriter.dbg = false;
            // tl.end();
            unblockLogger() ;
            // SystemOutAndErr.unblock();
        }
    }

	public void testBadURIAsProperty1() throws IOException {
		try {
			// RDFDefaultErrorHandler.logger.setLevel( Level.OFF );
			checkPropURI("_:aa", null, null, BadURI);
		} finally { // RDFDefaultErrorHandler.logger.setLevel( Level.WARN );
		}
	}

	public void testBadURIAsProperty2() throws IOException {
		try {
			// RDFDefaultErrorHandler.logger.setLevel( Level.OFF );
			checkPropURI("_:aa", "allowBadURIs", "true", NoError);
		} finally {// RDFDefaultErrorHandler.logger.setLevel( Level.WARN );
		}
	}

	public void testLiAsProperty1() throws IOException {
		checkPropURI(RDF.getURI() + "li", null, null, BadPropURI);
	}

	/*
	 * public void testLiAsProperty2() throws IOException {
	 * checkPropURI(RDF.getURI()+"li", "brickley", "true", ExtraTriples); }
	 */
	public void testDescriptionAsProperty() throws IOException {
		checkPropURI(RDF.getURI() + "Description", null, null, BadPropURI);
	}

	public void testBadProperty1() throws IOException {
		checkPropURI("http://x/a.b/", null, null, BadPropURI);
	}

	/*
	 * public void testBadProperty2() throws IOException {
	 * checkPropURI("http:/a.b/", "brickley", "http://example.org/b#",
	 * ExtraTriples); }
	 *
	 */
	public void testRelativeAPI() {
        @SuppressWarnings("deprecation")
		RDFWriterI w = createMemModel().getWriter(lang);
		String old = (String) w.setProperty("relativeURIs", "");
		assertEquals("default value check", old,
				"same-document, absolute, relative, parent");
		w.setProperty("relativeURIs", "network, grandparent,relative,  ");
		w.setProperty("relativeURIs",
				"  parent, same-document, network, parent, absolute ");
		// TestLogger tl = new TestLogger(URI.class);
		blockLogger();
		w.setProperty("relativeURIs", "foo"); // will get warning
		assertTrue("A warning should have been generated.", unblockLogger());
	}

	private void relative(String relativeParam, String base,
			Collection<String> regexesPresent, Collection<String> regexesAbsent)
			throws IOException {

		Model m = createMemModel();
		m.read("file:testing/abbreviated/relative-uris.rdf");

		String contents ;
		try ( ByteArrayOutputStream bos = new ByteArrayOutputStream() ) {
	        @SuppressWarnings("deprecation")
		    RDFWriterI writer = m.getWriter(lang);
		    writer.setProperty("relativeURIs", relativeParam);
		    writer.write(m, bos, base);
	        contents = bos.toString(StandardCharsets.UTF_8);
		}

		try {
			Model m2 = createMemModel();
			m2.read(new StringReader(contents), base);
			assertTrue(m.isIsomorphicWith(m2));
			Iterator<String> it = regexesPresent.iterator();
			while (it.hasNext()) {
				String regexPresent = it.next();
				assertTrue("Looking for /" + regexPresent + "/", Pattern
						.compile(Util.substituteStandardEntities(regexPresent),
								Pattern.DOTALL).matcher(contents).find()
				//
				// matcher.contains(
				// contents,
				// awk.compile(
				// Util.substituteStandardEntities(regexPresent)))
				);
			}
			it = regexesAbsent.iterator();
			while (it.hasNext()) {
				String regexAbsent = it.next();
				assertTrue(
						"Looking for (not) /" + regexAbsent + "/",
						!Pattern.compile("[\"']"+ Util.substituteStandardEntities(regexAbsent)+ "[\"']", Pattern.DOTALL)
								.matcher(contents).find()

				// matcher.contains(
				// contents,
				// awk.compile(
				// "[\"']"
				// + Util.substituteStandardEntities(regexAbsent)
				// + "[\"']"))
				);
			}
			contents = null;
		} finally {
			if (contents != null) {
				System.err.println("===================");
				System.err.println("Offending content - " + toString());
				System.err.println("===================");
				System.err.println(contents);
				System.err.println("===================");
			}
		}
	}

	static String rData1[][] = {
			// http://www.example.org/a/b/c/d/
			{ "", "http://www.example.org/a/b/c/d/",
					"http://www.example.org/a/b/c/d/e/f/g/",
					"http://www.example.org/a/b/C/D",
					"http://www.example.org/A/B#foo/",
					"http://www.example.org/a/b/c/d/X#bar",
					"http://example.com/A",
					"http://www.example.org/a/b/c/d/z[?]x=a", },
			{ "same-document", "", null, null, null, null, null, null, },
			{ "absolute", "/a/b/c/d/", "/a/b/c/d/e/f/g/", "/a/b/C/D",
					"/A/B#foo/", "/a/b/c/d/X#bar", null, "/a/b/c/d/z[?]x=a", },
			{ "relative", "[.]", "e/f/g/", null, null, "X#bar", null,
					"z[?]x=a", },
			{ "parent", "[.][.]/d/", "[.][.]/d/e/f/g/", null, null,
					"[.][.]/d/X#bar", null, "[.][.]/d/z[?]x=a", },
			{ "network", "//www.example.org/a/b/c/d/",
					"//www.example.org/a/b/c/d/e/f/g/",
					"//www.example.org/a/b/C/D", "//www.example.org/A/B#foo/",
					"//www.example.org/a/b/c/d/X#bar", "//example.com/A",
					"//www.example.org/a/b/c/d/z[?]x=a", },
			{ "grandparent", "[.][.]/[.][.]/c/d/", "[.][.]/[.][.]/c/d/e/f/g/",
					"[.][.]/[.][.]/C/D", null, "[.][.]/[.][.]/c/d/X#bar", null,
					"[.][.]/[.][.]/c/d/z[?]x=a", }, };

	static String rData2[][] = {
			// http://www.example.org/a/b/c/d
			{ "", "http://www.example.org/a/b/c/d/",
					"http://www.example.org/a/b/c/d/e/f/g/",
					"http://www.example.org/a/b/C/D",
					"http://www.example.org/A/B#foo/",
					"http://www.example.org/a/b/c/d/X#bar",
					"http://example.com/A",
					"http://www.example.org/a/b/c/d/z[?]x=a", },
			{ "same-document", null, null, null, null, null, null, null, },
			{ "absolute", "/a/b/c/d/", "/a/b/c/d/e/f/g/", "/a/b/C/D",
					"/A/B#foo/", "/a/b/c/d/X#bar", null, "/a/b/c/d/z[?]x=a", },
			{ "relative", "d/", "d/e/f/g/", null, null, "d/X#bar", null,
					"d/z[?]x=a", },
			{ "parent", "[.][.]/c/d/", "[.][.]/c/d/e/f/g/", "[.][.]/C/D", null,
					"[.][.]/c/d/X#bar", null, "[.][.]/c/d/z[?]x=a", },
			{ "network", "//www.example.org/a/b/c/d/",
					"//www.example.org/a/b/c/d/e/f/g/",
					"//www.example.org/a/b/C/D", "//www.example.org/A/B#foo/",
					"//www.example.org/a/b/c/d/X#bar", "//example.com/A",
					"//www.example.org/a/b/c/d/z[?]x=a", },
			{ "grandparent", "[.][.]/[.][.]/b/c/d/",
					"[.][.]/[.][.]/b/c/d/e/f/g/", "[.][.]/[.][.]/b/C/D", null,
					"[.][.]/[.][.]/b/c/d/X#bar", null,
					"[.][.]/[.][.]/b/c/d/z[?]x=a", }, };

	static String rData3[][] = {
			// http://www.example.org/A/B#
			{ "", "http://www.example.org/a/b/c/d/",
					"http://www.example.org/a/b/c/d/e/f/g/",
					"http://www.example.org/a/b/C/D",
					"http://www.example.org/A/B#foo/",
					"http://www.example.org/a/b/c/d/X#bar",
					"http://example.com/A",
					"http://www.example.org/a/b/c/d/z[?]x=a", },
			{ "same-document", null, null, null, "#foo/", null, null, null, },
			{ "absolute", "/a/b/c/d/", "/a/b/c/d/e/f/g/", "/a/b/C/D",
					"/A/B#foo/", "/a/b/c/d/X#bar", null, "/a/b/c/d/z[?]x=a", },
			{ "relative", null, null, null, "B#foo/", null, null, null, },
			{ "parent", "[.][.]/a/b/c/d/", "[.][.]/a/b/c/d/e/f/g/",
					"[.][.]/a/b/C/D", "[.][.]/A/B#foo/",
					"[.][.]/a/b/c/d/X#bar", null, "[.][.]/a/b/c/d/z[?]x=a", },
			{ "network", "//www.example.org/a/b/c/d/",
					"//www.example.org/a/b/c/d/e/f/g/",
					"//www.example.org/a/b/C/D", "//www.example.org/A/B#foo/",
					"//www.example.org/a/b/c/d/X#bar", "//example.com/A",
					"//www.example.org/a/b/c/d/z[?]x=a", },
			{ "grandparent", null, null, null, null, null, null, null, }, };

	private void relative(int i, String base, String d[][]) throws IOException {
		Set<String> in = new HashSet<>();
		Set<String> out = new HashSet<>();
		for (int j = 1; j < d[i].length; j++) {

			in.add(d[i][j] == null ? d[0][j] : d[i][j]);
			if (i != 0 && d[i][j] != null)
				out.add(d[0][j]);
		}
		// System.out.println(base + "["+i+"]");
		relative(d[i][0], base, in, out);
	}

	public void testRelative() throws Exception {
		for (int i = 0; i < 7; i++) {
			relative(i, "http://www.example.org/a/b/c/d/", rData1);
			relative(i, "http://www.example.org/a/b/c/d", rData2);
			relative(i, "http://www.example.org/A/B#", rData3);
		}
	}

	private static String uris[] = { "http://www.example.org/a/b/c/d/",
			"http://www.example.org/a/b/c/d/e/f/g/",
			"http://www.example.org/a/b/C/D",
			"http://www.example.org/A/B#foo/",
			"http://www.example.org/a/b/c/d/X#bar", "http://example.com/A",
			"http://www.example.org/a/b/c/d/z?x=a", };
}
