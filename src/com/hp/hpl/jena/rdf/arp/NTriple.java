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
 
 * * $Id: NTriple.java,v 1.4 2003-08-27 13:05:52 andy_seaborne Exp $
   
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

	private static ARP arp;
	private static String xmlBase = null;
    private static boolean numbers = false;
	/** Starts an RDF/XML to NTriple converter.
	 * @param args The command-line arguments.
	 */
	static public void main(String args[]) {
		mainEh(args, null);
	}
	/** Starts an RDF/XML to NTriple converter.
	 * @param args The command-line arguments.
	 */
	static public void mainEh(String args[], ErrorHandler eh) {
		boolean doneOne = false;
		SH sh = new SH();
		int i;
		arp = new ARP();
		arp.setStatementHandler(sh);
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
    
    static private void lineNumber() {
        if (numbers) {
            Locator locator = arp.getLocator();
            if ( locator != null)
          System.out.println("# "+locator.getSystemId()+":"+locator.getLineNumber()+"("+
          locator.getColumnNumber()+")");
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
		System.err.println("    -t        No n-triple output, error checking only.");
		System.err.println("    -x        Lax mode - warnings are suppressed.");
		System.err.println("    -s        Strict mode - most warnings are errors.");
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
		System.err.println("              Ignores numbered error/warning conditions.");
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
				case 'x' :
					arp.setLaxErrorMode();
					break;
				case 's' :
					arp.setStrictErrorMode();
					break;
				case 't' :
					arp.setStatementHandler(new NoSH());
					break;
				case 'r' :
					arp.setEmbedding(false);
					break;
                    case 'n':
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
							arp.setErrorMode(n[0] * 100 + n[1] * 10 + n[2], mode);
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
		try {
			File ff = new File(surl);
			in = new FileInputStream(ff);
			url = ff.toURL();
		} catch (Exception ignore) {
			try {
				url = new URL(surl);
				in = url.openStream();
			} catch (Exception e) {
				System.err.println("ARP: Failed to open: " + surl);
				System.err.println("    " + ParseException.formatMessage(ignore));
				System.err.println("    " + ParseException.formatMessage(e));
				return;
			}
		}
		process(in, url.toExternalForm(), surl);
	}
	static private void process(InputStream in, String xmlBasex, String surl) {
		String xmlBasey = xmlBase == null ? xmlBasex : xmlBase;
		try {
			arp.load(in, xmlBasey);
		} catch (IOException e) {
			System.err.println("Error: " + surl + ": " + ParseException.formatMessage(e));
		} catch (SAXParseException e) {
            // already reported.
        } catch (SAXException sax) {
			System.err.println("Error: " + surl + ": " + ParseException.formatMessage(sax));
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
			System.out.println(".");
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
			System.out.println(".");
		}
	}
	static private void resource(AResource r) {
		if (r.isAnonymous())
			System.out.print("_:j" + r.getAnonymousID() + " ");
		else {
			System.out.print("<");
			escapeURI(r.getURI());
			System.out.print("> ");
		}
	}
	static private void escape(String s) {
		char ar[] = s.toCharArray();
		for (int i = 0; i < ar.length; i++) {
			switch (ar[i]) {
				case '\\' :
					System.out.print("\\\\");
					break;
				case '"' :
					System.out.print("\\\"");
					break;
				case '\n' :
					System.out.print("\\n");
					break;
				case '\r' :
					System.out.print("\\r");
					break;
				case '\t' :
					System.out.print("\\t");
					break;
				default :
					if (ar[i] >= 32 && ar[i] <= 126)
						System.out.print(ar[i]);
					else {
						System.out.print("\\u");
						String hexstr = Integer.toHexString(ar[i]).toUpperCase();
						int pad = 4 - hexstr.length();

						for (; pad > 0; pad--)
							System.out.print("0");
						System.out.print(hexstr);
					}
			}
		}
	}
	
	static private boolean okURIChars[] = new boolean[128];
	static {
		for (int i= 32; i<127; i++)
		  okURIChars[i] =true;
	    okURIChars['<'] = false;
	    okURIChars['>'] = false;
	    okURIChars['\\'] = false;
		       
	}
	static private void escapeURI(String s) {
		char ar[] = s.toCharArray();
		for (int i = 0; i < ar.length; i++) {
			if (ar[i]<okURIChars.length && okURIChars[ar[i]]) {
						System.out.print(ar[i]);
			} else {
						System.out.print("\\u");
						String hexstr = Integer.toHexString(ar[i]).toUpperCase();
						int pad = 4 - hexstr.length();

						for (; pad > 0; pad--)
							System.out.print("0");
						System.out.print(hexstr);
			}
		}
	}
	static private void literal(ALiteral l) {
		//if (l.isWellFormedXML())
		//	System.out.print("xml");
		System.out.print("\"");
		escape(l.toString());
		System.out.print("\"");
		String lang = l.getLang();
		if (lang != null && !lang.equals(""))
			System.out.print("@" + lang);
        String dt = l.getDatatypeURI();
        if ( dt != null && !dt.equals("")) {
            System.out.print("^^<");
            escapeURI(dt);
            System.out.print(">");
        }
           
		System.out.print(" ");
	}

}