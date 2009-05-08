/******************************************************************
 * File:        TypeMapper.java
 * Created by:  Dave Reynolds
 * Created on:  07-Dec-02
 * 
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TypeMapper.java,v 1.13 2009-05-08 14:09:36 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 * The TypeMapper provides a global registry of known datatypes.
 * The datatypes can be retrieved by their URI or from the java class
 * that is used to represent them.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.13 $ on $Date: 2009-05-08 14:09:36 $
 */

// Added extended set of class mappings and getTypeByClass
// as suggested by Thorsten Moeller. der 8/5/09

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
    
    public TypeMapper() {
        // add primitive types
        classToDT.put(float.class, classToDT.get(Float.class));
        classToDT.put(double.class, classToDT.get(Double.class));
        classToDT.put(int.class, classToDT.get(Integer.class));
        classToDT.put(long.class, classToDT.get(Long.class));
        classToDT.put(short.class, classToDT.get(Short.class));
        classToDT.put(byte.class, classToDT.get(Byte.class));
        classToDT.put(boolean.class, classToDT.get(Boolean.class));

        // add missing character types
        classToDT.put(char.class, classToDT.get(String.class));
        classToDT.put(Character.class, classToDT.get(String.class));

        // add mapping for URL class
        classToDT.put(URL.class, classToDT.get(URI.class));        
    }
    
//=======================================================================
// Variables

    /** Map from uri to datatype */
    private HashMap<String, RDFDatatype> uriToDT = new HashMap<String, RDFDatatype>();
    
    /** Map from java class to datatype */
    private HashMap<Class<?>, RDFDatatype> classToDT = new HashMap<Class<?>, RDFDatatype>();
    
//=======================================================================
// Methods


    /**
     * Version of getTypeByName which will treat unknown URIs as typed
     * literals but with just the default implementation
     * 
     * @param uri the URI of the desired datatype
     * @return Datatype the datatype definition
     * registered at uri, if there is no such registered type it
     * returns a new instance of the default datatype implementation, if the
     * uri is null it returns null (indicating a plain RDF literal).
     */
    public RDFDatatype getSafeTypeByName(String uri) {
        RDFDatatype dtype = uriToDT.get(uri);
        if (dtype == null) {
            if (uri == null) {
                // Plain literal
                return null;
            } else {
                // Uknown datatype
                if (JenaParameters.enableSilentAcceptanceOfUnknownDatatypes) {
                    dtype = new BaseDatatype(uri);
                    registerDatatype(dtype);
                } else {
                    throw new DatatypeFormatException(
                        "Attempted to created typed literal using an unknown datatype - " + uri);
                }
            }
        }
        return dtype;
    }
    
    /**
     * Lookup a known datatype. An unkown datatype or a datatype with uri null
     * will return null will mean that the value will be treated as a old-style plain literal.
     * 
     * @param uri the URI of the desired datatype
     * @return Datatype the datatype definition of null if not known.
     */
    public RDFDatatype getTypeByName(String uri) {
        return uriToDT.get(uri);
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
        return classToDT.get(value.getClass());
    }
    
    /**
     * List all the known datatypes 
     */
    public Iterator<RDFDatatype> listTypes() {
        return uriToDT.values().iterator();
    }
    
    /**
     * Look up a datatype suitable for representing instances of the
     * given Java class.
     *
     * @param class a Java class to be represented
     * @return a datatype whose value space matches the given java class
     */
    public RDFDatatype getTypeByClass(Class<?> clazz) {
        return classToDT.get(clazz);
    }

    /**
     * Register a new datatype
     */
    public void registerDatatype(RDFDatatype type) {
        uriToDT.put(type.getURI(), type);
        Class<?> jc = type.getJavaClass();
        if (jc != null) {
            classToDT.put(jc, type);
        }
    }

    // Temporary development code
    public static void main(String[] args) {
        for (Iterator<RDFDatatype> iter = theTypeMap.listTypes(); iter.hasNext();) {
            RDFDatatype dt = iter.next();
            System.out.println(" - " + dt);
        }
    }
}

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
