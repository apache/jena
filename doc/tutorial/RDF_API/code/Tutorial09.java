/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Tutorial09.java,v 1.1 2003-06-26 07:22:25 chris-dollin Exp $
*/

package com.hp.hpl.jena.tutorial.rdf;

import com.hp.hpl.jena.rdf.model.*;

import java.io.*;

/** Tutorial 9 - demonstrate graph operations
 *
 * @author  bwm - updated by kers/Daniel
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2003-06-26 07:22:25 $'
 */
public class Tutorial09 extends Object {
    
    static final String inputFileName1 = "vc-db-3.rdf";    
    static final String inputFileName2 = "vc-db-4.rdf";
    
    public static void main (String args[]) {
       try {
            // create an empty model
            Model model1 = ModelFactory.createDefaultModel();
            Model model2 = ModelFactory.createDefaultModel();
           
            // use the class loader to find the input file
            InputStream in1 = Tutorial09.class
                                       .getClassLoader()
                                       .getResourceAsStream(inputFileName1);
            if (in1 == null) {
                throw new IllegalArgumentException(
                                       "File: " + inputFileName1 + " not found");
            }
            InputStream in2 = Tutorial09.class
                                       .getClassLoader()
                                       .getResourceAsStream(inputFileName2);
            if (in2 == null) {
                throw new IllegalArgumentException(
                                       "File: " + inputFileName2 + " not found");
            }
            
            // read the RDF/XML files
            model1.read(new InputStreamReader(in1), "");
            model2.read(new InputStreamReader(in2), "");
            
            // merge the graphs
            Model model = model1.union(model2);
            
            // print the graph as RDF/XML
            model.write(System.out, "RDF/XML-ABBREV");
            System.out.println();
            
        } catch (Exception e) {
            System.out.println("Failed: " + e);
        }
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2003
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
 */