/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.validation;

import java.io.IOException ;
import java.io.PrintStream ;
import java.util.Iterator ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.riot.system.IRIResolver ;

import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;

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

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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