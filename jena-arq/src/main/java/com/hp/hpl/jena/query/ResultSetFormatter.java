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

package com.hp.hpl.jena.query;

import java.io.ByteArrayOutputStream ;
import java.io.OutputStream ;
import java.io.PrintWriter ;
import java.io.UnsupportedEncodingException ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.ResultSetMgr ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.ARQNotImplemented ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingOutputStream ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.resultset.CSVOutput ;
import com.hp.hpl.jena.sparql.resultset.JSONOutput ;
import com.hp.hpl.jena.sparql.resultset.RDFOutput ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.resultset.TSVOutput ;
import com.hp.hpl.jena.sparql.resultset.TextOutput ;
import com.hp.hpl.jena.sparql.resultset.XMLOutput ;
import com.hp.hpl.jena.sparql.resultset.XMLOutputASK ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.util.FileUtils ;

/** ResultSetFormatter - Convenience ways to call the various output formatters.
 *  in various formats.
 *  @see ResultSetMgr
 */

public class ResultSetFormatter {
    // See also ResultSetMgr -- this post-dates this code.
    // Ideally, the operation here should call ResultSetMgr.
    
    private ResultSetFormatter() {}
    /**
     * Output a result set in a text format.  The result set is consumed.
     * Use @see{ResultSetFactory.makeRewindable(ResultSet)} for a rewindable one.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param qresults   result set
     */
    public static void out(ResultSet qresults)
    { out(System.out, qresults) ; }

    /**
     * Output a result set in a text format.  The result set is consumed.
     * Use @see{ResultSetFactory.makeRewindable(ResultSet)} for a rewindable one.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param out        OutputStream
     * @param qresults   result set
     */
    public static void out(OutputStream out, ResultSet qresults)
    { out(out, qresults, (PrefixMapping)null) ; }
    
    /**
     * Output a result set in a text format.  The result set is consumed.
     * Use @see{ResultSetFactory.makeRewindable(ResultSet)} for a rewindable one.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param qresults   result set
     * @param query     May be used to abbreviate URIs 
     */
    public static void out(ResultSet qresults, Query query)
    { out(System.out, qresults, query) ; }
    
    /**
     * Output a result set in a text format.  The result set is consumed.
     * Use @see{ResultSetFactory.makeRewindable(ResultSet)} for a rewindable one.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param qresults   result set
     * @param prologue   May be used to abbreviate URIs 
     */
    public static void out(ResultSet qresults, Prologue prologue)
    { out(System.out, qresults, prologue) ; }

    //    /**
//     * Output a result set in a text format.
//     * @param out        OutputStream
//     * @param qresults   result set
//     * @param query      May be used to abbreviate URIs 
//     */
//    public static void out(OutputStream out, ResultSet qresults, Query query)
//    { out(out, qresults, query.getPrefixMapping()) ; }
    
    /**
     * Output a result set in a text format.  The result set is consumed.
     * Use @see{ResultSetFactory.makeRewindable(ResultSet)} for a rewindable one.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param qresults  result set
     * @param pmap      Prefix mapping for abbreviating URIs.
     */
    public static void out(ResultSet qresults, PrefixMapping pmap)
    { out(System.out, qresults, pmap) ; }

    /**
     * Output a result set in a text format.  The result set is consumed.
     * Use @see{ResultSetFactory.makeRewindable(ResultSet)} for a rewindable one.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
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
     * Output a result set in a text format.  The result set is consumed.
     * Use @see{ResultSetFactory.makeRewindable(ResultSet)} for a rewindable one.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param out       OutputStream
     * @param qresults  result set
     * @param prologue  Prologue, used to abbreviate IRIs
     */
    public static void out(OutputStream out, ResultSet qresults, Prologue prologue)
    {
        TextOutput tFmt = new TextOutput(prologue) ;
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
    
    /** Return a string that has the result set serialized as a text table
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * 
     * @param qresults  result set
     * @return  string
     */
    
    public static String asText(ResultSet qresults)
    {
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        out(arr, qresults) ;
        try { return new String(arr.toByteArray(), "UTF-8") ; }
        catch (UnsupportedEncodingException e)
        {
            Log.warn(ResultSetFormatter.class, "UnsupportedEncodingException") ;
            return null ;
        }
    }

    /** Return a string that has the result set serialized as a text table
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * 
     * @param qresults  result set
     * @param prologue  Prologue, used to abbreviate IRIs

     * @return  string
     */
    
    public static String asText(ResultSet qresults, Prologue prologue)
    {
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        out(arr, qresults, prologue) ;
        try { return new String(arr.toByteArray(), "UTF-8") ; }
        catch (UnsupportedEncodingException e)
        {
            Log.warn(ResultSetFormatter.class, "UnsupportedEncodingException") ;
            return null ;
        }
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
            // Force nodes to be materialized.
            QuerySolution result = resultSet.nextSolution() ;
            for ( Iterator<String> iter = result.varNames() ; iter.hasNext() ; )
            {
                String vn = iter.next();
                RDFNode n = result.get(vn) ;
            }
            count++ ;
        }
        return count ;
    }
    

    /**
     * Turn the result set into a java.util.List
     * @param resultSet   The result set
     * @return            List of QuerySolutions
     */
    static public List<QuerySolution> toList(ResultSet resultSet)
    {
        List<QuerySolution> list = new ArrayList<>() ;
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
    
    /** Output a ResultSet in some format.
     * 
     * @param resultSet Result set
     * @param rFmt      A format to encode the result set in
     */
    
    static public void output(ResultSet resultSet, ResultsFormat rFmt)
    { output(System.out, resultSet, rFmt) ; }

    /** Output a ResultSet in some format.
     *  To get detailed control over each format, call the appropropiate operation directly. 
     * 
     * @param outStream Output
     * @param resultSet Result set
     * @param rFmt      A format to encode the result set in
     */
    
    static public void output(OutputStream outStream, ResultSet resultSet, ResultsFormat rFmt)
    {
        if ( rFmt.equals(ResultsFormat.FMT_RS_XML) )
        {
            outputAsXML(outStream, resultSet) ;
            return ;
        }

        if ( rFmt.equals(ResultsFormat.FMT_RS_JSON) )
        {
            outputAsJSON(outStream, resultSet) ;
            return ;
        }

        if ( rFmt.equals(ResultsFormat.FMT_RS_CSV) )
        {
            outputAsCSV(outStream, resultSet);
            return ;
        }

        if ( rFmt.equals(ResultsFormat.FMT_RS_TSV) )
        {
            outputAsTSV(outStream, resultSet);
            return ;
        }

        if ( rFmt.equals(ResultsFormat.FMT_RS_BIO) )
        {
            outputAsBIO(outStream, resultSet);
            return ;
        }

        if ( rFmt.equals(ResultsFormat.FMT_RS_JSON) )
        {
            outputAsJSON(outStream, resultSet) ;
            return ;
        }

        if ( rFmt.equals(ResultsFormat.FMT_RDF_XML) )
        {
            outputAsRDF(outStream, "RDF/XML-ABBREV", resultSet) ;
            return ;
        }
        
        if ( rFmt.equals(ResultsFormat.FMT_RDF_TTL) )
        {
            outputAsRDF(outStream, "TTL", resultSet) ;
            return ;
        }

        if ( rFmt.equals(ResultsFormat.FMT_RDF_NT) )
        {
            outputAsRDF(outStream, "N-TRIPLES", resultSet) ;
            return ;
        }
        throw new ARQException("Unknown ResultSet format: "+rFmt);
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
    { outputAsRDF(System.out, format, booleanResult) ; }

    
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
     * @param stylesheet    The URL of the stylesheet
     */
    public static void outputAsXML(boolean booleanResult, String stylesheet)
    { outputAsXML(System.out, booleanResult, stylesheet) ; }

    /** Output a boolean result in the XML format
     * 
     * @param outStream     output stream
     * @param booleanResult 
     * @param stylesheet    The URL of the stylesheet
     */
    
    public static void outputAsXML(OutputStream outStream, boolean booleanResult, String stylesheet)
    {
        XMLOutputASK fmt = new XMLOutputASK(outStream, stylesheet) ;
        fmt.exec(booleanResult) ;
    }

    /** Return a string that has the result set serialized as XML (not RDF)
     * <p>
     *  This builds the string in memory which can lead to memory exhaustion
     *  for large results.  It is generally better to use the 
     *  {@link #outputAsXML(OutputStream, ResultSet)} overload instead
     *  </p>
     * 
     * @param qresults  result set
     * @return  string
     */
    
    public static String asXMLString(ResultSet qresults)
    {
        return asXMLString(qresults, null) ;
    }
    
    /** Return a string that has the result set serialized as XML (not RDF)
     *  with a style sheet directive inserted into the XML.
     *  <p>
     *  This builds the string in memory which can lead to memory exhaustion
     *  for large results.  It is generally better to use the 
     *  {@link #outputAsXML(OutputStream, ResultSet, String)} overload instead
     *  </p>
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
     * <p>
     *  This builds the string in memory which can lead to memory exhaustion
     *  for large results.  It is generally better to use the 
     *  {@link #outputAsXML(OutputStream, boolean)} overload instead
     *  </p>
     * 
     * @param booleanResult The boolean result to encode
     * @return  string
     */
    
    public static String asXMLString(boolean booleanResult)
    {
        return asXMLString(booleanResult, null) ;
    }

    /** Return a string that has the result set serilized as XML (not RDF)
     * <p>
     *  This builds the string in memory which can lead to memory exhaustion
     *  for large results.  It is generally better to use the 
     *  {@link #outputAsXML(OutputStream, boolean, String)} overload instead
     *  </p>
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
    
    // ---- JSON (and YAML)
    
    /** Output a result set in the JSON format
     *  Format: <a href="http://www.w3.org/TR/rdf-sparql-json-res/">Serializing SPARQL Query Results in JSON</a> 
     *  JSON: <a href="http://json.org">http://json.org/</a>
     * @param resultSet     result set
     */
    
    static public void outputAsJSON(ResultSet resultSet)
    { outputAsJSON(System.out, resultSet) ; }
    
    /** Output a result set in the JSON format
     *  Format: <a href="http://www.w3.org/TR/rdf-sparql-json-res/">Serializing SPARQL Query Results in JSON</a> 
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
     *  Format: <a href="http://www.w3.org/TR/rdf-sparql-json-res/">Serializing SPARQL Query Results in JSON</a> 
     *  JSON: <a href="http://json.org">http://json.org/</a>
     *  
     * @param booleanResult The boolean result to encode
     */

    static public void outputAsJSON(boolean booleanResult)
    { outputAsJSON(System.out, booleanResult ) ; }
    
    /** Output a result set in the JSON format
     *  Format: <a href="http://www.w3.org/TR/rdf-sparql-json-res/">Serializing SPARQL Query Results in JSON</a> 
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
    
    // ---- SSE
    
    /** Output a boolean result in the SSE format
     *  Format: <a href="http://jena.apache.org/documentation/notes/sse.html">SSE</a> 
     *  
     * @param booleanResult The boolean result to encode
     */
    
    static public void outputAsSSE(boolean booleanResult)
    { outputAsSSE(System.out, booleanResult ) ; }
    
    /** Output a boolean result in the SSE format
     *  Format: <a href="http://jena.apache.org/documentation/notes/sse.html">SSE</a> 
     *  
     * @param outStream     output stream
     * @param booleanResult The boolean result to encode
     */
    
    static public void outputAsSSE(OutputStream outStream, boolean booleanResult)
    {
        throw new ARQNotImplemented("outputAsSSE") ;
    }

    /** Output a result set in the SSE format
     *  Format: <a href="http://jena.apache.org/documentation/notes/sse.html">SSE</a>
     *  @param resultSet     result set
     */
    
    static public void outputAsSSE(ResultSet resultSet)
    { outputAsSSE(System.out, resultSet) ; }
    
    /** Output a result set in the SSE format
     *  Format: <a href="http://jena.apache.org/documentation/notes/sse.html">SSE</a>
     *  @param resultSet     result set
     */
    
    static public void outputAsSSE(ResultSet resultSet, Prologue prologue)
    { outputAsSSE(System.out, resultSet, prologue) ; }

    /** Output a result set in the SSE format
     *  Format: <a href="http://jena.apache.org/documentation/notes/sse.html">SSE</a>
     * @param outStream  The output stream
     * @param resultSet     The result set
     */
    
    static public void outputAsSSE(OutputStream outStream, ResultSet resultSet)
    { outputAsSSE(outStream, resultSet, null) ; }
    
    /** Output a result set in the SSE format
     *  Format: <a href="http://jena.apache.org/documentation/notes/sse.html">SSE</a>
     * @param outStream     output stream
     * @param resultSet     result set
     * @param prologue
     */
    
    static public void outputAsSSE(OutputStream outStream, ResultSet resultSet, Prologue prologue)
    {
        throw new ARQNotImplemented("outputAsSSE") ;
    }
    
    // ---- CSV
    
    /** Output a boolean result in CSV format
     *  
     * @param booleanResult The boolean result to encode
     */
    
    static public void outputAsCSV(boolean booleanResult)
    { outputAsCSV(System.out, booleanResult ) ; }
    
    /** Output a boolean result in in CSV format
     *  
     * @param outStream     output stream
     * @param booleanResult The boolean result to encode
     */
    
    static public void outputAsCSV(OutputStream outStream, boolean booleanResult)
    {
        CSVOutput fmt = new CSVOutput() ;
        fmt.format(outStream, booleanResult) ;
    }

    /** Output a result set in CSV format
     *  @param resultSet     result set
     */
    
    static public void outputAsCSV(ResultSet resultSet)
    { outputAsCSV(System.out, resultSet) ; }
    
    /** Output a result set in CSV format
     * @param outStream  The output stream
     * @param resultSet     The result set
     */
    
    static public void outputAsCSV(OutputStream outStream, ResultSet resultSet)
    {
        CSVOutput fmt = new CSVOutput() ;
        fmt.format(outStream, resultSet) ;
    }

    // ---- TSV
    
    /** Output a boolean result in TSV (tab separated values) format
     *  
     * @param booleanResult The boolean result to encode
     */
    
    static public void outputAsTSV(boolean booleanResult)
    { outputAsTSV(System.out, booleanResult ) ; }
    
    /** Output a boolean result in in TSV format
     *  
     * @param outStream     output stream
     * @param booleanResult The boolean result to encode
     */
    
    static public void outputAsTSV(OutputStream outStream, boolean booleanResult)
    {
        TSVOutput fmt = new TSVOutput() ;
        fmt.format(outStream, booleanResult) ;
    }

    /** Output a result set in TSV format
     *  @param resultSet     result set
     */
    
    static public void outputAsTSV(ResultSet resultSet)
    { outputAsTSV(System.out, resultSet) ; }
    
    /** Output a result set in TSV format
     * @param outStream  The output stream
     * @param resultSet     The result set
     */
    
    static public void outputAsTSV(OutputStream outStream, ResultSet resultSet)
    {
        TSVOutput fmt = new TSVOutput() ;
        fmt.format(outStream, resultSet) ;
    }
    
    /** Output a result set in BIO format */
    public static void outputAsBIO(OutputStream out, ResultSet results)
    {
        List<Var> vars = Var.varList(results.getResultVars()) ;
        
        BindingOutputStream bout = new BindingOutputStream(out, vars) ;
        for ( ; results.hasNext() ; )
        {
            Binding b = BindingUtils.asBinding(results.next()) ;
            bout.write(b) ;
        }
        bout.flush() ;
    }

    
}
