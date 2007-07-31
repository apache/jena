/*
 (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP, all rights reserved.
 [See end of file]
 $Id: TestFileUtils.java,v 1.12 2007-07-31 16:24:04 jeremy_carroll Exp $
 */

package com.hp.hpl.jena.util.test;

import com.hp.hpl.jena.util.FileUtils;

import junit.framework.*;

/**
 * TestFileUtils
 * 
 * @author kers
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

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */