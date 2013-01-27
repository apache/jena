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

import com.hp.hpl.jena.shared.*;

/**
 * Exception thrown when a lexical form does not match the stated
 * datatype.
 */
public class DatatypeFormatException extends JenaException 
{
    
    // TODO Could consider storing the lexical form and datatype in locals
    // with accessors.
    
    /**
     * Preferred constructor.
     * @param lexicalForm the illegal string discovered
     * @param dtype the datatype that found the problem
     * @param msg additional context for the error
     */
    public DatatypeFormatException(String lexicalForm, RDFDatatype dtype, String msg) {
        super("Lexical form '" + lexicalForm +
               "' is not a legal instance of " + dtype + " " + msg);
    }
                  
    /**
     * Creates a new instance of <code>DatatypeFormatException</code> 
     * without detail message.
     */
    public DatatypeFormatException() {
    }
    
    /**
     * Constructs an instance of <code>DatatypeFormatException</code> 
     * with the specified detail message.
     * @param msg the detail message.
     */
    public DatatypeFormatException(String msg) {
        super(msg);
    }

}
