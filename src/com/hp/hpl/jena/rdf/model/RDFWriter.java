/*
 *  (c) Copyright Hewlett-Packard Company 2000 
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
 * $Id: RDFWriter.java,v 1.3 2003-04-03 12:22:34 jeremy_carroll Exp $
 */

package com.hp.hpl.jena.rdf.model;
import java.io.Writer;
import java.io.OutputStream;
/** RDFWriter is an interface to RDF serializers.
 *
 * <p>An <code>RDFWriter</code> is a class which serializes an RDF model
 * to some RDF serializaion language.  RDF/XML, n-triple and n3 are
 * examples of serialization languages.</p>
 * @author bwm
 * @version $Revision: 1.3 $
 */
public interface RDFWriter {
/** Namespace prefixes can be set using a system property of the form
 * <code>NSPREFIXPROPBASE + uri</code>.  
 * Such prefixes override the built in
 * ones and those set using 
 * {@link #setNsPrefix(java.lang.String, java.lang.String)}.
 */
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
	 * @throws RDFException Generic RDF exception.
	 */    
	    public void write(Model model, Writer out, String base) 
	        throws RDFException;
    
    
/** Serialize Model <code>model</code> to OutputStream <code>out</out>.
 * The implementation chooses  the character encoding, utf-8 is preferred.
 * 
 * 
 * @param out The OutputStream to which the serialization should be sent.
 * @param model The model to be written.
 * @param base the base URI for relative URI calculations.  <code>
   null</code> means use only absolute URI's.
 * @throws RDFException Generic RDF exception.
 */    
    public void write(Model model, OutputStream out, String base) 
        throws RDFException;
    
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
 * <dd>See {@link com.hp.hpl.jena.xmloutput.RDFXMLWriterI#setProperty(String,
 * Object)}
 * </dl>
 * @return the old value for this property, or <code>null</code>
 * if no value was set.
 * @param propName The name of the property.
 * @param propValue The new value of the property
 * @throws RDFException Throws a generic RDF exception
 */ 
    public Object setProperty(String propName, Object propValue) 
      throws RDFException;
/** Set the the namespace prefix to be used for a particular namespace.
 * @param prefix the prefix to be used
 * @param ns the namespace
 */    
    public void   setNsPrefix(String prefix, String ns);
/** Set an error handler.
 * @param errHandler The new error handler to be used.
 * @return the old error handler
 */    
    public RDFErrorHandler  setErrorHandler(RDFErrorHandler errHandler);
}
