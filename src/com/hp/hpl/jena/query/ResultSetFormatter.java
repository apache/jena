/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.query;

import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.resultset.*;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;


import java.io.* ;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;

/** ResultSetFormatter - Convenience ways to call the various output formatters.
 *  in various formats. 
 * 
 * @author   Andy Seaborne
 * @version  $Id: ResultSetFormatter.java,v 1.42 2007/01/02 11:20:16 andy_seaborne Exp $
 */

public class ResultSetFormatter
{
    /**
     * Output a result set in a text format.
     * @param qresults   result set
     */
    public static void out(ResultSet qresults)
    { out(System.out, qresults) ; }

    /**
     * Output a result set in a text format.
     * @param out        OutputStream
     * @param qresults   result set
     */
    public static void out(OutputStream out, ResultSet qresults)
    { out(out, qresults, (PrefixMapping)null) ; }
    
    /**
     * Output a result set in a text format.
     * @param qresults   result set
     * @param query      May be used to abbreviate URIs 
     */
    public static void out(ResultSet qresults, Query query)
    { out(System.out, qresults, query.getPrefixMapping()) ; }
    
    /**
     * Output a result set in a text format.
     * @param out        OutputStream
     * @param qresults   result set
     * @param query      May be used to abbreviate URIs 
     */
    public static void out(OutputStream out, ResultSet qresults, Query query)
    { out(out, qresults, query.getPrefixMapping()) ; }
    
    /**
     * Output a result set in a text format.
     * @param qresults  result set
     * @param pmap      Prefix mapping for abbreviating URIs.
     */
    public static void out(ResultSet qresults, PrefixMapping pmap)
    { out(System.out, qresults, pmap) ; }

    /**
     * Output a result set in a text format.
     * @param out       OutputStream
     * @param qresults  result set
     * @param pmap      Prefix mapping for abbreviating URIs.
     */
    public static void out(OutputStream out, ResultSet qresults, PrefixMapping pmap)
    {
        TextOutput tFmt = new TextOutput(pmap) ;
        tFmt.format(out, qresults) ;
    }

    /**
     * Output an ASK answer
     * @param answer    The boolean answer
     */
    public static void out(boolean answer)
    { out(System.out, answer) ; }

    /**
     * Output an ASK answer
     * @param out       OutputStream
     * @param answer    The boolean answer
     */
    public static void out(OutputStream out, boolean answer)
    {
        TextOutput tFmt = new TextOutput((SerializationContext)null) ;
        tFmt.format(out, answer) ;
    }
    
    // ----------------------------------------------------------------
    // Do nothing formatting
    
    /** This operation faithfully walks the results but does nothing with them.
     *  @return The count of the number of solutions. 
     */

    public static int consume(ResultSet resultSet)
    {
        int count = 0 ;
        for ( ; resultSet.hasNext() ; )
        {
            //QuerySolution result = 
                resultSet.nextSolution() ;
            count++ ;
        }
        return count ;
    }
    

    /**
     * Turn the result set into a java.util.List
     * @param resultSet   The result set
     * @return            List of QuerySolutions
     */
    static public List toList(ResultSet resultSet)
    {
        List list = new ArrayList() ;
        for ( ; resultSet.hasNext() ; )
        {
            QuerySolution result = 
                resultSet.nextSolution() ;
            list.add(result) ;
        }
        return list ;
    }
    
    // ----------------------------------------------------------------
    // As RDF
    
    /** Encode the result set as RDF.
     * @param  resultSet
     * @return Model       Model contains the results
     */

    static public Model toModel(ResultSet resultSet)
    {
        RDFOutput rOut = new RDFOutput() ;
        return rOut.toModel(resultSet) ;
    }

    /**
     * Encode a boolean result set as RDF. 
     * @param booleanResult
     * @return Model       Model contains the results
     */
    public static Model toModel(boolean booleanResult)
    {
        RDFOutput rOut = new RDFOutput() ;
        return rOut.toModel(booleanResult) ;
    }

    
    /** Encode the result set as RDF in the model provided.
     *  
     * @param  model     The place where to put the RDF.
     * @param  resultSet
     * @return Resource  The resource for the result set.
     */ 

    static public Resource asRDF(Model model, ResultSet resultSet)
    {
        RDFOutput rOut = new RDFOutput() ;
        return rOut.asRDF(model, resultSet) ;
    }
    
    /** Encode the boolean as RDF in the model provided.
     *  
     * @param  model     The place where to put the RDF.
     * @param  booleanResult
     * @return Resource  The resource for the result set.
     */ 

    static public Resource asRDF(Model model, boolean booleanResult)
    {
        RDFOutput rOut = new RDFOutput() ;
        return rOut.asRDF(model, booleanResult) ;
    }
    
    /** Output a ResultSetin some format.
     * 
     * @param resultSet Result set
     * @param rFmt      A format to encode the result set in
     */
    
    static public void output(ResultSet resultSet, ResultSetFormat rFmt)
    { output(System.out, resultSet, rFmt) ; }

    /** Output a ResultSetin some format.
     *  To get detailed control over each format, call the appropropiate operation directly. 
     * 
     * @param outStream Output
     * @param resultSet Result set
     * @param rFmt      A format to encode the result set in
     */
    
    static public void output(OutputStream outStream, ResultSet resultSet, ResultSetFormat rFmt)
    {
        if ( rFmt.equals(ResultSetFormat.syntaxXML) )
        {
            outputAsXML(outStream, resultSet) ;
            return ;
        }

        if ( rFmt.equals(ResultSetFormat.syntaxText) )
        {
            out(outStream, resultSet) ;
            return ;
        }

        if ( rFmt.equals(ResultSetFormat.syntaxJSON) )
        {
            outputAsJSON(outStream, resultSet) ;
            return ;
        }
        
        if ( rFmt.equals(ResultSetFormat.syntaxRDF_XML) )
        {
            outputAsRDF(outStream, "RDF/XML-ABBREV", resultSet) ;
            return ;
        }
        
        LogFactory.getLog(ResultSetFormatter.class).warn("Unknown ResultSetFormat: "+rFmt);
    }
    
    /** Write out an RDF model that encodes the result set
     * 
     * @param format        Name of RDF format (names as Jena writers) 
     * @param resultSet     The result set to encode in RDF
     */
    
    static public void outputAsRDF(String format, ResultSet resultSet)
    { outputAsRDF(System.out, format, resultSet) ; }

    /** Write out an RDF model that encodes the result set
     * 
     * @param outStream     Output
     * @param format        Name of RDF format (names as Jena writers) 
     * @param resultSet     The result set to encode in RDF
     */
    
    static public void outputAsRDF(OutputStream outStream, String format, ResultSet resultSet)
    {
        PrintWriter out = FileUtils.asPrintWriterUTF8(outStream) ;
        outputAsRDF(out, format, resultSet) ;
        out.flush() ;
    }

    /** Write out an RDF model that encodes the result set.
     *  See also the same method taking an output stream.
     *  
     * @param out           Output : ideally, should be a UTF-8 print writer (not system default) 
     * @param format        Name of RDF format (names as Jena writers) 
     * @param resultSet     The result set to encode in RDF
     */
    
    static private void outputAsRDF(PrintWriter out, String format, ResultSet resultSet)
    {
        Model m = toModel(resultSet) ;
        m.write(out, format) ;
        out.flush() ;
    }
    
    /** Write out an RDF model that encodes a boolean result
     * 
     * @param format        Name of RDF format (names as Jena writers) 
     * @param booleanResult The boolean result to encode in RDF
     */

    static public void outputAsRDF(String format,  boolean booleanResult)
    { outputAsRDF(format, booleanResult) ; }

    
    /** Write out an RDF model that encodes a boolean result
     * 
     * @param outStream     Output
     * @param format        Name of RDF format (names as Jena writers) 
     * @param booleanResult The boolean result to encode in RDF
     */

    static public void outputAsRDF(OutputStream outStream, String format,  boolean booleanResult)
    {
        PrintWriter out = FileUtils.asPrintWriterUTF8(outStream) ;
        outputAsRDF(out, format, booleanResult) ;
        out.flush() ;
    }

    /** Write out an RDF model that encodes a boolean result.
     *  See also the same method taking an output stream.
     *  
     * @param out           Output : ideally, should be a UTF-8 print writer (not system default) 
     * @param format        Name of RDF format (names as Jena writers) 
     * @param booleanResult The boolean result to encode in RDF
     */
    
    static private void outputAsRDF(PrintWriter out, String format,  boolean booleanResult)
    {
        Model m = toModel(booleanResult) ;
        m.write(out, format) ;
        out.flush() ;
    }

    
    // ---- XML Output

    /** Output a result set in the XML format
     * 
     * @param qresults      result set
     */
    
    static public void outputAsXML(ResultSet qresults)
    { outputAsXML(System.out, qresults) ; }

    /** Output a result set in the XML format
     * 
     * @param outStream     output stream
     * @param qresults      result set
     */
    
    static public void outputAsXML(OutputStream outStream, ResultSet qresults)
    {
        outputAsXML(outStream, qresults, (String)null) ;
    }
    
    /** Output a result set in the XML format, inserting a style sheet in the XMl output
     * 
     * @param qresults      result set
     * @param stylesheet    The URL of the stylsheet
     */
    
    static public void outputAsXML(ResultSet qresults, String stylesheet)
    { outputAsXML(System.out, qresults, stylesheet) ; }

    /** Output a result set in the XML format, inserting a style sheet in the XMl output
     * 
     * @param outStream     output stream
     * @param qresults      result set
     * @param stylesheet    The URL of the stylsheet
     */
    
    static public void outputAsXML(OutputStream outStream, ResultSet qresults, String stylesheet)
    {
        XMLOutput xOut = new XMLOutput(stylesheet) ;
        xOut.format(outStream, qresults) ;
    }
    
    // ----  XML output: ASK
    
    /** Output a boolean result in the XML format
     * 
     * @param booleanResult The boolean result to encode
     */
    
    public static void outputAsXML(boolean booleanResult)
    { outputAsXML(System.out, booleanResult) ; }
    
    /** Output a boolean result in the XML format
     * 
     * @param outStream     output stream
     * @param booleanResult The boolean result to encode
     */
    
    public static void outputAsXML(OutputStream outStream, boolean booleanResult)
    {
        outputAsXML(outStream, booleanResult, null) ;
    }

    /** Output a boolean result in the XML format
     * 
     * @param booleanResult 
     * @param stylesheet    The URL of the stylsheet
     */
    public static void outputAsXML(boolean booleanResult, String stylesheet)
    { outputAsXML(System.out, booleanResult, stylesheet) ; }

    /** Output a boolean result in the XML format
     * 
     * @param outStream     output stream
     * @param booleanResult 
     * @param stylesheet    The URL of the stylsheet
     */
    
    public static void outputAsXML(OutputStream outStream, boolean booleanResult, String stylesheet)
    {
        XMLOutputASK fmt = new XMLOutputASK(outStream, stylesheet) ;
        fmt.exec(booleanResult) ;
    }

    /** Return a string that has the result set serilized as XML (not RDF)
     * 
     * @param qresults  result set
     * @return  string
     */
    
    public static String asXMLString(ResultSet qresults)
    {
        return asXMLString(qresults, null) ;
    }
    
    /** Return a string that has the result set serilized as XML (not RDF)
     *  with a style sheet directive inserted into the XML.
     * @param qresults  result set
     * @param stylesheet
     * @return  string
     */
    
    public static String asXMLString(ResultSet qresults, String stylesheet)
    {
        XMLOutput xOut = new XMLOutput(stylesheet) ;
        return xOut.asString(qresults) ;
    }
    
    /** Return a string that has the result set serilized as XML (not RDF)
     * 
     * @param booleanResult The boolean result to encode
     * @return  string
     */
    
    public static String asXMLString(boolean booleanResult)
    {
        return asXMLString(booleanResult, null) ;
    }

    /** Return a string that has the result set serilized as XML (not RDF)
     * 
     * @param booleanResult The boolean result to encode
     * @param stylesheet
     * @return  string
     */
    
    public static String asXMLString(boolean booleanResult, String stylesheet)
    {
        XMLOutput xOut = new XMLOutput(stylesheet) ;
        return xOut.asString(booleanResult) ;
    }
    
    // JSON (and YAML)
    
    /** Output a result set in the JSON format
     *  Format: <a href="http://www.w3.org/2001/sw/DataAccess/json-sparql/">Serializing SPARQL Query Results in JSON</a> 
     *  JSON: <a href="http://json.org">http://json.org/</a>
     * @param resultSet     result set
     */
    
    static public void outputAsJSON(ResultSet resultSet)
    { outputAsJSON(System.out, resultSet) ; }
    
    /** Output a result set in the JSON format
     *  Format: <a href="http://www.w3.org/2001/sw/DataAccess/json-sparql/">Serializing SPARQL Query Results in JSON</a> 
     *  JSON: <a href="http://json.org">http://json.org/</a>
     *  
     * @param outStream     output stream
     * @param resultSet     result set
     */
    
    static public void outputAsJSON(OutputStream outStream, ResultSet resultSet)
    {
        JSONOutput jOut = new JSONOutput() ;
        jOut.format(outStream, resultSet) ; 
    }
    
    /** Output a result set in the JSON format
     *  Format: <a href="http://www.w3.org/2001/sw/DataAccess/json-sparql/">Serializing SPARQL Query Results in JSON</a> 
     *  JSON: <a href="http://json.org">http://json.org/</a>
     *  
     * @param booleanResult The boolean result to encode
     */
    
    static public void outputAsJSON(boolean booleanResult)
    { outputAsJSON(System.out, booleanResult ) ; }
    
    /** Output a result set in the JSON format
     *  Format: <a href="http://www.w3.org/2001/sw/DataAccess/json-sparql/">Serializing SPARQL Query Results in JSON</a> 
     *  JSON: <a href="http://json.org">http://json.org/</a>
     *  
     * @param outStream     output stream
     * @param booleanResult The boolean result to encode
     */
    
    static public void outputAsJSON(OutputStream outStream, boolean booleanResult)
    {
        JSONOutput jOut = new JSONOutput() ;
        jOut.format(outStream, booleanResult) ; 
    }

}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
