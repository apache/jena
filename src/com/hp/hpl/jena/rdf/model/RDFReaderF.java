/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 * $Id: RDFReaderF.java,v 1.6 2005-02-21 12:14:22 andy_seaborne Exp $
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
 * @author bwm
 * @version $Version$ $Date: 2005-02-21 12:14:22 $
 */

public interface RDFReaderF {
    
/** return an RDFReader instance for the default serialization language.
 * @return an RDFReader instance for the default serialization language.
 */    
    public RDFReader getReader() ;
    
/** return an RDFReader instance for the specified serialization language.
 * @return the RDFWriter instance
 * @param lang the serialization langauge - <code>null</code> selects the
 *            default
 
 */    
    public RDFReader getReader(String lang) ;
    
/** set the class name for the RDFReader for a langauge
 * @param lang the language for which this class should be used
 * @param className the class name for readers for this language
 * @return the old class name for this language
 */    
    public String setReaderClassName(String lang, String className);
 //* @deprecated Replaced by setReader
}

