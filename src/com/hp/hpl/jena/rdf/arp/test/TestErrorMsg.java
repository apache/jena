/*
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP All
 * rights reserved. [See end of file] $Id: TestXMLFeatures.java,v 1.35
 * 2003/11/29 15:07:53 jeremy_carroll Exp $
 */

package com.hp.hpl.jena.rdf.arp.test;

import com.hp.hpl.jena.rdf.arp.*;

import junit.framework.*;
import org.xml.sax.*;
import org.apache.oro.text.awk.AwkCompiler;
import org.apache.oro.text.awk.AwkMatcher;
import org.apache.oro.text.regex.MalformedPatternException;

import java.io.*;

public class TestErrorMsg extends TestCase {
	static AwkCompiler awk = new AwkCompiler();
	static AwkMatcher matcher = new AwkMatcher();

	public TestErrorMsg(String name) {
		super(name);
	}
	public String toString() {
		return getName();
	}

	public static Test suite() {
		TestSuite s= new TestSuite(TestErrorMsg.class);
		s.setName("ARP Error Messages");
		return s;
	}

	/**
	 * @param filename
	 *            Read this file
	 * @param regex
	 *            Error msg must match this.
	 *
	private void check(String filename, String regex)
		throws IOException, MalformedPatternException, SAXException {
		check(filename, regex, null);
	}
	*/
	private void check(
		String filename,
		String regexPresent,
		String regexAbsent)
		throws IOException, MalformedPatternException {
		final StringBuffer buf = new StringBuffer();
		ARP arp = new ARP();
		arp.getHandlers().setErrorHandler(new ErrorHandler() {

			public void warning(SAXParseException exception) {
				buf.append(exception.getMessage());
				buf.append("\n");
			}

			public void error(SAXParseException e) {
				warning(e);
			}

			public void fatalError(SAXParseException e) {
				warning(e);
			}

		});
		InputStream in = new FileInputStream("testing/arp/error-msgs/"+filename+".rdf");
		try {
		arp.load(in, "file:///" + filename);
		}
		catch (SAXException e){
			
		}

		in.close();
		String contents = buf.toString();

		if (regexPresent != null)
			assertTrue(
				"Should find /" + regexPresent + "/",
				matcher.contains(contents, awk.compile(regexPresent)));
		if (regexAbsent != null)
			assertTrue(
				"Should not find /" + regexAbsent + "/",
				!matcher.contains(contents, awk.compile(regexAbsent)));
		contents = null;
	}
	
	public void testErrMsg01() throws Exception {
		check("test01",null,"Unusual");
	}

	public void testErrMsg02() throws Exception {
		check("test02","parseType","Unusual");
	}
	public void testErrMsg03() throws Exception {
		check("test03","parseType","Unusual");
	}
	public void testErrMsg04a() throws Exception {
		check("test04",null,"Unusual");
	}
	public void testErrMsg04b() throws Exception {
		check("test04",null,"parseType");
	}
	public void testErrMsg05() throws Exception {
		check("test05",null,"Unusual");
	}
	public void testUTF8() throws Exception {
		check("testutf8","UTF","Unusual");
	}
}
/*
 * (c) Copyright  2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP All
 * rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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
 * 
 * $Id: TestErrorMsg.java,v 1.5 2005-02-21 12:11:08 andy_seaborne Exp $
 */
