/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

// To do:
//   Better detection of illegal characters in qnames (? and = for example) 

package com.hp.hpl.jena.n3;

//import org.apache.log4j.Logger;
import java.io.OutputStream;
import java.io.Writer;

import com.hp.hpl.jena.rdf.model.*;

/** An N3 writher that indirections onto one of the other writers.
 *
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaWriter.java,v 1.18 2003-06-09 16:03:18 andy_seaborne Exp $
 */



public class N3JenaWriter implements RDFWriter
{
    static public boolean DEBUG = false ;
    static public final String propWriteSimple = "com.hp.hpl.jena.n3.N3JenaWriter.writeSimple" ;
    static public final String propWriterName = "com.hp.hpl.jena.n3.N3JenaWriter.writer" ;

    static public final String n3WriterPrettyPrinter = "N3-PP" ;
    static public final String n3WriterPlain         = "N3-PLAIN" ;
    static public final String n3WriterTriples       = "N3-TRIPLES" ;
    static public final String n3WriterTriplesAlt    = "N3-TRIPLE" ;

    RDFWriter writer = null ;
    
    /** Entry point for N3 writers.  This writer will choose the actual writer
     *  to use by looking at the system property
     *  <code>com.hp.hpl.jena.n3.N3JenaWriter.writer</code> to get the
     *  writer name.
     *  <p>
     *  The following N3 writers are provided:
     *  <ul>
     *  <li>N3-PP: Pretty Printer (the default)</li>
     *  <li>N3-PLAIN: Plain, record/frame-oriented format</li> 
     *  <li>N3-TRIPLES: Triples, with prefixes.</li>
     *  </ul>
     *  </p>
     */  
    
    public N3JenaWriter() { writer = chooseWriter() ; }
    
    RDFWriter chooseWriter()
    {
        // Compatibility with Jena1
        if ( System.getProperty(propWriteSimple, "false").equals("true"))
            return new N3JenaWriterCommon() ;
        
        // Choose the writer
        String writerName = System.getProperty(propWriterName) ;
        if ( writerName == null ||
             writerName.equals("N3") || writerName.equals(n3WriterPrettyPrinter) )
            return new N3JenaWriterPP() ;
        
        if ( writerName.equalsIgnoreCase(n3WriterPlain) )
            return new N3JenaWriterCommon() ;
        
        if ( writerName.equalsIgnoreCase(n3WriterTriples) ||
             writerName.equalsIgnoreCase(n3WriterTriplesAlt) )
            return new N3JenaWriterTriples() ;
            
        // Don't know or default.
        return new N3JenaWriterPP() ;
    }
    
    
    /** Write the model out in N3, encoded in in UTF-8
     * @see #write(Model,Writer,String)
     */

    public void write(Model model, Writer out, String base) throws RDFException
    {
        writer.write(model, out, base) ;
    }

    /** Write the model out in N3.  The writer should be one suitable for UTF-8 which
    * excludes a PrintWriter or a FileWriter which use default character set.
    *
    * Examples:
    * <pre>
    * try {
    *      Writer w =  new BufferedWriter(new OutputStreamWriter(output, "UTF-8")) ;
    *      model.write(w, base) ;
    *      try { w.flush() ; } catch (IOException ioEx) {}
    *  } catch (java.io.UnsupportedEncodingException ex) {} //UTF-8 is required so can't happen
    * </pre>
    * or
    * <pre>
    * try {
    *     OutputStream out = new FileOutputStream(file) ;
    *     Writer w =  new BufferedWriter(new OutputStreamWriter(out, "UTF-8")) ;
    *     model.write(w, base) ;
    * }
    * catch (java.io.UnsupportedEncodingException ex) {}
    * catch (java.io.FileNotFoundException noFileEx) { ... }
    * </pre>
    * @see #write(Model,Writer,String)
    */

    public void write(Model model, OutputStream out, String base) throws RDFException
    {
        writer.write(model, out, base) ;
   }


    /**
     * @see com.hp.hpl.jena.rdf.model.RDFWriter#setProperty(java.lang.String, java.lang.Object)
     */
    public Object setProperty(String propName, Object propValue) throws RDFException
    {
        return writer.setProperty(propName, propValue) ;
    }

    /**
     * @see com.hp.hpl.jena.rdf.model.RDFWriter#setNsPrefix(java.lang.String, java.lang.String)
     */
    public void setNsPrefix(String prefix, String ns)
    {
        writer.setNsPrefix(prefix, ns) ;
        
    }

    /**
     * @see com.hp.hpl.jena.rdf.model.RDFWriter#getPrefixFor(java.lang.String)
     */
    public String getPrefixFor(String ns)
    {
        return writer.getPrefixFor(ns) ;
    }

    /**
     * @see com.hp.hpl.jena.rdf.model.RDFWriter#setErrorHandler(com.hp.hpl.jena.rdf.model.RDFErrorHandler)
     */
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
    {
        return writer.setErrorHandler(errHandler) ;
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
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
 *
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
 */
