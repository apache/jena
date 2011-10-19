/*
 *  (c) Copyright 2001, 2002  Hewlett-Packard Development Company, LP
 * See end of file.
 */

package com.hp.hpl.jena.rdf.arp.impl;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
/**
 * This class is not part of the API.
 * It is public merely for test purposes.
 * @author Jeremy Carroll
 *
 * 
 */
public class ARPSaxErrorHandler extends Object implements org.xml.sax.ErrorHandler {
    protected RDFErrorHandler errorHandler;
    
    public ARPSaxErrorHandler(RDFErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    @Override
    public void error(SAXParseException e) throws SAXException {
        errorHandler.error(e);
    }
    
    @Override
    public void warning(SAXParseException e) throws SAXException {
        errorHandler.warning(e);
    }
    
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        errorHandler.fatalError(e);
    }

	/**
	 * @param errorHandler The errorHandler to set.
	 */
	public void setErrorHandler(RDFErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

}
/*
 *  (c) Copyright 2001, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 */