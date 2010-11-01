/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd. 
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.validation;

import java.io.IOException ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateValidator extends ValidatorBase
{
    public UpdateValidator() 
    { }
    
    static final String paramLineNumbers      = "linenumbers" ;
    static final String paramFormat           = "outputFormat" ;
    static final String paramUpdate            = "update" ;
    static final String paramSyntax           = "languageSyntax" ;
    //static final String paramSyntaxExtended   = "extendedSyntax" ;
    
    @Override
    protected void execute(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        try {
//            if ( log.isInfoEnabled() )
//                log.info("validation request") ;
            
            String[] args = httpRequest.getParameterValues(paramUpdate) ;
            
            if ( args == null || args.length == 0 )
            {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "No update parameter to validator") ;
                return ;
            }
            
            if ( args.length > 1 )
            {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Too many update parameters") ;
                return ;
            }

            final String queryString = httpRequest.getParameter(paramUpdate).replaceAll("(\r|\n| )*$", "") ;
            
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
            
            // Currentl default.
            boolean outputSPARQL = true ;
            boolean outputPrefix = false ;
            boolean outputAlgebra = false ;
            boolean outputQuads = false ;
            
            if ( a != null )
            {
                outputSPARQL = false ;
                for ( int i = 0 ; i < a.length ; i++ )
                {
                    if ( a[i].equals("sparql") ) 
                        outputSPARQL = true ;
                    if ( a[i].equals("prefix") ) 
                        outputPrefix = true ;
                    if ( a[i].equals("algebra") ) 
                        outputAlgebra = true ;
                    if ( a[i].equals("quads") ) 
                        outputQuads = true ;
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
            
            printHead(outStream, "SPARQL Update Validation Report") ;
            
            outStream.println("<body>") ;
            outStream.println("<h1>SPARQL Update Validator</h1>") ;
            
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
            UpdateRequest request= null ;
            try {
                request = UpdateFactory.create(queryString, "http://example/base/", language) ;
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
            
            // Because we pass into anon inner classes
            final UpdateRequest updateRequest = request ;
            
            // OK?  Pretty print
            if ( updateRequest != null && outputSPARQL )
            {
                outStream.println("<p>Formatted, parsed update request:</p>") ;
                Content c = new Content(){
                    @Override
                    public void print(IndentedWriter out)
                    {
                        updateRequest.output(out) ;
                    }
                        
                } ;
                output(outStream, c, lineNumbers) ;
            }
            
//            if ( query != null && outputAlgebra )
//            {
//                outStream.println("<p>Algebra structure:</p>") ;
//                final Op op = Algebra.compile(query) ;   // No optimization
//                final SerializationContext sCxt = new SerializationContext(query) ;
//                Content c = new Content(){
//                    public void print(IndentedWriter out)
//                    {  op.output(out, sCxt) ; }
//                } ;
//                output(outStream, c , lineNumbers) ;
//            }
//            
//            if ( query != null && outputQuads )
//            {
//                outStream.println("<p>Quad structure:</p>") ;
//                final Op op = Algebra.toQuadForm(Algebra.compile(query)) ;
//                final SerializationContext sCxt = new SerializationContext(query) ;
//                Content c = new Content(){
//                    public void print(IndentedWriter out)
//                    {  op.output(out, sCxt) ; }
//                } ;
//                output(outStream, c , lineNumbers) ;
//            }
            
            outStream.println("</html>") ;
            
        } catch (Exception ex)
        {
            serviceLog.warn("Exception in doGet",ex) ;
        }
    }

    interface Content { void print(IndentedWriter out) ; }
    
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

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd. 
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