/*
    (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    [See end of file]
    $Id: RDFException.java,v 1.13 2005-02-21 12:14:21 andy_seaborne Exp $
 */

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.shared.*;

/** LEGACY Generic RDF Exception class. This class is preserved to allow
 * users moving from Jena 1 to Jena 2 to continue to use the magic numbers
 * for Jena exceptions. The codebase no longer uses these numbers directly,
 * but instead uses particular Jena exceptions. Those exceptions presently
 * subclass RDFException and set the magic numbers, but This Will Change.
 * @author bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.13 $ $Date: 2005-02-21 12:14:21 $
 */
public class RDFException extends JenaException {
    
    protected int       errorCode = 0;
    protected int       otherCode = 0;
    protected Exception nestedException = null;
    protected String    message = null;

    /** A method which would access a model has been invoked on an object
     * with no associated model.
     */
    public static final int NOTRELATEDTOMODEL                 =   1;
    /** The URI supplied for a Property is invalid.
     */
    public static final int INVALIDPROPERTYURI                =   2;
    /** A method which is unsupported has been called.
     */
    public static final int UNSUPPORTEDOPERATION              =   3;
    /** The object of a statement is not a Resource.
     */
    public static final int OBJECTNOTRESOURCE                 =   4;
    /** The object of a Statement is not a Literal.
     */
    public static final int OBJECTNOTLITERAL                  =   5;
    /** A property was not found in the model.
     */
    public static final int PROPERTYNOTFOUND                  =   6;
    /** The resource is no anonymous.
     */
    public static final int NOTANONRESOURCE                   =   7;
    /** The iterator is closed.
     */
    public static final int ITERATORCLOSED                    =   8;
    /** The error code is invalid.
     */
    public static final int INVALIDERRORCODE                  =   9;
    /** This exception contains another which has been caught and encapsulated as
     * an RDF exception.
     */
    public static final int NESTEDEXCEPTION                   =   10;
    /** A literal did not represent a boolean value.
     */
    public static final int INVALIDBOOLEANFORMAT              =  11;
    /** A literal did not contain a valid char.
     */
    public static final int LITERALNOTCHAR                    =  12;
    /** An Alt resource has no default value.
     */
    public static final int ALTHASNODEFAULT                   =  13;
    /** An application supplied Selector has raised an exception, which is
     * encapsulated in this exception.
     */
    public static final int SELECTOREXCEPTION                 =  14;
    /** The object of a statement is not of the expected type.
     */
    public static final int OBJECTWRONGTYPE                   =  15;
    /** A required element does not exist.
     */
    public static final int NOSUCHELEMENT                     =  16;
    /** Internal Error - an assertion has failed.
     */
    public static final int ASSERTIONFAILURE                  =  17;
    /** A sequence index is out of the valid range for that sequence.
     */
    public static final int SEQINDEXBOUNDS                    =  18;
    /** An enhanced resource does not have a constructor of the form
        foo(Resource r)
     */
    public static final int NORESOURCECONSTRUCTOR             =  19;

/** No reader is know for that lanaguage
 */    
    public static final int NOREADERFORLANG                   =  20;
    
/** No Writer is known for that language
 */    
    public static final int NOWRITERFORLANG                   =  21;
    
/** Unknown Property
 */    
    public static final int UNKNOWNPROPERTY                   =  22;

/** Statement not present
 */
    public static final int STATEMENTNOTPRESENT               =  23;
    
/** Syntax Errors in input stream
 */
    public static final int SYNTAXERROR                       =  24;
    
    protected static final int MAXERRORCODE                   =  24;

    protected static final String[] errorMessage = 
        { "not used",
          "Object is not related to a model",
          "Invalid property URI", 
          "Unsupported operation",
          "Object of statement is not a resource",
          "Object of statement is not a literal",
          "Subject does not have that property",
          "Resource is not anonymous",
          "Iterator has been closed",
          "Invalid error code",
          "Nested Exception",
          "Literal is not a boolean",
          "Literal is not a single character",
          "Alt container has no default",
          "Selector threw exception",
          "Statement object does match requested type",
          "Iterator does not have an element",
          "Assertion failure",
          "Sequence index out of range",
          "Enhanced Resource lacks Resource constructor",
          "No RDFReader is defined for that language",
          "No RDFWriter is defined for that lanaguage",
          "Property not known",
          "Statement not present",
          "Syntax errors in input stream"
        };
        
        protected RDFException(){}
    
    /** Create an RDF exception with the given error code.
     * @param errorCode The code number of the error which has occurred.
     */
    public RDFException(int errorCode) {
        if (1<=errorCode && errorCode<=MAXERRORCODE) {
            this.errorCode = errorCode;
        } else {
            this.errorCode = INVALIDERRORCODE;
            this.otherCode = errorCode;
        }
        message = errorMessage[this.errorCode];
    }
    
    /** Encapsulate an exception in an RDFException.
     * @param e The exception to be encapsulated.
     */  
    
    public RDFException(Exception e) {
        this.errorCode = NESTEDEXCEPTION;
        this.message = errorMessage[this.errorCode];
        nestedException = e;
    }

    /** Create a new RDFException with a given error code and message
     * @param errorCode The code number of the error which has occurred.
     * @param message The message associated with the error
     */
    
    public RDFException(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public RDFException(String s) {
        super(s); this.message = s;
    }


    /** Return an error message describing the exception.
     * @return the error message
     */
    public String toString() {
        String m = getMessage(), name = this.getClass().getName();
        return m == null ? name : name + ": " + m;
    }
    
    public String getMessage() {
        String result = Integer.toString(errorCode) + " " + this.message;
        if (errorCode == INVALIDERRORCODE) {
            result = result + " = " + Integer.toString(otherCode);
        } else if (errorCode == NESTEDEXCEPTION
                || errorCode == SELECTOREXCEPTION) {
            result = this.message + " = " + nestedException.toString();
        }
        return result;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
        changed by Chris, because "use this if we haven't one" caused
        a recursive failure of test cases that exported DoesNotReifyException.
    */
    public Throwable getCause() {
        return nestedException ; // was: !=null?nestedException:this;
    }
    public Exception getNestedException() {
        
        return nestedException;
    }
}
/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 * $Id: RDFException.java,v 1.13 2005-02-21 12:14:21 andy_seaborne Exp $
 *
 * Created on 26 July 2000, 07:00
 */
