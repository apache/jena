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

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;

/** A datatype that does not use the Xerces machinary for isEqual, yet is still an XSDDatatype.
 * Assumes no derived XSD datatypes.
 */

public class XSDPlainType extends XSDDatatype
{
	/**
    * New instance creation delegating to {@link XSDDatatype#XSDDatatype(String)}.
    */
   public XSDPlainType(String typeName)
    {
        super(typeName) ;
    }

    /**
     * New instance creation delegating to {@link XSDDatatype#XSDDatatype(String, Class)}.
     */
    public XSDPlainType(String typeName, Class<?> clazz)
    {
        super(typeName, clazz) ;
    }

    /**
     * Compares two instances of values of the given datatype.
     * This default requires value and datatype equality.
     * This is the same as BaseDatatype
     */
    @Override
    public boolean isEqual(LiteralLabel litLabel1, LiteralLabel litLabel2) {
        // This is the function from BaseDatatype!
        return isEqualPlain(litLabel1, litLabel2) ;
    }
}
