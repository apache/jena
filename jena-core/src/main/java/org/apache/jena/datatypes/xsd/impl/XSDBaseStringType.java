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

package org.apache.jena.datatypes.xsd.impl;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.impl.LiteralLabel ;

/**
 * Base implementation for all string datatypes derived from
 * xsd:string. The only purpose of this place holder is
 * to support the isValidLiteral tests across string types.
 */
public class XSDBaseStringType extends XSDDatatype {

    /**
     * Constructor.
     * @param typeName the name of the XSD type to be instantiated, this is
     * used to lookup a type definition from the Xerces schema factory.
     */
    public XSDBaseStringType(String typeName) {
        super(typeName);
    }

    /**
     * Constructor.
     * @param typeName the name of the XSD type to be instantiated, this is
     * used to lookup a type definition from the Xerces schema factory.
     * @param javaClass the java class for which this xsd type is to be
     * treated as the cannonical representation
     */
    public XSDBaseStringType(String typeName, Class<?> javaClass) {
        super(typeName, javaClass);
    }

    /**
     * Compares two instances of values of the given datatype.
     * This ignores lang tags and optionally allows plain literals to
     * equate to strings. The latter option is currently set by a static
     * global flag in LiteralLabel.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        // value1 will have been used to dispatch here so we know value1 is an xsd:string or extension
        if ( value2.getDatatype() instanceof XSDBaseStringType)
            return value1.getValue().equals(value2.getValue());
        return false;
    }
 }
