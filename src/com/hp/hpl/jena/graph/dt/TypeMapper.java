/******************************************************************
 * File:        TypeMapper.java
 * Created by:  Dave Reynolds
 * Created on:  07-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TypeMapper.java,v 1.1.1.1 2002-12-19 19:13:40 bwm Exp $
 *****************************************************************/
package com.hp.hpl.jena.graph.dt;

import java.util.HashMap;
import java.util.Iterator;

/**
 * The TypeMapper provides a global registry of known datatypes.
 * The datatypes can be retrieved by their URI or from the java class
 * that is used to represent them.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1.1.1 $ on $Date: 2002-12-19 19:13:40 $
 */
public class TypeMapper {

//=======================================================================
// Statics

    /**
     * Return the single global instance of the TypeMapper.
     * Done this way rather than simply making the static
     * field directly accessible to allow us to dynamically
     * replace the entire mapper table if needed.
     */
    public static TypeMapper getInstance() {
        return theTypeMap;
    }
    
    /**
     * The single global instance of the TypeMapper
     */
    private static TypeMapper theTypeMap;
    
    /**
     * Static initializer. Adds builtin datatypes to the mapper.
     */
    static {
        theTypeMap = new TypeMapper();
        theTypeMap.registerDatatype(XMLLiteralType.theXMLLiteralType);
        XSDDatatype.loadXSDSimpleTypes(theTypeMap);
    }
    
//=======================================================================
// Variables

    /** Map from uri to datatype */
    private HashMap uriToDT = new HashMap();
    
    /** Map from java class to datatype */
    private HashMap classToDT = new HashMap();
    
//=======================================================================
// Methods

    /**
     * Method getTypeByName.
     * 
     * @param uri the URI of the desired datatype
     * @return Datatype the datatype definition
     * registered at uri, if there is no such registered type it
     * returns the default datatype implementation.
     */
    public RDFDatatype getTypeByName(String uri) {
        RDFDatatype dtype = (RDFDatatype) uriToDT.get(uri);
        if (dtype == null) {
            // @TODO add log message
            // @TODO add switch to prevent warning messages
            dtype = new BaseDatatype(uri);
            registerDatatype(dtype);
        }
        return dtype;
    }
    
    /**
     * Method getTypeByValue. Look up a datatype suitable for representing
     * the given java value object.
     * 
     * @param value a value instance to be represented
     * @return Datatype a datatype whose value space matches the java class
     * of <code>value</code>
     */
    public RDFDatatype getTypeByValue(Object value) {
        return (RDFDatatype) classToDT.get(value.getClass());
    }
    
    /**
     * List all the known datatypes 
     */
    public Iterator listTypes() {
        return uriToDT.values().iterator();
    }
    
    /**
     * Register a new datatype
     */
    public void registerDatatype(RDFDatatype type) {
        uriToDT.put(type.getURI(), type);
        Class jc = type.getJavaClass();
        if (jc != null) {
            classToDT.put(jc, type);
        }
    }

    // Temporary development code
    public static void main(String[] args) {
        for (Iterator iter = theTypeMap.listTypes(); iter.hasNext();) {
            RDFDatatype dt = (RDFDatatype) iter.next();
            System.out.println(" - " + dt);
        }
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
