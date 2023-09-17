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
 * RRX (RIOT RDF/XML) is a set of three RDF/XML parsers. They produce the same
 * output, same errors and the same warnings. Each has a {@code LangRDFXML_???} file
 * that is the RIOT reader interface implementation and a {@code RDFXMLParser-???}
 * class which is the parser engine.
 * <ul>
 * <li>SAX based {@linkplain ParserRDFXML_SAX}. This is the default parser for Jena5 onwards for
 * RDF/XML in RIOT, i.e it is the registration for {@linkplain org.apache.jena.riot.Lang#RDFXML} and it is the fastest.
 * <li>A StAX-based parser using {@link XMLStreamReader}.
 * <li>A StAX-based parser using {@link XMLEventReader}.
 * </ul>
 * In addition Jena (in jena-core) has the original ARP parser in package {@code org.apach.jena-rdfxml.rdfxml0} referred to as "ARP0".
 * This was the RDF/XML parser up to Jena 4.6.1. ARP1 uses package jena-iri directly to handle IRIs.
 * <p>
 * "ARP1" is in package {@code org.apach.jena-rdfxml.rdfxml1} is derived from ARP0.
 * It uses the {@link org.apache.jena.irix} abstraction to handle IRIs.
 * <br/>
 * </p>
 * <table border=1 style="border-width: 2px ; border-style: solid; border-collapse: collapse">
 * <thead>
 * <tr>
 *    <th>Parser</th>
 *    <th><code>Lang</code</th>
 *    <th><code>riot --syntax</code></th>
 *    <th>Notes</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 *   <td>RDFXML</td>
 *   <td><code>Lang.RDFXML</code></td>
 *   <td>&nbsp;</td>
 *   <td>System default</td>
 * </tr>
 * <tr>
 *   <td>RRX SAX</td>
 *   <td><code>Lang.RDFXML-SAX</code></td>
 *   <td><code>rrxsax</code></td>
 *   <td></td>
 * </tr>
 * <tr>
 *   <td>RRX StAX stream reader</td>
 *   <td><code>Lang.RDFXML-StAX-SR</code></td>
 *   <td><code>rrxstaxsr</code></td>
 *   <td></td>
 * </tr>
 * <tr>
 *   <td>RRX StAX event reader</td>
 *   <td><code>Lang.RDFXML-StAX-EV</code></td>
 *   <td><code>rrxstaxev</code></td>
 *   <td></td>
 * </tr>
 * <tr>
 *   <td>ARP1</td>
 *   <td><code>None</code></td>
 *   <td><code>arp1</code></td>
 *   <td></td>
 * </tr>
 *  * <tr>
 *   <td>ARP0</td>
 *   <td><code>None</code></td>
 *   <td><code>arp0</code></td>
 *   <td></td>
 * </tr>
 * </tbody>
 * </table>
 *
 */
package org.apache.jena.riot.lang.rdfxml;
