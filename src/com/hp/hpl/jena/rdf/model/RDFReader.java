/*
 *  (c) Copyright 2000  Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * $Id: RDFReader.java,v 1.8 2003-08-27 13:05:52 andy_seaborne Exp $
 */

package com.hp.hpl.jena.rdf.model;

import java.io.Reader;
import java.io.InputStream;

/** An <code>RDFReader</code> reads a serialized represenation of RDF,
 * e.g. RDF/XML, n-triple or n3 and adds the statements to a model.
 *
 * @author bwm
 * @version $Revision: 1.8 $
 */
public interface RDFReader {

	/** 
	 * It is usually a mistake to use this method.
	 * Read serialized RDF from a <code>Reader</code> and add the statements to a
	 * <code>Model</code>. It is generally better to use an InputStream if
	 * possible. {@link #read(Model,InputStream,String)}, otherwise there is a
	 * danger of a mismatch between the character encoding of say the FileReader
	 * and the character encoding of the data in the file.
	 * @param model The model to which statements are added.
	 * @param r the reader from which to read
	 * @param base The base to use when converting relative to absolute URI's.
	 * The base URI may be null if there are no relative URIs to convert.
	 * A base URI of "" may permit relative URIs to be used in the
	 * model unconverted.
	 */
	public void read(Model model, Reader r, String base) ;

	/** Read serialized RDF from an <code>InputStream</code> and add the statements
	 * to a <code>Model</code>.
	 * @param model The model to which statements are added.
	 * @param r The InputStream from which to read
	 * @param base The base to use when converting relative to absolute URI's.
	 * The base URI may be null if there are no relative URIs to convert.
	 * A base URI of "" may permit relative URIs to be used in the
	 * model unconverted.
	 */
	public void read(Model model, InputStream r, String base);

	/** Read serialized RDF from a url and add the statements to a model.
	 * @param model the model to which statements should be added
	 * @param url the url, as a string, from which the serialized RDF
	 * should be read.
	 */
	public void read(Model model, String url) ;

	/** Set the value of a reader property.
	 *
	 * <p>The behaviour of a reader may be influenced by setting property values.
	 * The properties and there effects may depend on the individual reader
	 * implementation.</p>
	 * <p>An RDFReader's behaviour can be influenced by defining property values
	 * interpreted by that particular reader class.  The values for such
	 * properties can be changed by calling this method.  </p>
	 *
	 * <p>No standard properties are defined.  For the properties recognised
	 * by any particular reader implementation, see the the documentation for
	 * that implementation. </p>
     * <p> The built-in RDFReaders have properties as defined by:
 * <dl>
 * <dt>N3</dt><dt>N-TRIPLE</dt>
 * <dd>No properties.</dd>
 * <dt>RDF/XML</dt><dt>RDF/XML-ABBREV</dt>
 * <dd>See {@link com.hp.hpl.jena.rdf.arp.JenaReader#setProperty(String,Object)}
 * </dl>
	 * @param propName the name of the property
	 * @param propValue the value of the property
	 * @return the previous value of the property, or <code>null</code>
	 * if there wasn't one
	 */
	public Object setProperty(String propName, Object propValue);
	/** Set an error handler for the reader
	 * @param errHandler the new error handler
	 * @return the previous error handler
	 */
	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler);
    
}
