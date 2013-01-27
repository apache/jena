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
import java.io.Writer;
import java.io.OutputStream;
/** RDFWriter is an interface to RDF serializers.
 *
 * <p>An <code>RDFWriter</code> is a class which serializes an RDF model
 * to some RDF serializaion language.  RDF/XML, n-triple and n3 are
 * examples of serialization languages.</p>
 */
public interface RDFWriter {

	public static final String NSPREFIXPROPBASE
	  = "com.hp.hpl.jena.nsprefix.";
	/** Caution: Serialize Model <code>model</code> to Writer <code>out</code>.
	 * It is often better to use an OutputStream and permit Jena
	 * to choose the character encoding. The charset restrictions
	 * on the Writer are defined by the different implementations
	 * of this interface. Typically using an OutputStreamWriter (e.g.
	 * a FileWriter) at least permits the implementation to
	 * examine the encoding. With an arbitrary Writer  implementations
	 * assume  a utf-8 encoding.
	 * 
	 * @param out The Writer to which the serialization should
	 * be sent.
	 * @param model The model to be written.
	 * @param base the base URI for relative URI calculations.  <code>
	   null</code> means use only absolute URI's.
	 */    
	    public void write(Model model, Writer out, String base);
    
    
/** Serialize Model <code>model</code> to OutputStream <code>out</out>.
 * The implementation chooses  the character encoding, utf-8 is preferred.
 * 
 * 
 * @param out The OutputStream to which the serialization should be sent.
 * @param model The model to be written.
 * @param base the base URI for relative URI calculations.  <code>
   null</code> means use only absolute URI's. This is used for relative
   URIs that would be resolved against the document retrieval URL.
   Particular writers may include this value in the output. 
 */    
    public void write(Model model, OutputStream out, String base);
    
/** Set a property to control the behaviour of this writer.
 *
 * <p>An RDFWriter's behaviour can be influenced by defining property values
 * interpreted by that particular writer class.  The values for such
 * properties can be changed by calling this method.  </p>
 *
 * <p>No standard properties are defined.  For the properties recognised
 * by any particular writer implementation, see the the documentation for
 * that implementation.  </p>
 * <p>
 * The built-in RDFWriters have properties as defined by:
 * <dl>
 * <dt>N3</dt><dt>N-TRIPLE</dt>
 * <dd>No properties.</dd>
 * <dt>RDF/XML</dt><dt>RDF/XML-ABBREV</dt>
 * </dl>
 * @return the old value for this property, or <code>null</code>
 * if no value was set.
 * @param propName The name of the property.
 * @param propValue The new value of the property
 */ 
    public Object setProperty(String propName, Object propValue);

/** Set an error handler.
 * @param errHandler The new error handler to be used.
 * @return the old error handler
 */    
    public RDFErrorHandler  setErrorHandler(RDFErrorHandler errHandler);
}
