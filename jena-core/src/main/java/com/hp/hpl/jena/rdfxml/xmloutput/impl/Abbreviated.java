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

package com.hp.hpl.jena.rdfxml.xmloutput.impl;

import java.io.PrintWriter;
import java.io.Writer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.RDFSyntax;
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
   @see com.hp.hpl.jena.rdf.model.RDFWriterF#getWriter(String)
 */
public class Abbreviated extends BaseXMLWriter implements RDFErrorHandler {

	private Resource types[] =
		new Resource[] {
			OWL.Ontology,
			//OWL.DataRange, named or orphaned dataranges unusual.      
			RDFS.Datatype,
			RDFS.Class,
			OWL.Class,
			OWL.ObjectProperty,
			RDF.Property,
			OWL.DatatypeProperty,
			OWL.TransitiveProperty,
			OWL.SymmetricProperty,
			OWL.FunctionalProperty,
			OWL.InverseFunctionalProperty,
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

	@Override
    protected void unblockAll() {
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
        blockRule(RDFSyntax.propertyAttr);
    }
    
    @Override
    protected void blockRule(Resource r) {
        if (r.equals(RDFSyntax.sectionReification)) sReification=true;
       // else if (r.equals(RDFSyntax.resourcePropertyElt)) sResourcePropertyElt=true;
        else if (r.equals(RDFSyntax.sectionListExpand)) sListExpand=true;
        else if (r.equals(RDFSyntax.parseTypeLiteralPropertyElt)) sParseTypeLiteralPropertyElt=true;
        else if (r.equals(RDFSyntax.parseTypeResourcePropertyElt)) sParseTypeResourcePropertyElt=true;
        else if (r.equals(RDFSyntax.parseTypeCollectionPropertyElt)) sParseTypeCollectionPropertyElt=true;
        else if (r.equals(RDFSyntax.idAttr)) {
            sIdAttr=true;
            sReification = true;
        }
        else if (r.equals(RDFSyntax.propertyAttr)) sPropertyAttr=true;
        //else if (r.equals(DAML_OIL.collection)) sDamlCollection=true;
        else {
            logger.warn("Cannot block rule <"+r.getURI()+">");
        }
    }
	@Override
    Resource[] setTypes(Resource[] propValue) {
		Resource[] rslt = types;
		types = propValue;
		return rslt;
	}

	@Override
    synchronized public void write(Model baseModel, Writer out, String base)
	    { 
		if (baseModel.getGraph().getCapabilities().findContractSafe() == false) 
            {
			logger.warn( "Workaround for bugs 803804 and 858163: using RDF/XML (not RDF/XML-ABBREV) writer  for unsafe graph " + baseModel.getGraph().getClass() );
			baseModel.write( out, "RDF/XML", base );
            } 
        else
            super.write( baseModel, out, base );
		}
		
	@Override
    protected void writeBody(
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
	@Override
    public void error(Exception e) {
		errorHandler.error(e);
	}

	@Override
    public void warning(Exception e) {
		errorHandler.warning(e);
	}

	@Override
    public void fatalError(Exception e) {
		errorHandler.fatalError(e);
	}



}
