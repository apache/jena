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

/**
 * Implementation of RFC 3986 (URI), RFC 3987 (IRI). As is common, these are referred to
 * as "3986" regardless just as {@code java.net.URI} covers IRIs. {@code java.net.URI}
 * parses and allocates and follows RFC 2396 with modifications (several of which are in
 * RFC 3986).
 *
 * This provides a fast checking operation which does not copy the various parts of the
 * IRI and which creates a single object. The cost of extracting and allocating strings
 * happen when the getter for the component is called.
 *
 * Implements the algorithms specified in RFC 3986 operations for:
 * <ul>
 * <li>Checking a string matches the IRI grammar.
 * <li>Extracting components of an IRI
 * <li>Normalizing an IRI
 * <li>Resolving an IRI against a base IRI.
 * <li>Relativizing an IRI for a given base IRI.
 * <li>Building an IRI from components.
 * </ul>
 *
 * Additions:
 * <ul>
 * <li>Scheme specific rules for Linked Data HTTP and URNs.
 * </ul>
 * HTTP IRIs forbid the "user@" part which is strongly discouraged in IRIs.<br/>
 * Some additional checks for <a href="https://tools.ietf.org/html/rfc8141">RFC 8141</a>
 * for URNs are included such as being of the form {@code urn:NID:NSS}.
 *
 * Restrictions and limitations:
 * <ul>
 * <li>Only java characters supported (i.e. UTF16 16 bit characters)
 * <li>No normal form C checking when checking
 * </ul>
 *
 * RFCs:
 * <p>
 * <ul>
 * <li><a href="https://tools.ietf.org/html/rfc3986">RFC 3986 "Uniform Resource Identifier (URI): Generic Syntax"</a></li>
 * <li><a href="https://tools.ietf.org/html/rfc3987">RFC 3987 "Internationalized Resource Identifiers (IRIs)"</a></li>
 * <li><a href="https://tools.ietf.org/html/rfc8141">RFC 8141 Uniform Resource Names (URNs) [scheme specific details]</a></li>
 * <li><a href="https://tools.ietf.org/html/rfc7230">RFC 7230 HTTP 1.1 [scheme specific details]</a>
 * </ul>
 *
 * Obsoleted RFCs:
 * <ul>
 * <li><a href="https://tools.ietf.org/html/rfc2396">RFC2396 Uniform Resource Identifiers (URI): Generic Syntax</a></li>
 * <li><a href="https://tools.ietf.org/html/rfc2141">RFC 2141 URN Syntax</a></li>
 * </ul>
 *
 * <h3>RFC 3986 Grammar</h3>
 * <a href="https://tools.ietf.org/html/rfc3986#page-49">Appendix A.  Collected ABNF for URI</a>
 * <h3>RFC 3987 modifications Grammar</h3>
 * <a href="https://tools.ietf.org/html/rfc3987#page-7">2.2.  ABNF for IRI References and IRIs</a>
 *
 */

package org.apache.jena.rfc3986;
