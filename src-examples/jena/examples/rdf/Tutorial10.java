/*
 * (c) Copyright 2003, 2004, Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 * $Id: Tutorial10.java,v 1.1 2005-03-12 12:51:32 andy_seaborne Exp $
 */
package jena.examples.rdf ;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/** Tutorial 10 - demonstrate a container
 *
 * @author  bwm - updated by kers/Daniel
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2005-03-12 12:51:32 $'
 */
public class Tutorial10 extends Object {
    
    static final String inputFileName = "vc-db-1.rdf";
    
    public static void main (String args[]) {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();
       
        // use the class loader to find the input file
        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        
        // read the RDF/XML file
        model.read(new InputStreamReader(in), "");
        
        // create a bag
        Bag smiths = model.createBag();
        
        // select all the resources with a VCARD.FN property
        // whose value ends with "Smith"
        StmtIterator iter = model.listStatements(
            new 
                SimpleSelector(null, VCARD.FN, (RDFNode) null) {
                    public boolean selects(Statement s) {
                            return s.getString().endsWith("Smith");
                    }
                });
        // add the Smith's to the bag
        while (iter.hasNext()) {
            smiths.add( iter.nextStatement().getSubject());
        }
        
        // print the graph as RDF/XML
        model.write(new PrintWriter(System.out));
        System.out.println();
        
        // print out the members of the bag
        NodeIterator iter2 = smiths.iterator();
        if (iter2.hasNext()) {
            System.out.println("The bag contains:");
            while (iter2.hasNext()) {
                System.out.println("  " +
                    ((Resource) iter2.next())
                                     .getRequiredProperty(VCARD.FN)
                                     .getString());
            }
        } else {
            System.out.println("The bag is empty");
        }
    }
}

/*
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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
 */
