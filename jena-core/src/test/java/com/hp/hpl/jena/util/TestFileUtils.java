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

package com.hp.hpl.jena.util;

import com.hp.hpl.jena.util.FileUtils;

import junit.framework.*;

/**
 * TestFileUtils
 */
public class TestFileUtils extends TestCase {
	public TestFileUtils(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(TestFileUtils.class);
	}

	public void testLangXML() {
		assertEquals("RDF/XML", FileUtils.langXML);
	}

	public void testLangXMLAbbrev() {
		assertEquals("RDF/XML-ABBREV", FileUtils.langXMLAbbrev);
	}

	public void testLangNTriple() {
		assertEquals("N-TRIPLE", FileUtils.langNTriple);
	}

	public void testLangN3() {
		assertEquals("N3", FileUtils.langN3);
	}

	public void testLangTurtle() {
		assertEquals("TURTLE", FileUtils.langTurtle);
	}

	public void testGuessLangLowerCase() {
		assertEquals(FileUtils.langN3, FileUtils.guessLang("simple.n3"));
		assertEquals(FileUtils.langN3, FileUtils.guessLang("hello.there.n3"));
		assertEquals(FileUtils.langTurtle, FileUtils.guessLang("simple.ttl"));
		assertEquals(FileUtils.langTurtle, FileUtils
				.guessLang("hello.there.ttl"));
		assertEquals(FileUtils.langNTriple, FileUtils.guessLang("simple.nt"));
		assertEquals(FileUtils.langNTriple, FileUtils.guessLang("whats.up.nt"));
		assertEquals(FileUtils.langXML, FileUtils.guessLang("poggle.rdf"));
		assertEquals(FileUtils.langXML, FileUtils.guessLang("wise.owl"));
		assertEquals(FileUtils.langXML, FileUtils.guessLang("dotless"));
	}

	public void testGuessLangMixedCase() {
		assertEquals(FileUtils.langN3, FileUtils.guessLang("simple.N3"));
		assertEquals(FileUtils.langN3, FileUtils.guessLang("hello.there.N3"));
		assertEquals(FileUtils.langTurtle, FileUtils.guessLang("simple.TTL"));
		assertEquals(FileUtils.langTurtle, FileUtils
				.guessLang("hello.there.TTL"));
		assertEquals(FileUtils.langNTriple, FileUtils.guessLang("simple.NT"));
		assertEquals(FileUtils.langNTriple, FileUtils.guessLang("whats.up.Nt"));
		assertEquals(FileUtils.langXML, FileUtils.guessLang("poggle.rDf"));
		assertEquals(FileUtils.langXML, FileUtils.guessLang("wise.OwL"));
		assertEquals(FileUtils.langXML, FileUtils.guessLang("dotless"));
	}

	public void testGuessLangFallback() {
		assertEquals("spoo", FileUtils.guessLang("noSuffix", "spoo"));
		assertEquals("pots", FileUtils.guessLang("suffix.unknown", "pots"));
		assertEquals(FileUtils.langXML, FileUtils.guessLang("rdf.rdf", "spoo"));
		assertEquals(FileUtils.langXML, FileUtils.guessLang("rdf.owl", "spoo"));
	}

	public void testMisplacedDots() {
		assertEquals("spoo", FileUtils.guessLang("stuff.left/right", "spoo"));
		assertEquals("spoo", FileUtils.guessLang("stuff.left\\right", "spoo"));
	}

	public void testFilename1() {
		isFilename("foo");
	}

	public void testFilename2() {
		isFilename("foo/bar");
	}

	public void testFilename3() {
		isFilename("foo\\bar");
	}

	public void testFilename4() {
		isFilename("\\bar");
	}

	public void testFilename5() {
		isFilename("foo/bar");
	}

	public void testFilename6() {
		isFilename("c:foo");
	}

	public void testFilename7() {
		isFilename("c:\\foo");
	}

	public void testFilename8() {
		isFilename("c:\\foo\\bar");
	}

	public void testFilename9() {
		isFilename("file::foo");
	}

	public void testFilename10() {
		isNotFilename("http://www.hp.com/");
	}

	public void testFilename11() {
		isNotFilename("urn:tag:stuff");
	}

	public void testTranslateFilename1() {
		checkToFilename("file:Dir/File", "Dir/File");
	}

	public void testTranslateFilename2() {
		checkToFilename("c:\\Dir\\File", "c:\\Dir\\File");
	}

	public void testTranslateFilename3() {
		checkToFilename("unknown:File", null);
	}

	public void testTranslateFilename4() {
		checkToFilename("file:Dir/File With Space", "Dir/File With Space");
	}

	public void testTranslateFilename5() {
		checkToFilename("file:Dir/File%20With Enc%21", "Dir/File With Enc!");
	}

	public void testTranslateFilename6() {
		checkToFilename("file:///dir/file", "/dir/file");
	}

	public void testTranslateFilename7() {
		checkToFilename("file:///c:/dir/file", "/c:/dir/file");
	}

	public void testTranslateFilename8() {
		checkToFilename("file:file", "file");
	}

	public void testTranslateFilename9() {
		checkToFilename("file://file", "//file");
	}

	// Don't tranlate:
	public void testTranslateFilename10() {
		checkToFilename("Dir/File%20With Enc%21", "Dir/File%20With Enc%21");
	}

	public void testTranslateFilename11() {
		checkToFilename("Dir/File+With+Plus", "Dir/File+With+Plus");
	}

	public void testTranslateFilename12() {
		checkToFilename("file:Dir/File+With+Plus", "Dir/File+With+Plus");
	}

	void isFilename(String fn) {
		assertTrue("Should be a file name : " + fn, FileUtils.isFile(fn));
	}

	void isNotFilename(String fn) {
		assertFalse("Shouldn't be a  file name: " + fn, FileUtils.isFile(fn));
	}

	void checkToFilename(String url, String fn) {
		String t = FileUtils.toFilename(url);
		assertEquals("Wrong: " + t + " != " + fn, t, fn);
	}

	public void testToURL1() {
		checkToURL("A%H","%25");
	}
	public void testToURL2() {
		checkToURL("A#H","%23");
	}
	public void testToURL3() {
		checkToURL("A?H","%3F");
	}
	public void testToURL4() {
		checkToURL("A H","%20");
	}
	public void testToURL5() {
		checkToURL("ü","ü");
	}
	private void checkToURL(String fn, String match) {
		String r = FileUtils.toURL(fn);
		if (!r.matches("^.*/[^/]*" + match + "[^/]*$"))
			fail("Converted \"" + fn + "\" to <" + r
					+ "> which did not match /" + match + "/");
		if (!r.startsWith("file:///"))
			fail("Converted \"" + fn + "\" to <" + r
					+ "> which does not start file:///");
		if (r.startsWith("file:////"))
			fail("Converted \"" + fn + "\" to <" + r
					+ "> which has too many initial /");

	}
}
