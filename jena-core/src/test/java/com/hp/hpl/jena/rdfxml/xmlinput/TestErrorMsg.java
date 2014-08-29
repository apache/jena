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

package com.hp.hpl.jena.rdfxml.xmlinput;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.ARP ;

public class TestErrorMsg extends TestCase {

	public TestErrorMsg(String name) {
		super(name);
	}
	@Override
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
		throws IOException {
		final StringBuffer buf = new StringBuffer();
		ARP arp = new ARP();
		arp.getHandlers().setErrorHandler(new ErrorHandler() {

			@Override
            public void warning(SAXParseException exception) {
				buf.append(exception.getMessage());
				buf.append("\n");
			}

			@Override
            public void error(SAXParseException e) {
				warning(e);
			}

			@Override
            public void fatalError(SAXParseException e) {
				warning(e);
			}

		});
		try ( InputStream in = new FileInputStream("testing/arp/error-msgs/"+filename+".rdf") ){
		    arp.load(in, "file:///" + filename);
		}
		catch (SAXException e){ }
		String contents = buf.toString();

		if (regexPresent != null)
			assertTrue(
				"Should find /" + regexPresent + "/",
                Pattern.compile(regexPresent,Pattern.DOTALL).matcher(contents).find());
		if (regexAbsent != null)
			assertTrue(
				"Should not find /" + regexAbsent + "/",
				!Pattern.compile(regexAbsent,Pattern.DOTALL).matcher(contents).find());
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
