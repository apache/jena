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

package org.apache.jena.datatypes;

import org.apache.jena.shared.* ;

/**
 * Exception thrown when a lexical form does not match the stated
 * datatype.
 */
public class DatatypeFormatException extends JenaException 
{
    /**
     * Preferred constructor.
     * @param lexicalForm the illegal string discovered
     * @param dtype the datatype that found the problem
     * @param msg additional context for the error
     */
    public DatatypeFormatException(String lexicalForm, RDFDatatype dtype, String msg) {
        super("Lexical form '" + lexicalForm +
               "' is not a legal instance of " + dtype + " " + msg);
        this.lexicalForm = lexicalForm;
        this.dataType = dtype;
    }

    private final String lexicalForm;
    private final RDFDatatype dataType;

    /**
     * Creates a new instance of <code>DatatypeFormatException</code> 
     * without detail message.
     */
    public DatatypeFormatException() {
        this.lexicalForm = null;
        this.dataType = null;
    }

    /**
     * Constructs an instance of <code>DatatypeFormatException</code> 
     * with the specified detail message.
     * @param msg the detail message.
     */
    public DatatypeFormatException(String msg) {
        super(msg);
        this.lexicalForm = null;
        this.dataType = null;
    }

    /**
     * The invalid lexical form that caused this exception.
     *
     * @return the lexical form that caused the exception. Maybe null depending on how the exception was constructed.
     */
    public String getLexicalForm() {
        return this.lexicalForm;
    }

    /**
     * The datatype that has an invalid lexical form.
     *
     * @return the datatype that this exception is related to. Maybe null depending on how the exception was constructed.
     */
    public RDFDatatype getDataType() {
        return this.dataType;
    }
}
