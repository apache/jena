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

import org.apache.xerces.impl.dv.XSSimpleType;

import com.hp.hpl.jena.datatypes.xsd.*;

/**
 * Datatype template that adapts any response back from Xerces type parsing
 * to an appropriate java representation. This is primarily used in creating
 * user defined types - the built in types have a fixed mapping.
 * <p>
 * This class is probably now redundant in that XSDDatatype can support
 * run time conversion of union results. Left in for now during restructuring and
 * in case any existing user code expects this type - very unlikely.
 * </p>
 */
public class XSDGenericType extends XSDDatatype {

    /**
     * Hidden constructor used when loading in external user defined XSD types
     * @param xstype the XSSimpleType definition to be wrapped
     * @param namespace the namespace for the type (used because the grammar loading doesn't seem to keep that)
     */
    public XSDGenericType(XSSimpleType xstype, String namespace) {
        super(xstype, namespace);
    }


//  No longer need to perform any special case processing of union types since we
//  now do runtime type coercion - is that right?

//    /**
//     * Parse a lexical form of this datatype to a value
//     * @throws DatatypeFormatException if the lexical form is not legal
//     */
//    public Object parse(String lexicalForm) throws DatatypeFormatException {
//        try {
//            ValidationContext context = new ValidationState();
//            ValidatedInfo resultInfo = new ValidatedInfo();
//            Object result = typeDeclaration.validate(lexicalForm, context, resultInfo);
//            return convertValidatedDataValue(resultInfo);
//        } catch (InvalidDatatypeValueException e) { 
//            throw new DatatypeFormatException(lexicalForm, this, "during parse -" + e);
//        } 
//    }
    
 
}
