/*
 * VCardReader.java
 *
 * Authors: Alex Barnell, Jeremy Carroll
 * (c) Copyright Hewlett-Packard Company 2002
 * All rights reserved.
 *
 * See end-of-file for license terms.
 */


import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.DAMLModelImpl;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import java.io.*;
import java.util.*;

public class VCardReader {
    public static void main(String[] args)  throws Exception {
	// Create a model, and load both the schema (T-Box) and the instances (A-Box)
	DAMLModel model = new DAMLModelImpl();
	model.read(new FileReader("vcard-daml.rdf"), vcardBaseURI);
	model.read(new FileReader("vcards.rdf"), instanceBaseURI);
        
        // Create useful DAML Class objects
	DAMLClass vcardClass = (DAMLClass)model.getDAMLValue(vcardBaseURI + "#VCARD");

        // Create useful DAML Property objects
	DAMLProperty fnProp = (DAMLProperty)model.getDAMLValue(vcardBaseURI + "#FN");
	DAMLProperty emailProp = (DAMLProperty)model.getDAMLValue(vcardBaseURI + "#EMAIL");
        
	DAMLProperty rdfValueProp = (DAMLProperty)model.getDAMLValue(RDF.value.getURI());
	
	// Find the VCard instances
	Iterator i = vcardClass.getInstances();
	DAMLInstance vcard;
        DAMLInstance email;
	PropertyAccessor pa;
	DAMLDataInstance fullname;
	while (i.hasNext()) {
            vcard = (DAMLInstance) i.next();
            pa = vcard.accessProperty(emailProp);
            Iterator i2 = pa.getAll(true);
            while (i2.hasNext()) {
                email = (DAMLInstance)i2.next();
                if ( email.getPropertyValue(RDF.value).toString()
                       .equals("amanda_cartwright@example.org") ) {
                    // Print name.
                    pa = vcard.accessProperty(fnProp);
                    fullname = (DAMLDataInstance)pa.getDAMLValue();
                    if ( fullname != null )
                        System.out.println(" Formatted Name: " + fullname.getValue());
                    StmtIterator stit = email.listProperties(RDF.type);
                    while ( stit.hasNext() ) {
                        System.out.println(stit.next().getResource().getURI());
                    }
                        
                    
                }
            }

	}
	// Output model
	//model.write(new OutputStreamWriter(System.out), "RDF/XML-ABBREV");
    }

    private static String vcardBaseURI = "http://www.w3.org/2001/vcard-rdf/3.0";
    private static String instanceBaseURI = "urn:vcards";
}

/* 
 * (c) Copyright Hewlett-Packard Company 2002
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
 *****************************************************************************/
