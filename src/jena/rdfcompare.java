/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003 
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
 * $Id: rdfcompare.java,v 1.4 2003-07-01 14:43:54 andy_seaborne Exp $
 */

package jena;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.mem.ModelMem;

import java.net.URL;
import java.io.FileInputStream;

/** A program which read two RDF models and determines if they are the same.
 *
 *  <p>This program will read two RDF models, in a variety of languages,
 *     and compare them.  Input can be read either from a URL or from a file.
 *     The program writes its results to the standard output stream and sets
 *     its exit code to 0 if the models are equal, to 1 if they are not and
 *     to -1 if it encounters an error.</p>
 *
 *  <p></p>
 *
 *  <pre>java jena.rdfcompare model1 model2 [lang1 [lang2]]
 *
 *       model1 and model2 can be file names or URL's
 *       lang1 and lang2 specify the language of the input and can be:
 *           RDF/XML
 *           N-TRIPLE
 *           N3
 *  </pre>
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.4 $ $Date: 2003-07-01 14:43:54 $
 */
public class rdfcompare extends java.lang.Object {

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
        if (args.length < 2 || args.length > 4) {
            usage();
            System.exit(-1);
        }
        
        String in1 = args[0];
        String in2 = args[1];
        String lang1 = "RDF/XML";
        if (args.length > 2) {
            lang1 = args[2];
        } 
        String lang2 = "N-TRIPLE";
        if (args.length == 4) {
            lang2 = args[3];
        }
        
        System.out.println(in1 + " " + in2 + " " + lang1 + " " + lang2);
        try {
            Model m1 = new ModelMem();
            Model m2 = new ModelMem();
        
            read(m1, in1, lang1);
            read(m2, in2, lang2);
        
            if (m1.isIsomorphicWith(m2)) {
                System.out.println("models are equal");
                System.out.println();
                System.exit(0);
            } else {
                System.out.println("models are unequal");
                System.out.println();
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Unhandled exception:");
            System.err.println("    " + e.toString());
            System.exit(-1);
        }
    }
    
    protected static void usage() {
        System.err.println("usage:");
        System.err.println(
            "    java jena.rdfcompare source1 source2 [lang1 [lang2]]");
        System.err.println();
        System.err.println("    source1 and source2 can be URL's or filenames");
        System.err.println("    lang1 and lang2 can take values:");
        System.err.println("      RDF/XML");
        System.err.println("      N-TRIPLE");
        System.err.println("      N3");
        System.err.println("    lang1 defaults to RDF/XML, lang2 to N-TRIPLE");
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
