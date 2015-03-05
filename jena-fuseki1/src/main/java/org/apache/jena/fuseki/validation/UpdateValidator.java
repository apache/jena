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

            final String updateString = httpRequest.getParameter(paramUpdate).replaceAll("(\r|\n| )*$", "") ;
            
            String updateSyntax = httpRequest.getParameter(paramSyntax) ;
            if ( updateSyntax == null || updateSyntax.equals("") )
                updateSyntax = "SPARQL" ;

            Syntax language = Syntax.lookup(updateSyntax) ;
            if ( language == null )
            {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown syntax: "+updateSyntax) ;
                return ;
            }
            
            String lineNumbersArg = httpRequest.getParameter(paramLineNumbers) ; 
            String a[] = httpRequest.getParameterValues(paramFormat) ;
            
            // Currently default.
            boolean outputSPARQL = true ;
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
            
            // Print as received
            {
                outStream.println("<p>Input:</p>") ;
                // Not Java's finest hour.
                Content c = new Content(){
                    @Override
                    public void print(IndentedWriter out)
                    { out.print(updateString) ; }
                } ;
                output(outStream, c, lineNumbers) ;
            }
            
            // Attempt to parse it.
            UpdateRequest request= null ;
            try {
                request = UpdateFactory.create(updateString, "http://example/base/", language) ;
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
            outStream.println("</body>") ;
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
