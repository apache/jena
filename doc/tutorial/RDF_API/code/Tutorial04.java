/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Tutorial04.java,v 1.2 2003-06-27 08:19:15 chris-dollin Exp $
*/

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/** Tutorial 4 - create a model and write it in XML form to standard out
 *
 * @author  bwm - updated by Kers/Daniel
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.2 $' Date='$Date: 2003-06-27 08:19:15 $'
 */
public class Tutorial04 extends Object {
    
    // some definitions
    static String tutorialURI  = "http://hostname/rdf/tutorial/";
    static String briansName   = "Brian McBride";
    static String briansEmail1 = "brian_mcbride@hp.com";
    static String briansEmail2 = "brian_mcbride@hpl.hp.com";
    static String title        = "An Introduction to RDF and the Jena API";
    static String date         = "23/01/2001";
    
    public static void main (String args[]) {
    
        // some definitions
        String personURI    = "http://somewhere/JohnSmith";
        String givenName    = "John";
        String familyName   = "Smith";
        String fullName     = givenName + " " + familyName;
        
        try {
            // create an empty model
            Model model = ModelFactory.createDefaultModel();

            // create the resource
            //   and add the properties cascading style
            Resource johnSmith 
              = model.createResource(personURI)
                     .addProperty(VCARD.FN, fullName)
                     .addProperty(VCARD.N, 
                                  model.createResource()
                                       .addProperty(VCARD.Given, givenName)
                                       .addProperty(VCARD.Family, familyName));
            
            // now write the model in XML form to a file
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
