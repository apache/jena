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

package jena;

import static jena.cmdline.CmdLineUtils.setLog4jConfiguration;

import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.rdf.model.*;

import java.net.*;
import java.io.*;

/** A program which read an RDF model and copy it to the standard output stream.
 *
 *  <p>This program will read an RDF model, in a variety of languages,
 *     and copy it to the output stream in a possibly different language.
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
 *           TURTLE
 *           N3
 *       The input language defaults to RDF/XML and the output language
 *       defaults to N-TRIPLE.
 *  </pre>
 */
public class rdfcopy extends java.lang.Object {

    static { setLog4jConfiguration() ; }

	/**
	* @param args the command line arguments
	*/
	public static void main(String ... args) {
		if ( ( args.length < 1 ) || ( "-h".equals(args[0]) ) ) {
			usage();
			System.exit(-1);
		}

		String in = args[0];
		String inlang = "RDF/XML";
		int j;
		for (j = 1; j < args.length && args[j].contains( "=" ); j++)
        {}
		int lastInProp = j;
		if (j < args.length) {
			inlang = args[j];
		}
		j++;
		String outlang = "N-TRIPLE";
		
		for (; j < args.length && args[j].contains( "=" ); j++)
		{}
		
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
			RDFWriter w = m.getWriter(outlang);
			j++;
			for (; j < lastOutProp; j++) {
				int eq = args[j].indexOf("=");
				w.setProperty(
					args[j].substring(0, eq),
					args[j].substring(eq + 1));
			}
			w.write(m, System.out, null) ;
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
        System.err.println("      TURTLE");
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
