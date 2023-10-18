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
     * Constructs a new {@code DatatypeFormatException} with the specified
     * illegal lexical form, datatype and cause. The detail message (for later
     * retrieval by the {@link #getMessage()} method) is generated using the given
     * datatype and illegal lexical form.
     *
     * @param  lexicalForm the illegal lexical form discovered. The illegal lexical form
     *         is saved for later retrieval by the {@link #getLexicalForm()} method.
     * @param  dtype the datatype that found the problem. The datatype is saved for
     *         later retrieval by the {@link #getDataType()} method.
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public DatatypeFormatException(String lexicalForm, RDFDatatype dtype, Throwable cause) {
        super(String.format("Lexical form '%s' is not a legal instance of %s.", lexicalForm, dtype), cause);
        this.lexicalForm = lexicalForm;
        this.dataType = dtype;
    }

    /**
     * Constructs a new {@code DatatypeFormatException} with the specified
     * illegal lexical form, datatype and detail message.
     *
     * @param  lexicalForm the illegal lexical form discovered. The illegal lexical form
     *         is saved for later retrieval by the {@link #getLexicalForm()} method.
     * @param  dtype the datatype that found the problem. The datatype is saved for
     *         later retrieval by the {@link #getDataType()} method.
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     */
    public DatatypeFormatException(String lexicalForm, RDFDatatype dtype, String message) {
        super(String.format("Lexical form '%s' is not a legal instance of %s %s", lexicalForm, dtype, message));
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
     * Constructs a new {@code DatatypeFormatException} with the specified
     * detail message and cause.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public DatatypeFormatException(String message, Throwable cause) {
        super(message, cause);
        this.lexicalForm = null;
        this.dataType = null;
    }

    /**
     * Constructs a new @code DatatypeFormatException} with the specified
     * detail message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     *
     * @param  message the detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     */
    public DatatypeFormatException(String message) {
        super(message);
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
