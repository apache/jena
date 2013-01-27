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

package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 * Base implementation for all string datatypes derinved from
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

    
// Functionality moved to XSDDatatype but old code left here temporarily until
// we're sure the change is correct.
//    
//    /**
//     * Test whether the given LiteralLabel is a valid instance
//     * of this datatype. This takes into accound typing information
//     * as well as lexical form - for example an xsd:string is
//     * never considered valid as an xsd:integer (even if it is
//     * lexically legal like "1").
//     */
//    public boolean isValidLiteral(LiteralLabel lit) {
//        RDFDatatype dt = lit.getDatatype();
//        if ( dt == null && lit.language().equals("") ) return true;
//        if ( this.equals(dt) ) return true;
//        if (dt instanceof XSDBaseStringType) {
//            return isValid(lit.getLexicalForm());
//        } else {
//            return false;
//        }
//    }
    
    /**
     * Compares two instances of values of the given datatype. 
     * This ignores lang tags and optionally allows plain literals to
     * equate to strings. The latter option is currently set by a static
     * global flag in LiteralLabel.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        // value1 will have been used to dispatch here so we know value1 is an xsdstring or extension
        if ((value2.getDatatype() == null && JenaParameters.enablePlainLiteralSameAsString) ||
             (value2.getDatatype() instanceof XSDBaseStringType)) {
                return value1.getValue().equals(value2.getValue());
        } else {
                return false;
        }
    }

 }
