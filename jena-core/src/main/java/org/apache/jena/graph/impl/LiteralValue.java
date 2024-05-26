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

package org.apache.jena.graph.impl;

import java.util.Objects;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;

/**
 * Code for literal values.
 * <p>
 * Up to Jena4, literal values were in LiteralLabel
 */
class LiteralValue {

    /**
     * Given a lexical form, and a datatype, return the represented value.
     * <p>
     * If the lexical form is ill-formed (i.e. it is not correct for the
     * datatype (example: {@code "tuesday"^^xsd:integer}) return null.
     */
    static Object calculateValue(String lexicalForm, RDFDatatype dtype) {
        Objects.requireNonNull(lexicalForm, "Lexical form");
        Objects.requireNonNull(dtype, "Datatype");
        try {
            Object value = dtype.parse(lexicalForm);
            return value;
        } catch (DatatypeFormatException ex) {
            return null;
        }
    }

    /**
     * Return the datatype for the value, according to the Jena Model API.
     * <p>
     * For example, {@code Integer} is {@code xsd:int}.
     * Return null for no mapping.
     * <p>
     * See {@link #datatypeForValueAny} to always return a
     * <p>
     * For
     */
    static RDFDatatype datatypeForValue(Object value) {
        return TypeMapper.getInstance().getTypeByValue( value );
    }

    /**
     * Return a datatype for the value, according to the Jena Model API.
     * <p>
     * If the datatype is not one of the standard set, create and register
     * an {@link AdhocDatatype}.
     * <p>
     * This function does not return null;
     */
    static RDFDatatype datatypeForValueAny(Object value) {
        RDFDatatype dt = datatypeForValue(value);
        if (dt != null)
            return dt;
        dt = inventDatatypeForValue(value);
        return dt;
    }

    /**
     * Create and register an {@link AdhocDatatype} for a value.
     * <p>
     * This is not a good idea.
     * Consider supplying the datatype explicitly when creating a {@link Node_Literal}.
     */
    private static RDFDatatype inventDatatypeForValue(Object value) {
        Class<?> c = value.getClass();
        Log.warn( LiteralValue.class, "Inventing a datatype for " + c );
        RDFDatatype dt = new AdhocDatatype( c );
        TypeMapper.getInstance().registerDatatype( dt );
        return dt;
    }
}
