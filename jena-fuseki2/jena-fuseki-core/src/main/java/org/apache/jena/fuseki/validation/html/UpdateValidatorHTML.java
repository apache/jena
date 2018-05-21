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

package org.apache.jena.fuseki.validation.html;

import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.finishFixed;
import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.output;
import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.printHead;
import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.serviceLog;
import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.setHeaders;
import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.startFixed;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.update.UpdateFactory ;
import org.apache.jena.update.UpdateRequest ;

public class UpdateValidatorHTML
{
    public UpdateValidatorHTML() 
    { }
    
    static final String paramLineNumbers      = "linenumbers" ;
    static final String paramFormat           = "outputFormat" ;
    static final String paramUpdate            = "update" ;
    static final String paramSyntax           = "languageSyntax" ;
    //static final String paramSyntaxExtended   = "extendedSyntax" ;
    
    public static void executeHTML(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        try {
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
            outStream.println("<p>Input:</p>") ;
            output(outStream, (out)->out.print(updateString), lineNumbers) ;
            
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
                output(outStream, (out)->updateRequest.output(out), lineNumbers) ;
            }
            outStream.println("</body>") ;
            outStream.println("</html>") ;
            
        } catch (Exception ex)
        {
            serviceLog.warn("Exception in doGet",ex) ;
        }
    }
}
