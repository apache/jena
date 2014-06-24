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

    public static void setInstance(TypeMapper typeMapper) {
        theTypeMap = typeMapper ;
    }

    /**
     * The single global instance of the TypeMapper
     */
    private static TypeMapper theTypeMap;

    /**
     * Static initializer. Adds builtin datatypes to the mapper.
     */
    static { reset() ; }
    public static void reset() {
        theTypeMap = new TypeMapper();
        theTypeMap.registerDatatype(XMLLiteralType.theXMLLiteralType);
        XSDDatatype.loadXSDSimpleTypes(theTypeMap);

        // add primitive types
        theTypeMap.classToDT.put(float.class, theTypeMap.classToDT.get(Float.class));
        theTypeMap.classToDT.put(double.class, theTypeMap.classToDT.get(Double.class));
        theTypeMap.classToDT.put(int.class, theTypeMap.classToDT.get(Integer.class));
        theTypeMap.classToDT.put(long.class, theTypeMap.classToDT.get(Long.class));
        theTypeMap.classToDT.put(short.class, theTypeMap.classToDT.get(Short.class));
        theTypeMap.classToDT.put(byte.class, theTypeMap.classToDT.get(Byte.class));
        theTypeMap.classToDT.put(boolean.class, theTypeMap.classToDT.get(Boolean.class));

        // add missing character types
        theTypeMap.classToDT.put(char.class, theTypeMap.classToDT.get(String.class));
        theTypeMap.classToDT.put(Character.class, theTypeMap.classToDT.get(String.class));

        // add mapping for URL class
        theTypeMap.classToDT.put(URL.class, theTypeMap.classToDT.get(URI.class));
    }

    public TypeMapper() {
   	 super();
    }

//=======================================================================
// Variables

    /** Map from uri to datatype */
    private final HashMap<String, RDFDatatype> uriToDT = new HashMap<>();

    /** Map from java class to datatype */
    private final HashMap<Class<?>, RDFDatatype> classToDT = new HashMap<>();

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
    public RDFDatatype getSafeTypeByName(final String uri) {
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
    public RDFDatatype getTypeByName(final String uri) {
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
    public RDFDatatype getTypeByValue(final Object value) {
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
     * @param clazz a Java class to be represented
     * @return a datatype whose value space matches the given java class
     */
    public RDFDatatype getTypeByClass(final Class<?> clazz) {
        return classToDT.get(clazz);
    }

    /**
     * Register a new datatype
     */
    public void registerDatatype(final RDFDatatype type) {
        uriToDT.put(type.getURI(), type);
        final Class<?> jc = type.getJavaClass();
        if (jc != null) {
            classToDT.put(jc, type);
        }
    }

    // Temporary development code
    public static void main(final String[] args) {
        for (final Iterator<RDFDatatype> iter = theTypeMap.listTypes(); iter.hasNext();) {
            final RDFDatatype dt = iter.next();
            System.out.println(" - " + dt);
        }
    }
}
