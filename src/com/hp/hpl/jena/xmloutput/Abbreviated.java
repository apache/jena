/*
 *  (c)     Copyright Hewlett-Packard Company 2000, 2001, 2002
 *   All rights reserved.
 * [See end of file]
 *  $Id: Abbreviated.java,v 1.2 2003-04-01 17:20:47 jeremy_carroll Exp $
 */

package com.hp.hpl.jena.xmloutput;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.RDFSyntax;
import com.hp.hpl.jena.vocabulary.DAML_OIL;

import java.io.*;
//Writer;
//import java.io.PrintWriter;

/** Writes out RDF in the abbreviated syntax,  for human consumption 
   not only machine readable.
 * It is not normal to call the constructor directly, but to use
 * the method RDFWriterF.getWriter("RDF/XML-ABBREV").
 * Does not support the <code>NSPREFIXPROPBASE</code> system properties.
 * Use <code>setNsPrefix</code>.
 * For best results it is necessary to set the property 
   <code>"prettyTypes"</code>. See setProperty for information.
   @see com.hp.hpl.jena.rdf.model.RDFWriterF#getWriter
 * @author jjc
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.2 $' Date='$Date: 2003-04-01 17:20:47 $'
 */
public class Abbreviated extends BaseXMLWriter implements RDFErrorHandler {

	private Resource types[] =
		new Resource[] {
			DAML_OIL.Ontology,
			DAML_OIL.Datatype,
			DAML_OIL.Class,
			RDFS.Class,
			DAML_OIL.Property,
			DAML_OIL.ObjectProperty,
			DAML_OIL.DatatypeProperty,
			DAML_OIL.TransitiveProperty,
			DAML_OIL.UnambiguousProperty,
			DAML_OIL.UniqueProperty,
			};
            
	boolean sReification;
    
    
	boolean sIdAttr;
    boolean sDamlCollection;
    boolean sParseTypeCollectionPropertyElt;
    boolean sListExpand;
    boolean sParseTypeLiteralPropertyElt;
    boolean sParseTypeResourcePropertyElt;
    boolean sPropertyAttr;
    

    boolean sResourcePropertyElt;

	void unblockAll() {
		sDamlCollection = false;
		sReification = false;
		sResourcePropertyElt = false;
		sParseTypeLiteralPropertyElt = false;
		sParseTypeResourcePropertyElt = false;
		sParseTypeCollectionPropertyElt = false;
		sIdAttr = false;
		sPropertyAttr = false;
        sListExpand = false;
	}
    {
        unblockAll();
    }
    void blockRule(Resource r) {
        if (r.equals(RDFSyntax.sectionReification)) sReification=true;
        else if (r.equals(RDFSyntax.resourcePropertyElt)) sResourcePropertyElt=true;
else if (r.equals(RDFSyntax.sectionListExpand)) sListExpand=true;
        else if (r.equals(RDFSyntax.parseTypeLiteralPropertyElt)) sParseTypeLiteralPropertyElt=true;
        else if (r.equals(RDFSyntax.parseTypeResourcePropertyElt)) sParseTypeResourcePropertyElt=true;
        else if (r.equals(RDFSyntax.parseTypeCollectionPropertyElt)) sParseTypeCollectionPropertyElt=true;
        else if (r.equals(RDFSyntax.idAttr)) {
            sIdAttr=true;
            sReification = true;
        }
        else if (r.equals(RDFSyntax.propertyAttr)) sPropertyAttr=true;
        else if (r.equals(DAML_OIL.collection)) sDamlCollection=true;
        else {
            logger.warn("Cannot block rule <"+r.getURI()+">");
        }
    }
	Resource[] setTypes(Resource[] propValue) {
		Resource[] rslt = types;
		types = (Resource[]) propValue;
		return rslt;
	}

	void writeBody(
		Model model,
		PrintWriter pw,
		String base,
		boolean useXMLBase) {
		Unparser unp = new Unparser(this, base, model, pw);

		unp.setTopLevelTypes(types);
		//unp.useNameSpaceDecl(nameSpacePrefices);
		if (useXMLBase)
			unp.setXMLBase(base);
		unp.write();
	}

	// Implemenatation of RDFErrorHandler
	public void error(Exception e) {
		errorHandler.error(e);
	}

	public void warning(Exception e) {
		errorHandler.warning(e);
	}

	public void fatalError(Exception e) {
		errorHandler.fatalError(e);
	}

	static public void main(String args[]) throws Exception {
		System.out.println("Test code for bug 77");
		Model m = new com.hp.hpl.jena.mem.ModelMem();
		m.read(
			new FileInputStream("modules/rdf/regression/arp/bug51_0.rdf"),
			"http://example.org/file");
		RDFWriter pw = m.getWriter("RDF/XML-ABBREV");
		pw.setNsPrefix("eg", "http://example.org/");
		pw.setNsPrefix("eg2", "http://example.org/foo#");
		pw.write(m, System.out, "http://example.org/file");
	}

}
/*
	(c) Copyright Hewlett-Packard Company 2002
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
