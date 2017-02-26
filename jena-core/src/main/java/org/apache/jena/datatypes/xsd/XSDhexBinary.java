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

package org.apache.jena.datatypes.xsd;

import javax.xml.bind.DatatypeConverter;

import org.apache.jena.datatypes.DatatypeFormatException ;

/**
 * Implement hexbinary type. Most of the work is done in the superclass.
 * This only needs to implement the unparsing.
 */
public class XSDhexBinary extends XSDbinary {
    
    public XSDhexBinary(String typeName) {
        super(typeName, false);
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        if (value instanceof byte[]) {
            return DatatypeConverter.printHexBinary((byte[])value);
        } else {
            throw new DatatypeFormatException("hexBinary asked to encode a non-byte arrary");
        }
    }
}
