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

package com.hp.hpl.jena.datatypes.xsd.impl;

import java.io.IOException ;
import java.io.StringReader ;

import org.xml.sax.ErrorHandler ;
import org.xml.sax.SAXException ;
import org.xml.sax.SAXParseException ;

import com.hp.hpl.jena.datatypes.BaseDatatype ;
import com.hp.hpl.jena.datatypes.DatatypeFormatException ;
import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.rdfxml.xmlinput.ALiteral ;
import com.hp.hpl.jena.rdfxml.xmlinput.ARP ;
import com.hp.hpl.jena.rdfxml.xmlinput.AResource ;
import com.hp.hpl.jena.rdfxml.xmlinput.StatementHandler ;
import com.hp.hpl.jena.shared.BrokenException ;
import com.hp.hpl.jena.vocabulary.RDF ;

/**
 * Builtin data type to represent XMLLiteral (i.e. items created
 * by use of <code>rdf:parsetype='literal'</code>.
 */
public class XMLLiteralType extends BaseDatatype implements RDFDatatype {
    /** Singleton instance */
    public static final RDFDatatype theXMLLiteralType = new XMLLiteralType(RDF.getURI() + "XMLLiteral");
    
    /**
     * Private constructor.
     */
    private XMLLiteralType(String uri) {
        super(uri);
    }
    
    /**
     * Convert a serialize a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        return value.toString();
    }
    
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        if ( !isValid(lexicalForm))
          throw new DatatypeFormatException("Bad rdf:XMLLiteral");
        return lexicalForm;
    }
    
    /**
     * Test whether the given string is a legal lexical form
     * of this datatype.
     */
    @Override
    public boolean isValid(final String lexicalForm) {
        /*
         * To check the lexical form we construct
         * a dummy RDF/XML document and parse it with
         * ARP. ARP performs an exclusive canonicalization,
         * the dummy document has exactly one triple.
         * If the lexicalForm is valid then the resulting
         * literal found by ARP is unchanged.
         * All other scenarios are either impossible
         * or occur because the lexical form is invalid.
         */
        final boolean status[] = new boolean[]{false,false,false};
        // status[0] true on error or other reason to know that this is not well-formed
        // status[1] true once first triple found
        // status[2] the result (good if status[1] and not status[0]).
        
        ARP arp = new ARP();
        
        arp.getHandlers().setErrorHandler(new ErrorHandler(){
        	@Override
            public void fatalError(SAXParseException e){
        		status[0] = true;
        	}
			@Override
            public void error(SAXParseException e){
				status[0] = true;
			}
			@Override
            public void warning(SAXParseException e){
				status[0] = true;
			}
        });
        arp.getHandlers().setStatementHandler(new StatementHandler(){
        @Override
        public void statement(AResource a, AResource b, ALiteral l){
        	/* this method is invoked exactly once
        	 * while parsing the dummy document.
        	 * The l argument is in exclusive canonical XML and
        	 * corresponds to where the lexical form has been 
        	 * in the dummy document. The lexical form is valid
        	 * iff it is unchanged.
        	 */
        	if (status[1] || !l.isWellFormedXML()) {
				status[0] = true;
			}
			//throw new BrokenException("plain literal in XMLLiteral code.");
            status[1] = true;
            status[2] = l.toString().equals(lexicalForm);
        }
		@Override
        public void statement(AResource a, AResource b, AResource l){
	      status[0] = true;
	      //throw new BrokenException("resource valued RDF/XML in XMLLiteral code.");
	    }
        });
        try {
        
        arp.load(new StringReader(
        "<rdf:RDF  xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n"
        +"<rdf:Description><rdf:value rdf:parseType='Literal'>"
        +lexicalForm+"</rdf:value>\n"
        +"</rdf:Description></rdf:RDF>"
        ));
        
        }
        catch (IOException ioe){
           throw new BrokenException(ioe);	
        }
        catch (SAXException s){
        	return false;
        }
        
        
        return (!status[0])&&status[1]&&status[2];
    }    

}
