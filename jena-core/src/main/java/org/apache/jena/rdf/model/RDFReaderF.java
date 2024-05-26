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

package org.apache.jena.rdf.model;

/** An RDFReader factory interface.
 * <blockquote><i>Legacy</i></blockquote>
 * Reading RDF is usually done via:
 * <ul>
 * <li>RDFDataMgr</li>
 * <li>RDFParser</li>
 * <li>Model.read</li>
 * </ul>
 * All of these will use the newer RIOT parsers, not implementations of this interface.
 *
 * <p>The factory will create an appropriate reader for the particular
 *   serialization language being read.  Predefined languages include:</p>
 * <ul>
 * <li>RDF/XML - default</li>
 * <li>RDF/XML-ABBREV</li>
 * <li>N-TRIPLE</li>
 * <li>N3</li>
 * </ul>
 */

public interface RDFReaderF {

    /** return an RDFReader instance for the specified serialization language.
     * @return the RDFWriter instance
     * @param lang the serialization language - <code>null</code> selects the default (RDF/XML).
     */
    public RDFReaderI getReader(String lang) ;
}
