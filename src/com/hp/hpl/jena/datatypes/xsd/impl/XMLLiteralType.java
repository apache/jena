/******************************************************************
 * File:        XMLLiteralType.java
 * Created by:  Dave Reynolds
 * Created on:  08-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: XMLLiteralType.java,v 1.8 2004-10-11 11:55:11 jeremy_carroll Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.rdf.arp.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.shared.BrokenException;
import java.io.*;

/**
 * Builtin data type to represent XMLLiteral (i.e. items created
 * by use of <code>rdf:parsetype='literal'</code>.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.8 $ on $Date: 2004-10-11 11:55:11 $
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
    public String unparse(Object value) {
        return value.toString();
    }
    
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        if ( !isValid(lexicalForm))
          throw new DatatypeFormatException("Bad rdf:XMLLiteral");
        return lexicalForm;
    }
    
    /**
     * Test whether the given string is a legal lexical form
     * of this datatype.
     */
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
        	public void fatalError(SAXParseException e){
        		status[0] = true;
        	}
			public void error(SAXParseException e){
				status[0] = true;
			}
			public void warning(SAXParseException e){
				status[0] = true;
			}
        });
        arp.getHandlers().setStatementHandler(new StatementHandler(){
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

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
