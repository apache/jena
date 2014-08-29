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

package com.hp.hpl.jena.rdfxml.xmlinput;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.impl.ARPLocation ;

import java.lang.reflect.Field;

/**
 * An exception during the RDF processing of ARP. Note: it is distinguished from
 * an XML related exception from Xerces because while both are
 * SAXParseException's, the latter are not com.hp.hpl.jena.arp.ParseException's.
 */
public class ParseException extends SAXParseException implements
        ARPErrorNumbers {

    /**
     * 
     */
    private static final long serialVersionUID = -5986976549492477885L;
    final int id;

    protected ParseException(int id, ARPLocation where, String msg) {
        super(msg, where.inputName, null, where.endLine, where.endColumn);
        this.id = id;
        
    }



    public ParseException(int id, ARPLocation where, Exception e) {
        super(e.getMessage(), where.inputName, null, where.endLine, where.endColumn,e);
        if (getCause()==null)
            initCause(e);
        this.id = id;
    }


    /**
     * The error number (from {@link ARPErrorNumbers}) related to this
     * exception.
     * 
     * @return The error number.
     */
    public int getErrorNumber() {
        return id;
    }

    /**
     * Is this error an RDF syntax error.
     * A syntax error indicates that well-formed XML,
     * uses RDF properties and attributes, and whitespace
     * and XML elements, in a way that does not conform with
     * the RDF/XML Syntax (Revised) specification.
     * (Currently most such errors have code
     * {@link ARPErrorNumbers#ERR_SYNTAX_ERROR},
     * but this may change in the future).
     * @return True if this is a syntax error
     */
    public boolean isSyntaxError() {
        switch (id) {
        case ERR_SYNTAX_ERROR:
        case ERR_BAD_RDF_ELEMENT:
        case ERR_BAD_RDF_ATTRIBUTE:
        case ERR_LI_AS_TYPE:
        case ERR_NOT_WHITESPACE:
            return true;
        }
        return false;
    }

    SAXParseException rootCause() {
        Exception e = getException();
        return e == null ? this : (SAXParseException) e;
    }

    


    private boolean promoteMe;

    /**
     * Intended for use within an RDFErrorHandler. This method is untested.
     * Marks the exception to be promoted to be thrown from the parser's entry
     * method.
     */
    public void promote() {
        promoteMe = true;
    }


    /**
     * The message without location information. Use either the formatMessage
     * method, or the SAXParseException interface, to access the location
     * information.
     * 
     * @return The exception message.
     */
    @Override
    public String getMessage() {
        // turn 1 to W001
        // turn 204 to E204
        String idStr = id != 0 ? "{" + (id < 200 ? "W" : "E")
                + ("" + (1000 + id)).substring(1) + "} " : "";
        
            return idStr + super.getMessage();
    }



    /**
     * Calls e.getMessage() and also accesses line and column information for
     * SAXParseException's.
     * 
     * @return e.getMessage() possibly prepended by error location information.
     * @param e
     *            The exception to describe.
     */
    static public String formatMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null)
            msg = e.toString();
        if (!(e instanceof SAXParseException))
            return msg;
        SAXParseException sax = (SAXParseException) e;
        String file = sax.getSystemId();
        if (file == null)
            file = sax.getPublicId();
        String rslt = file == null ? "" : file;
        if (sax.getLineNumber() == -1)
            return (file != null ? (file + ": ") : "") + msg;

        if (sax.getColumnNumber() == -1) {
            return rslt + "(line " + sax.getLineNumber() + "): " + msg;
        }
        return rslt + "(line " + sax.getLineNumber() + " column " + sax.getColumnNumber()
                + "): " + msg;

    }

    public boolean isPromoted() {
        return promoteMe;
    }



    /**
     * The  string from
     * {@link ARPErrorNumbers} associated with an integer error code
     * @param errNo An error code from {@link ARPErrorNumbers}.
     * @return The field name from {@link ARPErrorNumbers} with this error number, or null
     */
    static public String errorCodeName(int errNo) {
        Class<?> c = ARPErrorNumbers.class;
        java.lang.reflect.Field flds[] = c.getDeclaredFields();
        for ( Field fld : flds )
        {
            try
            {
                if ( fld.getInt( null ) == errNo )
                {
                    return fld.getName();
                }
            }
            catch ( Exception e )
            {
                // ignore exceptions
            }
        }
        return null;
    }



    /**
     * The integer code associated with a string from
     * {@link ARPErrorNumbers}.
     * @param upper A field name from {@link ARPErrorNumbers}, (in upper case).
     * @return The integer value or -1, if none.
     */
    static public int errorCode(String upper) {
        Class<?> c = ARPErrorNumbers.class;
        try {
            java.lang.reflect.Field fld = c.getField(upper);
            return fld.getInt(null);
        } catch (Exception e) {
            return -1;
        }
    }

}
