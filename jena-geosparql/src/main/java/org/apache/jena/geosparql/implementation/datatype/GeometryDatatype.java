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
package org.apache.jena.geosparql.implementation.datatype;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex.GeometryIndex;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;

/**
 *
 *
 */
public abstract class GeometryDatatype extends BaseDatatype {

    public GeometryDatatype(String uri) {
        super(uri);
    }

    public abstract GeometryWrapper read(String geometryLiteral);

    /**
     * This method Parses the Geometry Literal to the JTS Geometry
     *
     * @param lexicalForm - the Geometry Literal to be parsed
     * @return geometry - if the Geometry Literal is valid.
     * <br> empty geometry - if the Geometry Literal is empty.
     * <br> null - if the Geometry Literal is invalid.
     */
    @Override
    public final GeometryWrapper parse(String lexicalForm) throws DatatypeFormatException {
        return parse(lexicalForm, GeometryIndex.PRIMARY);
    }

    public final GeometryWrapper parse(String lexicalForm, GeometryIndex targetIndex) throws DatatypeFormatException {
        //Check the Geometry Literal Index to see if been previously read and cached.
        //DatatypeReader interface used to instruct index on how to obtain the GeometryWrapper.
        try {
            return GeometryLiteralIndex.retrieve(lexicalForm, this, targetIndex);
        } catch (IllegalArgumentException ex) {
            throw new DatatypeFormatException(ex.getMessage() + " - Illegal Geometry Literal: " + lexicalForm);
        }
    }

    private static final TypeMapper TYPE_MAPPER = TypeMapper.getInstance();
    private static boolean isDatatypesRegistered = false;

    public static final void registerDatatypes() {
        if (!isDatatypesRegistered) {
            TYPE_MAPPER.registerDatatype(WKTDatatype.INSTANCE);
            TYPE_MAPPER.registerDatatype(GMLDatatype.INSTANCE);
            isDatatypesRegistered = true;
        }
    }

    public static final GeometryDatatype get(RDFDatatype rdfDatatype) throws DatatypeFormatException {
        if (rdfDatatype instanceof GeometryDatatype) {
            return (GeometryDatatype) rdfDatatype;
        } else {
            throw new DatatypeFormatException("Unrecognised Geometry Datatype: " + rdfDatatype.getURI() + " Ensure that Datatype is extending GeometryDatatype.");
        }
    }

    public static final GeometryDatatype get(String datatypeURI) {
        checkURI(datatypeURI);
        RDFDatatype rdfDatatype = TYPE_MAPPER.getTypeByName(datatypeURI);

        return GeometryDatatype.get(rdfDatatype);
    }

    public static final boolean checkURI(String datatypeURI) throws DatatypeFormatException {
        registerDatatypes();
        RDFDatatype rdfDatatype = TYPE_MAPPER.getTypeByName(datatypeURI);
        if (rdfDatatype != null) {
            return rdfDatatype instanceof GeometryDatatype;
        } else {
            throw new DatatypeFormatException("Datatype not found: " + datatypeURI + " Ensure that GeoSPARQL is enabled and Datatype has been registered.");
        }
    }

    public static final boolean check(RDFDatatype rdfDatatype) {
        //Ensure that the registered datatypes from the type mapper are used.
        return checkURI(rdfDatatype.getURI());
    }

}
