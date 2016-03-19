/**
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

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.Violation ;
import org.apache.jena.riot.system.IRIResolver ;

public class IRIValidator extends ValidatorBaseJson {

    private static final long serialVersionUID = -2137772518194890583L;

    public IRIValidator() { }
    
    static IRIFactory iriFactory = IRIResolver.iriFactory ;
    
    static final String paramIRI           = "iri" ;

    // Output is an object  { "iris" : [ ] }
    // { "iri": "" , "error": [], "warnings": [] }
    static final String jIRIs    = "iris" ;
    static final String jIRI     = "iri" ;

    @Override
    protected JsonObject execute(ValidationAction action) {
        JsonBuilder obj = new JsonBuilder() ;
        obj.startObject() ;
        
        String args[] = getArgs(action, paramIRI) ;
        if ( args.length == 0 )
            ServletOps.errorBadRequest("No IRIs supplied");
        
        obj.key(jIRIs) ;
        obj.startArray() ;
        
        for ( String iriStr : args )
        {
            obj.startObject() ;
            obj.key(jIRI).value(iriStr) ;

            IRI iri = iriFactory.create(iriStr) ;


            List<String> errors = new ArrayList<>() ;
            List<String> warnings = new ArrayList<>() ;

            if ( iri.isRelative() )
                warnings.add("Relative IRI: "+iriStr) ;

            Iterator<Violation> vIter = iri.violations(true) ;
            for ( ; vIter.hasNext() ; )
            {
                Violation v = vIter.next() ;
                String str = v.getShortMessage() ;
                if ( v.isError() )
                    errors.add(str) ;
                else
                    warnings.add(str) ;
            }
            
            obj.key(jErrors) ;
            obj.startArray() ;
            for ( String msg : errors )
                obj.value(msg) ;
            obj.finishArray() ;
                
            obj.key(jWarnings) ;
            obj.startArray() ;
            for ( String msg : warnings )
                obj.value(msg) ;
            obj.finishArray() ;

            obj.finishObject() ;
        }
          
        
       obj.finishArray() ;
        
        obj.finishObject() ;
        return obj.build().getAsObject() ;
    }
    @Override
    protected String validatorName() {
        return "RDF Data" ;
    }
}

//static final String paramIRI      = "iri" ;
////static IRIFactory iriFactory = IRIFactory.iriImplementation() ;
//static IRIFactory iriFactory = IRIResolver.iriFactory ;
//
//@Override
//protected void execute(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
//{
//    try {
//        String[] args = httpRequest.getParameterValues(paramIRI) ;
//        ServletOutputStream outStream = httpResponse.getOutputStream() ;
//        PrintStream stdout = System.out ;
//        PrintStream stderr = System.err ;
//        System.setOut(new PrintStream(outStream)) ;
//        System.setErr(new PrintStream(outStream)) ;
//
//        setHeaders(httpResponse) ;
//
//        outStream.println("<html>") ;
//        printHead(outStream, "Jena IRI Validator Report") ;
//        outStream.println("<body>") ;
//
//        outStream.println("<h1>IRI Report</h1>") ;
//
//        startFixed(outStream) ;
//
//        try {
//            boolean first = true ;
//            for ( String iriStr : args )
//            {
//                if ( ! first )
//                    System.out.println() ;
//                first = false ;
//
//                IRI iri = iriFactory.create(iriStr) ;
//                System.out.println(iriStr + " ==> "+iri) ;
//                if ( iri.isRelative() )
//                    System.out.println("Relative IRI: "+iriStr) ;
//
//                Iterator<Violation> vIter = iri.violations(true) ;
//                for ( ; vIter.hasNext() ; )
//                {
//                    String str = vIter.next().getShortMessage() ;
//                    str = htmlQuote(str) ;
//                    
//                    System.out.println(str) ;
//                }
//            }
//        } finally 
//        {
//            finishFixed(outStream) ;
//            System.out.flush() ;
//            System.err.flush() ;
//            System.setOut(stdout) ;
//            System.setErr(stdout) ;
//        }
//
//        outStream.println("</body>") ;
//        outStream.println("</html>") ;
//    } catch (IOException ex) {}
//} 
