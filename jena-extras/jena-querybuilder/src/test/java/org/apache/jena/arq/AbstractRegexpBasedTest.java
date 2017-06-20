/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.jena.JenaRuntime ;

public abstract class AbstractRegexpBasedTest {
	protected static final String SPACE = "\\s+";
	protected static final String OPT_SPACE = "\\s*";
	protected static final String WHERE = "WHERE" + SPACE;
	protected static final String OPEN_CURLY = "\\{" + OPT_SPACE;
	protected static final String CLOSE_CURLY = OPT_SPACE + "\\}";
	protected static final String OPEN_PAREN = "\\(" + OPT_SPACE;
	protected static final String CLOSE_PAREN = OPT_SPACE + "\\)";
	protected static final String QUOTE = "\\\"";
	protected static final String LT = "\\<"+OPT_SPACE;
	protected static final String GT = "\\>"+OPT_SPACE;
	protected static final String EQ = "="+OPT_SPACE;
	protected static final String DOT = OPT_SPACE+"\\.";
	protected static final String ORDER_BY = "ORDER" + SPACE + "BY" + SPACE;
	protected static final String GROUP_BY = "GROUP" + SPACE + "BY" + SPACE;
	protected static final String HAVING = "HAVING" + SPACE;
	protected static final String PREFIX = "PREFIX" + SPACE;
	protected static final String CONSTRUCT = "CONSTRUCT" + SPACE;
	protected static final String ASK = "ASK" + SPACE;
	protected static final String SELECT = "SELECT" + SPACE;
	protected static final String DESCRIBE = "DESCRIBE" + SPACE;
	protected static final String UNION = "UNION" + SPACE;
	protected static final String LIMIT = "LIMIT" + SPACE;
	protected static final String OFFSET = "OFFSET" + SPACE;
	protected static final String OPTIONAL = "OPTIONAL" + SPACE;
	protected static final String BIND = "BIND";
	protected static final String SEMI = OPT_SPACE+"\\;";
	protected static final String VALUES = "VALUES" + SPACE;
	protected static final String MINUS = "MINUS" + SPACE;
	protected static final String PAREN_OPEN = "\\(";
    protected static final String PAREN_CLOSE = "\\)";

	protected static String quote(String s) {
		return String.format("%s%s%s", QUOTE, s, QUOTE);
	}

	protected static String uri(String s) {
		return String.format("%s%s%s", LT, s, GT);
	}

	/** Regex for rdf:type as a URI or the abbreviation 'a' */
	protected static String regexRDFtype = "("+uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")+"|a)" ;
	
	protected final static String var(String s) {
		return "\\?" + s;
	}

	/** Match the type of a xsd:string typed term.
	 * RDF 1.0 : use ^^xsd:string form.
	 * RDF 1.1 : use untyped form.
	 */
	protected final static String presentStringType() {
	    return 
	        JenaRuntime.isRDF11 ? "" : "\\^\\^\\<http://www.w3.org/2001/XMLSchema#string\\>" ;
	}

	protected final static void assertNotContainsRegex(String expected, String lst) {

		Pattern patt = Pattern.compile(expected, Pattern.DOTALL);

		if (patt.matcher(lst).find()) {
			fail(String.format("%s was found in %s", expected, lst));
		}
	}

	protected final static void assertContainsRegex(String expected, String entry) {

		Pattern patt = Pattern.compile(expected, Pattern.DOTALL);
		if (patt.matcher(entry).find()) {
			return;
		}
		fail(String.format("%s not found in %s", expected, entry));
	}

	protected final static void assertNotContainsRegex(String expected, String[] lst) {

		Pattern patt = Pattern.compile(expected, Pattern.DOTALL);
		for (String s : lst) {
			if (patt.matcher(s).find()) {
				fail(String.format("%s was found in %s", expected,
						Arrays.asList(lst)));
			}
		}
	}

	protected final static void assertContainsRegex(String expected, String[] lst) {
		Pattern patt = Pattern.compile(expected, Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
		for (String s : lst) {
			if (patt.matcher(s).find()) {
				return;
			}
		}
		fail(String.format("%s not found in %s", expected, Arrays.asList(lst)));
	}

}
