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

package com.hp.hpl.jena.rdf.model;

/** An RDFReader factory inferface.
 *
 * <p>This factory interface is slightly unusual, in that, as well as
 * creating and returning RDFReader's, it also provides methods
 * for creating a reader, invoking a read method on it and then
 * shuting it down.</p>
 *
 * <p>The factory will create an appropriate reader for the particular
 *   serialization language being read.  Predefined languages include:</p>
 * <ul>
 * <li>RDF/XML - default</li>
 * <li>RDF/XML-ABBREV</li>
 * <li>N-TRIPLE</li>
 * <li>N3</li>
 * </ul>
 *<p>System wide defaults for classes to use as readers for these languages
 *are defined.  These defaults may be overwridden by setting a system property
 *with a name of the form com.hp.hpl.jena.readers.<lang> to the class
 *name.</p>
 * <p><b>NOTE:</b> All settings are global in nature</p>
 */

public interface RDFReaderF {
    
/** return an RDFReader instance for the default serialization language.
 * @return an RDFReader instance for the default serialization language.
 */    
    public RDFReader getReader() ;
    
/** return an RDFReader instance for the specified serialization language.
 * @return the RDFWriter instance
 * @param lang the serialization language - <code>null</code> selects the
 *            default
 
 */    
    public RDFReader getReader(String lang) ;
    
/** set the class name for the RDFReader for a language
 * @param lang the language for which this class should be used
 * @param className the class name for readers for this language
 * @return the old class name for this language
 */    
    public String setReaderClassName(String lang, String className);
    
    /**
     * Resets the values to the initial condition.
     */
    public void resetRDFReaderF();
    
    /**
     * Remove lang from list of readers..
     * Must be one of the classes that was added using setReaderClassName()
     * @param lang The lang to remove.
     * @return the old class name for this language
     * @throws IllegalArgumentException if lang is one of the initial languages
     */
    public String removeReader( String lang ) throws IllegalArgumentException;
 
}
