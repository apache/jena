/*
 *  (c) Copyright 2001  Hewlett-Packard Development Company, LP
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
 
 * * $Id: NTriple.java,v 1.8 2004-01-20 10:05:26 jeremy_carroll Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * XML2NTriple.java
 *
 * Created on July 13, 2001, 10:06 PM
 */

package com.hp.hpl.jena.rdf.arp;

import java.net.*;
import java.io.*;

import org.xml.sax.*;
/** A command line interface into ARP.
 * Creates NTriple's or just error messages.
 * <pre>
 * java &lt;class-path&gt; com.hp.hpl.jena.arp.NTriple ( [ -[xstfu]][ -b xmlBase -[eiw] NNN[,NNN...] ] [ file ] [ url ] )...
 * </pre>
 * <p>
 * &lt;class-path&gt; should contain <code>jena.jar</code>, <code>xerces.jar</code>,  and <code>icu4j.jar</code> or equivalents.
 * </p>
 * All options, files and URLs can be intemingled in any order.
 * They are processed from left-to-right.
 * <dl>
 * file    </dt><dd>  Converts (embedded) RDF in XML file into N-triples
 * </dd><dt>
 * url  </dt><dd>     Converts (embedded) RDF from URL into N-triples
 * </dd><dt>
 * -b uri </dt><dd>   Sets XML Base to the absolute URI.
 * </dd><dt>
 * -r    </dt><dd>    Content is RDF (no embedding, rdf:RDF tag may be omitted).
 * </dd><dt>
 * -t  </dt><dd>      No n-triple output, error checking only.
 * </dd><dt>
 * -x   </dt><dd>     Lax mode - warnings are suppressed.
 * </dd><dt>
 * -s    </dt><dd>    Strict mode - most warnings are errors.
 * </dd><dt>
 * -u     </dt><dd>   Allow unqualified attributes (defaults to warning).
 * </dd><dt>
 * -f    </dt><dd>    All errors are fatal - report first one only.
 * </dd><dt>
 * -n    </dt><dd>    Show line numbers of each triple.
 * </dd><dt>
 * -b url </dt><dd>   Sets XML Base to the absolute url.
 * </dd><dt>
 * -e NNN[,NNN...]</dt><dd>
 * Treats numbered warning conditions as errrors.
 * </dd><dt>
 * -w NNN[,NNN...]</dt><dd>
 * Treats numbered error conditions as warnings.
 * </dd><dt>
 * -i NNN[,NNN...]
 * </dt><dd>
 * Ignores numbered error/warning conditions.
 * </dl>
 * @author jjc
 */
public class NTriple implements ARPErrorNumbers {

	private static StringBuffer line = new StringBuffer();
	private static ARP arp;
	private static String xmlBase = null;
	private static boolean numbers = false;
	/** Starts an RDF/XML to NTriple converter.
	 * @param args The command-line arguments.
	 */
	static public void main(String args[]) {
		mainEh(args, null, null);
	}
	static StatementHandler andMeToo = null;
	/** Starts an RDF/XML to NTriple converter,
	 * using an error handler, and an ARPHandler.
	 * Statements get processed both by this class,
	 * and by the passed in StatementHandler
	 * @param args The command-line arguments.
	 * @param eh Can be null.
	 * @param ap Can be null.
	 */
	static public void mainEh(String args[], ErrorHandler eh, ARPHandler ap) {
		boolean doneOne = false;
		andMeToo = ap;
		//SH sh = new SH();
		int i;
		arp = new ARP();
		arp.setStatementHandler(getSH(true));
		if (ap != null) {
			arp.setNamespaceHandler(ap);
			arp.setExtendedHandler(ap);
		}
		if (eh != null)
			arp.setErrorHandler(eh);

		for (i = 0; i < args.length - 1; i++) {
			if (args[i].startsWith("-")) {
				i += processOpts(args[i].substring(1), args[i + 1]);
			} else {
				doneOne = true;
				process(args[i]);
			}
		}
		if (args.length > 0) {
			if (args[i].startsWith("-")) {
				if (doneOne || processOpts(args[i].substring(1), "100") == 1)
					usage();
			} else {
				doneOne = true;
				process(args[i]);
			}
		}
		if (!doneOne) {
			process(System.in, "http://example.org/stdin", "standard input");
		}
	}

	/**
	 * @param b false for quiet.
	 * @return
	 */
	private static StatementHandler getSH(boolean b) {
		StatementHandler rslt = b?(StatementHandler)new SH():new NoSH();
		if (andMeToo!=null)
		  rslt = new TwoSH(rslt,andMeToo);
		return rslt;
	}

	static private void lineNumber() {
		if (numbers) {
			Locator locator = arp.getLocator();
			if (locator != null)
				print(
					"# "
						+ locator.getSystemId()
						+ ":"
						+ locator.getLineNumber()
						+ "("
						+ locator.getColumnNumber()
						+ ")\n");
		}
	}

	/*
	 * Options:
	 *  -x   Lax, Warnings suppressed
	 *  -s   Strict, Warnings are errors
	 *  -f   All errors are fatal.
	 *  -u   Suppress unqualified attribute warnings
	 *  -t   Error checking only, no n-triple output
	 *  -b:  set xml:base (same for all files?)
	 *  -e:  convert numbered warnings to errors
	 *  -i:  suppress numbered warnings
	 *  -w:  convert numbered errors/suppressed warnings to warnings
	 *  -n:  give line numbers
	 *
	 */
	static void usage() {
		System.err.println(
			"java <class-path> "
				+ NTriple.class.getName()
				+ " ( [ -[xstfu]][ -b xmlBase -[eiw] NNN[,NNN...] ] [ file ] [ url ] )... ");
		System.err.println(
			"    All options, files and URLs can be intemingled in any order.");
		System.err.println("    They are processed from left-to-right.");
		System.err.println(
			"    file      Converts (embedded) RDF in XML file into N-triples");
		System.err.println(
			"    url       Converts (embedded) RDF from URL into N-triples");
		System.err.println("    -b uri    Sets XML Base to the absolute URI.");
		System.err.println(
			"    -r        Content is RDF (no embedding, rdf:RDF tag may be omitted).");
		System.err.println(
			"    -t        No n-triple output, error checking only.");
		System.err.println("    -x        Lax mode - warnings are suppressed.");
		System.err.println(
			"    -s        Strict mode - most warnings are errors.");
		System.err.println("    -n        Show line and column numbers.");
		System.err.println(
			"    -u        Allow unqualified attributes (defaults to warning).");
		System.err.println(
			"    -f        All errors are fatal - report first one only.");
		System.err.println("    -b url    Sets XML Base to the absolute url.");
		System.err.println("    -e NNN[,NNN...]");
		System.err.println(
			"              Treats numbered warning conditions as errrors.");
		System.err.println("    -w NNN[,NNN...]");
		System.err.println(
			"              Treats numbered error conditions as warnings.");
		System.err.println("    -i NNN[,NNN...]");
		System.err.println(
			"              Ignores numbered error/warning conditions.");
		System.exit(1);
	}
	static private int processOpts(String opts, String nextArg) {
		boolean usedNext = false;
		for (int i = 0; i < opts.length(); i++) {
			char opt = opts.charAt(i);
			if ("beiw".indexOf(opt) != -1) {
				if (usedNext)
					usage();
				usedNext = true;
			}
			switch (opt) {
				case 'D':
				arp.setStatementHandler(new StatementHandler(){
int debugC = 0;

Runtime rt = Runtime.getRuntime();
{ rt.gc(); rt.gc(); }
int startMem = (int)(rt.totalMemory()-rt.freeMemory());
					public void statement(AResource subj, AResource pred, AResource obj) {
						statement(null,null,(ALiteral)null);
						
					}

					public void statement(AResource subj, AResource pred, ALiteral lit) {
						if (++debugC%100 == 0) {
							System.out.println(debugC);
							rt.gc();
							System.out.println(rt.totalMemory()-rt.freeMemory()-startMem);
						  rt.gc();
							System.out.println(rt.totalMemory()-rt.freeMemory()-startMem);
						  if (debugC == 500 && false) 
						  try {
						    Thread.sleep(20000);
						  }
						  catch (Exception e){
						  }
						}
							
						
					}
				});
				  break;
				case 'x' :
					arp.setLaxErrorMode();
					break;
				case 's' :
					arp.setStrictErrorMode();
					break;
				case 't' :
					arp.setStatementHandler(getSH(false));
					break;
				case 'r' :
					arp.setEmbedding(false);
					break;
				case 'n' :
					numbers = true;
					break;
				case 'b' :
					xmlBase = nextArg;
					break;
				case 'e' :
					setErrorMode(nextArg, EM_ERROR);
					break;
				case 'i' :
					setErrorMode(nextArg, EM_IGNORE);
					break;
				case 'w' :
					setErrorMode(nextArg, EM_WARNING);
					break;
				case 'f' :
					for (int j = 0; j < 400; j++) {
						if (arp.setErrorMode(j, -1) == EM_ERROR)
							arp.setErrorMode(j, EM_FATAL);
					}
					break;
				case 'u' :
					arp.setErrorMode(WARN_UNQUALIFIED_ATTRIBUTE, EM_IGNORE);
					arp.setErrorMode(WARN_UNQUALIFIED_RDF_ATTRIBUTE, EM_IGNORE);
					break;
				default :
					usage();
			}
		}
		return usedNext ? 1 : 0;
	}

	static private void setErrorMode(String numbers, int mode) {
		int n[] = new int[3];
		int j = 0;
		numbers += ",";
		for (int i = 0; i < numbers.length(); i++) {
			char c = numbers.charAt(i);
			switch (c) {
				case '0' :
				case '1' :
				case '2' :
				case '3' :
				case '4' :
				case '5' :
				case '6' :
				case '7' :
				case '8' :
				case '9' :
					if (j == 3)
						usage();
					n[j++] = c - '0';
					break;
				case ' ' :
				case ';' :
				case ',' :
					if (i == 0)
						usage();
					switch (j) {
						case 0 :
							break;
						case 3 :
							arp.setErrorMode(
								n[0] * 100 + n[1] * 10 + n[2],
								mode);
							j = 0;
							break;
						default :
							usage();
					}
					break;
				default :
					usage();
			}
		}
	}

	static private void process(String surl) {
		InputStream in;
		String xmlBase;

		URL url;
		String baseURL;

		try {
			File ff = new File(surl);
			in = new FileInputStream(ff);
			url = ff.toURL();
			baseURL = url.toExternalForm();
			if (baseURL.startsWith("file:/")
				&& !baseURL.startsWith("file://")) {
				baseURL = "file://" + baseURL.substring(5);
			}
		} catch (Exception ignore) {
			try {
				url = new URL(surl);
				in = url.openStream();
				baseURL = url.toExternalForm();
			} catch (Exception e) {
				System.err.println("ARP: Failed to open: " + surl);
				System.err.println(
					"    " + ParseException.formatMessage(ignore));
				System.err.println("    " + ParseException.formatMessage(e));
				return;
			}
		}
		process(in, baseURL, surl);
	}
	static private void process(InputStream in, String xmlBasex, String surl) {
		String xmlBasey = xmlBase == null ? xmlBasex : xmlBase;
		try {
			arp.load(in, xmlBasey);
		} catch (IOException e) {
			System.err.println(
				"Error: " + surl + ": " + ParseException.formatMessage(e));
		} catch (SAXParseException e) {
			// already reported.
		} catch (SAXException sax) {
			System.err.println(
				"Error: " + surl + ": " + ParseException.formatMessage(sax));
		}
	}
	private static class TwoSH implements StatementHandler {
		final StatementHandler a, b;
		public void statement(AResource subj, AResource pred, AResource obj) {
			a.statement(subj, pred, obj);
			b.statement(subj, pred, obj);
		}
		public void statement(AResource subj, AResource pred, ALiteral lit) {
			a.statement(subj, pred, lit);
			b.statement(subj, pred, lit);
		}
		TwoSH(StatementHandler A, StatementHandler B) {
			a = A;
			b = B;
		}
	}
	private static class NoSH implements StatementHandler {
		public void statement(AResource subj, AResource pred, AResource obj) {
		}
		public void statement(AResource subj, AResource pred, ALiteral lit) {
		}
	}
	private static class SH implements StatementHandler {
		public void statement(AResource subj, AResource pred, AResource obj) {
			lineNumber();
			resource(subj);
			resource(pred);
			resource(obj);
			line.append('.');
			System.out.println(line);
			line.setLength(0);
		}
		public void statement(AResource subj, AResource pred, ALiteral lit) {
			String lang = lit.getLang();
			String parseType = lit.getParseType();
			lineNumber();
			/*
			if (parseType != null) {
				System.out.print("# ");
				if (parseType != null)
					System.out.print("'" + parseType + "'");
				System.out.println();
			}
			*/
			resource(subj);
			resource(pred);
			literal(lit);
			line.append('.');
			System.out.println(line);
			line.setLength(0);
		}
	}
	static private void print(String s) {
		line.append(s);
	}
	static private void resource(AResource r) {
		if (r.isAnonymous()) {
			print("_:j");
			print(r.getAnonymousID());
			print(" ");
		} else {
			print("<");
			escapeURI(r.getURI());
			print("> ");
		}
	}
	static private void escape(String s) {
		int lg = s.length();
		for (int i = 0; i < lg; i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '\\' :
					print("\\\\");
					break;
				case '"' :
					print("\\\"");
					break;
				case '\n' :
					print("\\n");
					break;
				case '\r' :
					print("\\r");
					break;
				case '\t' :
					print("\\t");
					break;
				default :
					if (ch >= 32 && ch <= 126)
						line.append(ch);
					else {
						print("\\u");
						String hexstr = Integer.toHexString(ch).toUpperCase();
						int pad = 4 - hexstr.length();

						for (; pad > 0; pad--)
							print("0");
						print(hexstr);
					}
			}
		}
	}

	static private boolean okURIChars[] = new boolean[128];
	static {
		for (int i = 32; i < 127; i++)
			okURIChars[i] = true;
		okURIChars['<'] = false;
		okURIChars['>'] = false;
		okURIChars['\\'] = false;

	}
	static private void escapeURI(String s) {
		int lg = s.length();
		for (int i = 0; i < lg; i++) {
			char ch = s.charAt(i);
			if (ch < okURIChars.length && okURIChars[ch]) {
				line.append(ch);
			} else {
				print("\\u");
				String hexstr = Integer.toHexString(ch).toUpperCase();
				int pad = 4 - hexstr.length();

				for (; pad > 0; pad--)
					print("0");
				print(hexstr);
			}
		}
	}
	static private void literal(ALiteral l) {
		//if (l.isWellFormedXML())
		//	System.out.print("xml");
		line.append('"');
		escape(l.toString());
		line.append('"');
		String lang = l.getLang();
		if (lang != null && !lang.equals("")) {
			line.append('@');
			print(lang);
		}
		String dt = l.getDatatypeURI();
		if (dt != null && !dt.equals("")) {
			print("^^<");
			escapeURI(dt);
			line.append('>');
		}

		line.append(' ');
	}

}