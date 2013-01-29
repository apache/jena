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

import com.hp.hpl.jena.rdf.model.*;

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
 */
public class rdfcompare extends java.lang.Object {

    static { setLog4jConfiguration() ; }

    /**
    * @param args the command line arguments
    */
    public static void main (String ... args) {
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
            Model m1 = ModelFactory.createDefaultModel();
            Model m2 = ModelFactory.createDefaultModel();
        
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
