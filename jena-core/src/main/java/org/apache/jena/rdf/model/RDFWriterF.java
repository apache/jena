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

/** An RDFWriter factory interface.
 * <blockquote><i>Legacy</i></blockquote>
 * <p>
 * Writing RDF is usually done via:
 * <ul>
 * <li>RDFDataMgr</li>
 * <li>RDFWriterr</li>
 * <li>Model.write</li>
 * </ul>
 * All of these will use the newer RIOT writers, not implementations of this interface.
 *
 * <p>The factory will create an appropriate writer for the particular
 *   serialization language being read.  Predefined languages:</p>
 * <ul>
 * <li>RDF/XML - default</li>
 * <li>RDF/XML-ABBREV</li>
 * <li>N-TRIPLE</li>
 * </ul>
 *<p>System wide defaults for classes to use as readers for these languages
 *are defined.
 *</p>
 *<p><b>NOTE:</b> All settings are global in nature</p>
 */

public interface RDFWriterF {

/** return an RDFWriter instance for the default serialization language.
 * @return an RDFWriter instance for the default serialization language.
 */
    public RDFWriterI getWriter();

/** an RDFWriter instance for the specified serialization language.
 * @param lang the serialization language - <code>null</code> selects the
 *             default
 * @return the RDFWriter instance
 */
    public RDFWriterI getWriter(String lang);
}
