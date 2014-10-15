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

public abstract class AbstractRegexpBasedTest {
	protected static final String SPACE = "\\s+";
	protected static final String OPT_SPACE = "\\s*";
	protected static final String WHERE = "WHERE" + SPACE;
	protected static final String OPEN_CURLY = "\\{" + OPT_SPACE;
	protected static final String CLOSE_CURLY = OPT_SPACE + "\\}";
	protected static final String OPEN_PAREN = "\\(" + OPT_SPACE;
	protected static final String CLOSE_PAREN = OPT_SPACE + "\\)";
	protected static final String QUOTE = "\\\"";
	protected static final String LT = "\\<";
	protected static final String GT = "\\>";
	protected static final String DOT = "\\.";
	protected static final String ORDER_BY = "ORDER" + SPACE + "BY" + SPACE;
	protected static final String GROUP_BY = "GROUP" + SPACE + "BY" + SPACE;
	protected static final String HAVING = "HAVING" + SPACE;
	protected static final String PREFIX = "PREFIX" + SPACE;
	protected static final String SELECT = "SELECT" + SPACE;
	protected static final String UNION = "UNION" + SPACE;
	protected static final String LIMIT = "LIMIT" + SPACE;
	protected static final String OFFSET = "OFFSET" + SPACE;
	protected static final String OPTIONAL = "OPTIONAL" + SPACE;

	protected final String quote(String s) {
		return String.format("%s%s%s", QUOTE, s, QUOTE);
	}

	protected final String node(String s) {
		return String.format("%s%s%s", LT, s, GT);
	}

	protected final String var(String s) {
		return "\\?" + s;
	}

	protected final void assertNotContainsRegex(String expected, String lst) {

		Pattern patt = Pattern.compile(expected, Pattern.DOTALL);

		if (patt.matcher(lst).find()) {
			fail(String.format("%s was found in %s", expected, lst));
		}
	}

	protected final void assertContainsRegex(String expected, String entry) {

		Pattern patt = Pattern.compile(expected, Pattern.DOTALL);
		if (patt.matcher(entry).find()) {
			return;
		}
		fail(String.format("%s not found in %s", expected, entry));
	}

	protected final void assertNotContainsRegex(String expected, String[] lst) {

		Pattern patt = Pattern.compile(expected, Pattern.DOTALL);
		for (String s : lst) {
			if (patt.matcher(s).find()) {
				fail(String.format("%s was found in %s", expected,
						Arrays.asList(lst)));
			}
		}
	}

	protected final void assertContainsRegex(String expected, String[] lst) {

		Pattern patt = Pattern.compile(expected, Pattern.DOTALL);
		for (String s : lst) {
			if (patt.matcher(s).find()) {
				return;
			}
		}
		fail(String.format("%s not found in %s", expected, Arrays.asList(lst)));
	}

}
