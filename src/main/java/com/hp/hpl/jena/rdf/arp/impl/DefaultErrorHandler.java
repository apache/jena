/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 
 * * $Id: DefaultErrorHandler.java,v 1.1 2009-06-29 08:55:38 castagna Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * DefaultErrorHandler.java
 *
 * Created on July 10, 2001, 11:23 AM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import com.hp.hpl.jena.rdf.arp.ParseException;

/**
 *
 * @author  jjc
 * 
 */
public class DefaultErrorHandler implements org.xml.sax.ErrorHandler {

    /** Creates new DefaultErrorHandler */
    public DefaultErrorHandler() {
        // no initialization
    }

    @Override
    public void error(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        System.err.println("Error: " + ParseException.formatMessage(e)); 
    }
    
    @Override
    public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        System.err.println("Fatal Error: " + ParseException.formatMessage(e));
        throw e;
    }
    
    @Override
    public void warning(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
        System.err.println("Warning: " + ParseException.formatMessage(e)); 
//        e.printStackTrace();
        
    }
    
}
