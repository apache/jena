/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Tutorial05.java,v 1.1 2003-06-26 07:22:25 chris-dollin Exp $
*/

package com.hp.hpl.jena.tutorial.rdf;

import com.hp.hpl.jena.rdf.model.*;

import java.io.*;

/** Tutorial 5 - read RDF XML from a file and write it to standard out
 *
 * @author  bwm - updated by kers/Daniel
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2003-06-26 07:22:25 $'
 */
public class Tutorial05 extends Object {
    
    static final String inputFileName 
                             = "vc-db-1.rdf";
                              
    public static void main (String args[]) {
        
        try {
            // create an empty model
            Model model = ModelFactory.createDefaultModel();

            InputStream in = Tutorial05.class
                                       .getClassLoader()
                                       .getResourceAsStream(inputFileName);
            if (in == null) {
                throw new IllegalArgumentException(
                                       "File: " + inputFileName + " not found");
            }
            
            // read the RDF/XML file
            model.read(new InputStreamReader(in), "");
                        
            // write it to standard out
            model.write(System.out);            
          
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
 * Created on 27 January 2001
 */