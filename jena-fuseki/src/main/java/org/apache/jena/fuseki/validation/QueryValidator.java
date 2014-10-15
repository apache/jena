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

package org.apache.jena.fuseki.validation;

import java.io.IOException ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;

public class QueryValidator extends ValidatorBase 
{
    public QueryValidator() 
    { }

    static final String paramLineNumbers      = "linenumbers" ;
    static final String paramFormat           = "outputFormat" ;
    static final String paramQuery            = "query" ;
    static final String paramSyntax           = "languageSyntax" ;
    //static final String paramSyntaxExtended   = "extendedSyntax" ;
    
    @Override
    protected void execute(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        try {
//            if ( log.isInfoEnabled() )
//                log.info("validation request") ;
            
            String[] args = httpRequest.getParameterValues(paramQuery) ;
            
            if ( args == null || args.length == 0 )
            {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "No query parameter to validator") ;
                return ;
            }
            
            if ( args.length > 1 )
            {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Too many query parameters") ;
                return ;
            }

            final String queryString = httpRequest.getParameter(paramQuery).replaceAll("(\r|\n| )*$", "") ;
//            queryString = queryString.replace("\r\n", "\n") ;
//            queryString.replaceAll("(\r|\n| )*$", "") ;
            
            String querySyntax = httpRequest.getParameter(paramSyntax) ;
            if ( querySyntax == null || querySyntax.equals("") )
                querySyntax = "SPARQL" ;

            Syntax language = Syntax.lookup(querySyntax) ;
            if ( language == null )
            {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown syntax: "+querySyntax) ;
                return ;
            }
            
            String lineNumbersArg = httpRequest.getParameter(paramLineNumbers) ; 

            String a[] = httpRequest.getParameterValues(paramFormat) ;
            
            boolean outputSPARQL = false ;
            boolean outputPrefix = false ;
            boolean outputAlgebra = false ;
            boolean outputQuads = false ;
            boolean outputOptimized = false ;
            boolean outputOptimizedQuads = false ;
            
            if ( a != null )
            {
                for ( String anA : a )
                {
                    if ( anA.equals( "sparql" ) )
                    {
                        outputSPARQL = true;
                    }
                    if ( anA.equals( "prefix" ) )
                    {
                        outputPrefix = true;
                    }
                    if ( anA.equals( "algebra" ) )
                    {
                        outputAlgebra = true;
                    }
                    if ( anA.equals( "quads" ) )
                    {
                        outputQuads = true;
                    }
                    if ( anA.equals( "opt" ) )
                    {
                        outputOptimized = true;
                    }
                    if ( anA.equals( "optquads" ) )
                    {
                        outputOptimizedQuads = true;
                    }
                }
            }
            
//            if ( ! outputSPARQL && ! outputPrefix )
//                outputSPARQL = true ;
            
            boolean lineNumbers = true ;
            
            if ( lineNumbersArg != null )
                lineNumbers = lineNumbersArg.equalsIgnoreCase("true") || lineNumbersArg.equalsIgnoreCase("yes") ;
            
            // Headers
            setHeaders(httpResponse) ;
            
            ServletOutputStream outStream = httpResponse.getOutputStream() ;

            outStream.println("<html>") ;
            
            printHead(outStream, "SPARQL Query Validation Report") ;
            
            outStream.println("<body>") ;
            outStream.println("<h1>SPARQL Query Validator</h1>") ;
            // Print query as received
            {
                outStream.println("<p>Input:</p>") ;
                // Not Java's finest hour.
                Content c = new Content(){
                    @Override
                    public void print(IndentedWriter out)
                    { out.print(queryString) ; }
                } ;
                output(outStream, c, lineNumbers) ;
            }
            
            // Attempt to parse it.
            Query query = null ;
            try {
                query = QueryFactory.create(queryString, "http://example/base/", language) ;
            } catch (ARQException ex)
            {
                // Over generous exception (should be QueryException)
                // but this makes the code robust.
                outStream.println("<p>Syntax error:</p>") ;
                startFixed(outStream) ;
                outStream.println(ex.getMessage()) ;
                finishFixed(outStream) ;
            }
            catch (RuntimeException ex)
            { 
                outStream.println("<p>Internal error:</p>") ;
                startFixed(outStream) ;
                outStream.println(ex.getMessage()) ;
                finishFixed(outStream) ;
            }
            
            if ( query != null )
            {
                if ( outputSPARQL )
                    outputSyntax(outStream, query, lineNumbers) ;
                
                if ( outputAlgebra )
                    outputAlgebra(outStream, query, lineNumbers) ;
                
                if ( outputQuads )
                    outputAlgebraQuads(outStream, query, lineNumbers) ;
                
                if ( outputOptimized )
                    outputAlgebraOpt(outStream, query, lineNumbers) ;

                if ( outputOptimizedQuads )
                    outputAlgebraOptQuads(outStream, query, lineNumbers) ;
            }
            
            outStream.println("</body>") ;
            outStream.println("</html>") ;
            
        } catch (Exception ex)
        {
            serviceLog.warn("Exception in doGet",ex) ;
        }
    }
    
    interface Content { void print(IndentedWriter out) ; }
    
    private void outputSyntax(ServletOutputStream outStream, final Query query, boolean lineNumbers) throws IOException
    {
        outStream.println("<p>Formatted, parsed query:</p>") ;
        Content c = new Content(){
            @Override
            public void print(IndentedWriter out)
            { query.serialize(out) ; }
        } ;
        output(outStream, c, lineNumbers) ;
    }
    
    private void outputAlgebra(ServletOutputStream outStream, final Query query, boolean lineNumbers) throws IOException
    {
        outStream.println("<p>Algebra structure:</p>") ;
        final Op op = Algebra.compile(query) ;   // No optimization
        output(outStream, query, op, lineNumbers) ;
    }
        
    private void outputAlgebraOpt(ServletOutputStream outStream, final Query query, boolean lineNumbers) throws IOException
    {
        outStream.println("<p>Alebgra, with general triple optimizations:</p>") ;
        final Op op = Algebra.optimize(Algebra.compile(query)) ;
        output(outStream, query, op, lineNumbers) ;
    }
        
    private void outputAlgebraQuads(ServletOutputStream outStream, final Query query, boolean lineNumbers) throws IOException
    {
        outStream.println("<p>Quad structure:</p>") ;
        final Op op = Algebra.toQuadForm(Algebra.compile(query)) ;
        output(outStream, query, op, lineNumbers) ;
    }

    private void outputAlgebraOptQuads(ServletOutputStream outStream, final Query query, boolean lineNumbers) throws IOException
    {
        outStream.println("<p>Alebgra, with general quads optimizations:</p>") ;
        final Op op = Algebra.optimize(Algebra.toQuadForm(Algebra.compile(query))) ;
        output(outStream, query, op, lineNumbers) ;
    }
    
    private void output(ServletOutputStream outStream, Query query, final Op op, boolean lineNumbers) throws IOException
    {
        final SerializationContext sCxt = new SerializationContext(query) ;
        Content c = new Content(){
            @Override
            public void print(IndentedWriter out)
            {  op.output(out, sCxt) ; }
        } ;
        output(outStream, c , lineNumbers) ;
    }

    private void output(ServletOutputStream outStream, Content content, boolean lineNumbers) throws IOException
    {
        startFixed(outStream) ;
        IndentedLineBuffer out = new IndentedLineBuffer(lineNumbers) ; 
        content.print(out) ;
        out.flush() ;  
        String x = htmlQuote(out.asString()) ;
        byte b[] = x.getBytes("UTF-8") ;
        outStream.write(b) ;
        finishFixed(outStream) ;
    }
}
