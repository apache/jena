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
 * Support for RFC3986 IRIs.
 * <p>
 * The class {@link org.apache.jena.irix.IRIx IRIx} provides the application abstraction layer. There is an
 * {@link org.apache.jena.irix.IRIProvider IRIProvider} that is the implementation of IRIs providing parsing and
 * resolution. A provider is selected at start-up and is not expected to changes
 * while the system is running.
 * <p>
 * {@link org.apache.jena.irix.IRIxResolver IRIxResolver} is the main API. It provides {@link org.apache.jena.irix.IRIx IRIx} resolution with a
 * base URI, and policy choices.
 * <p>
 * The class {@link org.apache.jena.irix.IRIs IRIs} provides functions related to IRIs including
 * {@link org.apache.jena.irix.IRIs#reference IRIs.reference} to check that a string is suitable for use in RDF.
 * Use this function to check a string passed into the application.
 * <p>
 * Standards:
 * <ul>
 * <li>URI syntax -- <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a>
 * <li>HTTP scheme -- <a href="https://tools.ietf.org/html/rfc7230">RFC 7230</a>
 * <li>URN scheme -- <a href="https://tools.ietf.org/html/rfc8141">RFC 8141</a>
 * <li>{@code file} schema -- <a href="https://tools.ietf.org/html/rfc8089">RFC 8089</a>
 * </ul>
 */

@org.osgi.annotation.bundle.Export
package org.apache.jena.irix;

