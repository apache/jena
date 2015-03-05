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
import java.io.PrintStream ;
import java.util.Iterator ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.Violation ;
import org.apache.jena.riot.system.IRIResolver ;

public class IRIValidator extends ValidatorBase
{
    public IRIValidator() 
    { }
  
    static final String paramIRI      = "iri" ;
    //static IRIFactory iriFactory = IRIFactory.iriImplementation() ;
    static IRIFactory iriFactory = IRIResolver.iriFactory ;
    
    @Override
    protected void execute(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        try {
            String[] args = httpRequest.getParameterValues(paramIRI) ;
            ServletOutputStream outStream = httpResponse.getOutputStream() ;
            PrintStream stdout = System.out ;
            PrintStream stderr = System.err ;
            System.setOut(new PrintStream(outStream)) ;
            System.setErr(new PrintStream(outStream)) ;

            setHeaders(httpResponse) ;

            outStream.println("<html>") ;
            printHead(outStream, "Jena IRI Validator Report") ;
            outStream.println("<body>") ;

            outStream.println("<h1>IRI Report</h1>") ;

            startFixed(outStream) ;

            try {
                boolean first = true ;
                for ( String iriStr : args )
                {
                    if ( ! first )
                        System.out.println() ;
                    first = false ;

                    IRI iri = iriFactory.create(iriStr) ;
                    System.out.println(iriStr + " ==> "+iri) ;
                    if ( iri.isRelative() )
                        System.out.println("Relative IRI: "+iriStr) ;

                    Iterator<Violation> vIter = iri.violations(true) ;
                    for ( ; vIter.hasNext() ; )
                    {
                        String str = vIter.next().getShortMessage() ;
                        str = htmlQuote(str) ;
                        
                        System.out.println(str) ;
                    }
                }
            } finally 
            {
                finishFixed(outStream) ;
                System.out.flush() ;
                System.err.flush() ;
                System.setOut(stdout) ;
                System.setErr(stdout) ;
            }

            outStream.println("</body>") ;
            outStream.println("</html>") ;
        } catch (IOException ex) {}
    } 
}
