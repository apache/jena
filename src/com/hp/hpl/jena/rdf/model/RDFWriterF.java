/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
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
 * $Id: RDFWriterF.java,v 1.1.1.1 2002-12-19 19:17:54 bwm Exp $
 */

package com.hp.hpl.jena.rdf.model;

/** An RDFReader factory inferface.
 *
 * <p>The factory will create an appropriate writer for the particular
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
 * @author bwm
 * @version $Version$ $Date: 2002-12-19 19:17:54 $
 */

public interface RDFWriterF {
    
/** return an RDFWriter instance for the default serialization language.
 * @return an RDFWriter instance for the default serialization language.
 */    
    public RDFWriter getWriter() throws RDFException;
    
/** an RDFWriter instance for the specified serialization language.
 * @param lang the serialization langauge - <code>null</code> selects the
 *             default
 * @return the RDFWriter instance
 */    
    public RDFWriter getWriter(String lang) throws RDFException; 
    
/** set the class name for the RDFWriter for a langauge
 * @param lang the language for which this class should be used
 * @param className the class name for writers for this language
 * @return the old class name for this language
 */    
    public String setWriterClassName(String lang, String className);
    
// * @deprecated Replaced by setWriter.

}

