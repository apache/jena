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

package org.apache.jena.irix;

import org.apache.jena.shared.JenaException;

/**
 * Exception thrown due to IRI problems.
 * <p>
 * Problems can be:
 * <ul>
 * <li>parse errors (the IRI string does not conform to the grammar in
 *     <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>.
 * <li>URI scheme specific errors.
 * <li>Not acceptable for usage intended (in RDF a URI must be absolute and conform to schema-specific rules for an absolute URI).
 * <ul>
 */
public class IRIException extends JenaException {
    public IRIException(String msg) { super(msg); }
    public IRIException(String msg, Throwable ex) { super(msg, ex); }

    // Where in the parser, the exception comes from is not relevant.
    //@Override public Throwable fillInStackTrace() { return this ; }
}
