/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query;

import java.io.InputStream ;
import java.util.List ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.NotFoundException ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.resultset.* ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderTable ;
import org.openjena.atlas.logging.Log ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.util.FileManager ;

/** ResultSetFactory - make result sets from places other than a query. */

public class ResultSetFactory
{
    /** Load a result set from file or URL into a result set (memory backed).
     * @param filenameOrURI
     * @return ResultSet
     */
    
    public static ResultSet load(String filenameOrURI)
    {
        return load(filenameOrURI, null) ; 
    }
    
    /** Load a result set from file or URL into a result set (memory backed).
     * @param filenameOrURI
     * @param format
     * @return ResultSet
     */
    
    public static ResultSet load(String filenameOrURI, ResultSetFormat format)
    {
        if ( format == null )
            format = ResultSetFormat.guessSyntax(filenameOrURI) ;
        
        if ( format == null )
        {
            Log.warn(ResultSet.class, "Null format - defaulting to XML") ;
            format = ResultSetFormat.syntaxXML ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxText) )
        {
            Log.fatal(ResultSet.class, "Can't read a text result set") ;
            throw new ResultSetException("Can't read a text result set") ;
        }
        
        InputStream in = FileManager.get().open(filenameOrURI) ;
        if ( in == null )
            throw new NotFoundException("Not found: "+filenameOrURI) ;
        return load(in, format) ;
    }
    
    /** Load a result set from input stream into a result set (memory backed).
     * @param input
     * @param format
     * @return ResultSet
     */
    
    public static ResultSet load(InputStream input, ResultSetFormat format)
    {
        if ( format == null )
        {
            Log.warn(ResultSet.class, "Null format - defaulting to XML") ;
            format = ResultSetFormat.syntaxXML ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxText) )
        {
            Log.warn(ResultSet.class, "Can't read a text result set") ;
            throw new ResultSetException("Can't read a text result set") ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxXML) )
            return ResultSetFactory.fromXML(input) ;
        
        if ( format.equals(ResultSetFormat.syntaxRDF_XML) )
        {
            Model m = ModelFactory.createDefaultModel() ;
            m.read(input, null) ;
            return ResultSetFactory.fromRDF(m) ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxRDF_TURTLE) )
        {
            Model m = ModelFactory.createDefaultModel() ;
            m.read(input, null, "TURTLE") ;
            return ResultSetFactory.fromRDF(m) ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxRDF_N3) )
        {
            Model m = ModelFactory.createDefaultModel() ;
            m.read(input, null, "N3") ;
            return ResultSetFactory.fromRDF(m) ;
        }

        if ( format.equals(ResultSetFormat.syntaxJSON) )
        {
            // Only ResultSets
            return JSONInput.fromJSON(input) ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxTSV) )
        {
            return TSVInput.fromTSV(input) ;
        }
        
        Log.warn(ResultSet.class, "Unknown result set syntax: "+format) ;
        return null ;

    }
    
    /** Load a result set (or any other model) from file or URL
     * @param filenameOrURI
     * @return Model
     */
    
    public static Model loadAsModel(String filenameOrURI)
    {
        return loadAsModel(null, filenameOrURI, null) ; 
    }
    
    /** Load a result set (or any other model) from file or URL
     * @param model     Load into this model (returned)
     * @param filenameOrURI
     * @return Model 
     */
    public static Model loadAsModel(Model model, String filenameOrURI)
    {
        return loadAsModel(model, filenameOrURI, null) ; 
    }
    
    /** Load a result set (or any other model) from file or URL
     * @param filenameOrURI
     * @param format
     * @return Model
     */
    
    public static Model loadAsModel(String filenameOrURI, ResultSetFormat format)
    { return loadAsModel(null, filenameOrURI, format) ; }
    
    /** Load a result set (or any other model) from file or URL.
     *  Does not have to be a result set (e.g. CONSTRUCt results) 
     *  but it does interpret the ResultSetFormat possibilities.
     * @param model     Load into this model (returned)
     * @param filenameOrURI
     * @param format
     * @return Model
     */
    
    public static Model loadAsModel(Model model, String filenameOrURI, ResultSetFormat format)
    {
        if ( model == null )
            model = GraphFactory.makeDefaultModel() ;
        
        if ( format == null )
            format = ResultSetFormat.guessSyntax(filenameOrURI) ;
        
        if ( format == null )
        {
            Log.warn(ResultSet.class, "Null format - defaulting to XML") ;
            format = ResultSetFormat.syntaxXML ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxText) )
        {
            Log.fatal(ResultSet.class, "Can't read a text result set") ;
            throw new ResultSetException("Can't read a text result set") ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxXML) || format.equals(ResultSetFormat.syntaxJSON))
        {
            InputStream in = null ;
            try { 
                in = FileManager.get().open(filenameOrURI) ;
                if ( in == null )
                    throw new NotFoundException(filenameOrURI) ;
            }
            catch (NotFoundException ex) { throw new NotFoundException("File not found: "+filenameOrURI) ; }
            
            SPARQLResult x = null ;
            
            if ( format.equals(ResultSetFormat.syntaxJSON) )
                x = JSONInput.make(in, GraphFactory.makeDefaultModel()) ;
            else
                x = XMLInput.make(in, GraphFactory.makeDefaultModel()) ;
                    
            if ( x.isResultSet() )
                ResultSetFormatter.asRDF(model, x.getResultSet() ) ;
            else
                ResultSetFormatter.asRDF(model, x.getBooleanResult() ) ;
        
            return model ;
        }
        
        if ( format.isRDFGraphSyntax() )
            return FileManager.get().readModel(model, filenameOrURI) ;
        
        Log.fatal(ResultSet.class, "Unknown result set syntax: "+format) ;
        return null ;
    }

    /** 
     * Read in any kind of result kind (result set, boolean, graph)
     * Guess the syntax based on filename/URL extension. 
     */
    public static SPARQLResult result(String filenameOrURI)
    {
        return result(filenameOrURI, null) ;
    }

    /** 
     * Read in any kind of result kind (result set, boolean, graph)
     */
    
    public static SPARQLResult result(String filenameOrURI, ResultSetFormat format)
    {
        if ( format == null )
            format = ResultSetFormat.guessSyntax(filenameOrURI) ;
        
        if ( format == null )
        {
            Log.warn(ResultSet.class, "Null format - defaulting to XML") ;
            format = ResultSetFormat.syntaxXML ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxText) )
        {
            Log.fatal(ResultSet.class, "Can't read a text result set") ;
            throw new ResultSetException("Can't read a text result set") ;
        }
        
        if ( format.equals(ResultSetFormat.syntaxXML) || format.equals(ResultSetFormat.syntaxJSON))
        {
            InputStream in = null ;
            try { 
                in = FileManager.get().open(filenameOrURI) ;
                if ( in == null )
                    throw new NotFoundException(filenameOrURI) ;
            }
            catch (NotFoundException ex) { throw new NotFoundException("File not found: "+filenameOrURI) ; }
            
            SPARQLResult x = null ;
            
            if ( format.equals(ResultSetFormat.syntaxJSON) )
                return JSONInput.make(in, GraphFactory.makeDefaultModel()) ;
            else
                return XMLInput.make(in, GraphFactory.makeDefaultModel()) ;
        }
        
        if ( format.isRDFGraphSyntax() )
        {
            Model model = FileManager.get().loadModel(filenameOrURI) ;
            return new SPARQLResult(model) ;
        }

        Log.fatal(ResultSet.class, "Unknown result set syntax: "+format) ;
        return null ;
    }
    
    /** Read XML which is the format of the SPARQL result set format.
     * 
     * @param in    InputStream
     * @return      ResultSet
     */  
    public static ResultSet fromXML(InputStream in)
    {
        return XMLInput.fromXML(in) ;
    }
    
    /** Read XML which is the format of the SPARQL result set format.
     * 
     * @param str    String to process
     * @return      ResultSet
     */  
    public static ResultSet fromXML(String str)
    {
        return XMLInput.fromXML(str) ;
    }

    /** Turns an RDF model, with properties and classses from the
     * result set vocabulary, into a SPARQL result set.
     * The result set formed is a copy in memory.
     * 
     * @param model
     * @return ResultSet
     */
    public static ResultSet fromRDF(Model model) 
    {
        return new RDFInput(model) ;
    }

    /** Read from an input stream which is the format of the SPARQL result set format in JSON.
     * 
     * @param in    InputStream
     * @return      ResultSet
     */  
    public static ResultSet fromJSON(InputStream in)
    {
        return JSONInput.fromJSON(in) ;
    }
    
    /** Read from an input stream which is the format of the SPARQL result set format in TSV.
     * 
     * @param in    InputStream
     * @return      ResultSet
     */  
    public static ResultSet fromTSV(InputStream in)
    {
        return TSVInput.fromTSV(in) ;
    }
    
    /** Read from an input stream which is the format of the SPARQL result set format in SSE.
     * 
     * @param in    InputStream
     * @return      ResultSet
     */  
    public static ResultSet fromSSE(InputStream in)
    {
        Item item = SSE.parse(in) ;
        Log.warn(ResultSet.class, "Reading SSE result set not full implemented") ;
        // See SPARQLResult.  Have a level of ResultSetFactory that does "get SPARQLResult".
        // Or just boolean/result set because those are both srx. etc. 
        
        BuilderTable.build(item) ;
        return null ;
    }
    
    /** Turns an RDF model, with properties and classses from the
     * result set vocabulary, into a SPARQL result set.
     * The result set formed is a copy in memory.
     * 
     * @param model
     * @return ResultSet
     */
    static public ResultSet makeResults(Model model)
    {
        return new RDFInput(model) ;
    }
    
    /** Turns an RDF model, with properties and classses from the
     * result set vocabulary, into a SPARQL result set which is rewindable
     * (has a .reset()operation).
     * The result set formed is a copy in memory.
     * 
     * @param model
     * @return ResultSetRewindable
     */
    static public ResultSetRewindable makeRewindable(Model model)
    {
        return new RDFInput(model) ;
    }
    
    /** Turn an existing result set into a rewindable one.
     *  May take a copy - uses up the result set passed in.
     * @param resultSet
     * @return ResultSetRewindable
     */
    static public ResultSetRewindable makeRewindable(ResultSet resultSet)
    {
        return new ResultSetMem(resultSet) ;
    }
    

    /** Sort an existing result set.  Experimental.
     *  The list of variables is a list of names (strings),
     *  with "x" for ascending in variable "x" and "-x" for
     *  descending in variable "x"   
     * @param resultSet
     * @param conditions
     * @return ResultSet
     */
    static public ResultSet makeSorted(ResultSet resultSet, List<SortCondition> conditions)
    {
        return new SortedResultSet(resultSet, conditions) ;
    }

    /** Take a copy of a result set - the result set returns is an in-memory copy.
     *  It is not attached to the original query execution object which can be closed.
     * @param results
     * @return ResultSet
     */
    static public ResultSetRewindable copyResults(ResultSet results)
    {
        return new ResultSetMem(results) ; 
    }
    
    /** Build a result set from one of ARQ's lower level query iterator.
     *  @param queryIterator
     *  @param vars     List of variables, by name, for the result set
     *  @return ResultSet 
     */
    static public ResultSet create(QueryIterator queryIterator, List<String> vars)
    {
        return new ResultSetStream(vars, null, queryIterator) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
