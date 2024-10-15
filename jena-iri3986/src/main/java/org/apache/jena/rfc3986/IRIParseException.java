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

package org.apache.jena.rfc3986;

/**
 * Runtime exception thrown when finding errors in an RFC 3986/7 IRI.
 * @see RFC3986
 * @see IRI3986
 */
public class IRIParseException extends RuntimeException {
    // This is a signalling (alternative return) exception,
    // not a programming error.

    // Must gave a message.
    public IRIParseException(CharSequence entity, String message) { this(message); }

    // Must gave a message.
    public IRIParseException(String message) {super(message); }

    // Where in a parser, the exception comes from is not relevant.
    @Override public Throwable fillInStackTrace() { return this ; }
}
