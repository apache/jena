/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
 *
 * $Id: rdfcopy.java,v 1.8 2004-03-17 19:17:48 jeremy_carroll Exp $
 */

package jena;

import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.rdf.model.*;

import java.net.*;
import java.io.*;

/** A program which read an RDF model and copy it to the standard output stream.
 *
 *  <p>This program will read an RDF model, in a variety of languages,
 *     and copy it to the output stream in a possibly different langauge.
 *     Input can be read either from a URL or from a file.
 *     The program writes its results to the standard output stream and sets
 *     its exit code to 0 if the program terminate normally,  and
 *     to -1 if it encounters an error.</p>
 *
 *  <p></p>
 *
 *  <pre>java jena.rdfcopy model [inlang [outlang]]
 *
 *       model1 and model2 can be file names or URL's
 *       inlang and outlang specify the language of the input and output
 *       respectively and can be:
 *           RDF/XML
 *           N-TRIPLE
 *           N3
 *       The input language defaults to RDF/XML and the output language
 *       defaults to N-TRIPLE.
 *  </pre>
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.8 $ $Date: 2004-03-17 19:17:48 $
 */
public class rdfcopy extends java.lang.Object {

	/**
	* @param args the command line arguments
	*/
	public static void main(String args[]) {

		if (args.length < 1) {
			usage();
			System.exit(-1);
		}

		String in = args[0];
		String inlang = "RDF/XML";
		int j;
		for (j = 1; j < args.length && args[j].indexOf("=") != -1; j++);
		int lastInProp = j;
		if (j < args.length) {
			inlang = args[j];
		}
		j++;
		String outlang = "N-TRIPLE";
		for (; j < args.length && args[j].indexOf("=") != -1; j++);
		int lastOutProp = j;
		if (j < args.length) {
			outlang = args[j];
		}
		if (j + 1 < args.length) {
         //   System.err.println(j+"<<"+args.length);
			usage();
			System.exit(-1);
		}

		try {
			Model m = ModelFactory.createDefaultModel();
            String base = in ;
			RDFReader rdr = m.getReader(inlang);
			for (j = 1; j < lastInProp; j++) {
				int eq = args[j].indexOf("=");
				rdr.setProperty(
					args[j].substring(0, eq),
					args[j].substring(eq + 1));
			}
            
            try {
                rdr.read(m, in);
            } catch (JenaException ex)
            {
                if ( ! ( ex.getCause() instanceof MalformedURLException ) )
                    throw ex ;
                // Tried as a URL.  Try as a file name.
                // Make absolute
                File f = new File(in) ;
                base = "file:///"+f.getCanonicalPath().replace('\\','/') ;
                rdr.read(m, new FileInputStream(in), base) ;
            }
			//rdr.read(m, in);
			try {
				Runtime rt = Runtime.getRuntime();
				rt.gc();
				rt.gc();
				System.err.println(rt.totalMemory()-rt.freeMemory());
				System.err.println("Kill now!");
				Thread.sleep(30000);
			} 
			catch (Exception e) {
			}
			RDFWriter w = m.getWriter(outlang);
			j++;
			for (; j < lastOutProp; j++) {
				int eq = args[j].indexOf("=");
				w.setProperty(
					args[j].substring(0, eq),
					args[j].substring(eq + 1));
			}
            w.write(m,System.out,base);
			System.exit(0);
		} catch (Exception e) {
			System.err.println("Unhandled exception:");
			System.err.println("    " + e.toString());
			System.exit(-1);
		}
	}

	protected static void usage() {
		System.err.println("usage:");
		System.err.println("    java jena.rdfcopy in {inprop=inval}* [ inlang  {outprop=outval}* outlang]]");
		System.err.println();
		System.err.println("    in can be a URL or a filename");
		System.err.println("    inlang and outlang can take values:");
		System.err.println("      RDF/XML");
        System.err.println("      RDF/XML-ABBREV");
        System.err.println("      N-TRIPLE");
		System.err.println("      N3");
		System.err.println(
			"    inlang defaults to RDF/XML, outlang to N-TRIPLE");
        System.err.println("    The legal values for inprop and outprop depend on inlang and outlang.");
        System.err.println("    The legal values for inval and outval depend on inprop and outprop.");
		System.err.println();
	}

	protected static void read(Model model, String in, String lang)
		throws java.io.FileNotFoundException {
		try {
			URL url = new URL(in);
			model.read(in, lang);
		} catch (java.net.MalformedURLException e) {
			model.read(new FileInputStream(in), "", lang);
		}
	}
}
