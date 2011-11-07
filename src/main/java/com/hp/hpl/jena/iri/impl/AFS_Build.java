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

package com.hp.hpl.jena.iri.impl;

import java.util.Iterator;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;


/** Driver for the build process */
public class AFS_Build
{
    static public void main(String args[]) throws Exception
    {
        checkOne("http://123.18.56/foo") ;
        checkOne("http://123.18/foo") ;
        checkOne("http://123/foo") ;
        checkOne("http://123.18.56.19/foo") ;
        System.exit(0) ;

        // violation.xml ==> ViolationCodes
        BuildViolationCodes.main(args) ;
        
        // host.jflex
        PatternCompiler.main(args) ;
        // Other jflex files
        AbsLexer.main(args) ;
        
        // Now refresh and rebuild.
        // Need to edit result to remove "private" from yytext in each subparser
    }
    
    static void checkOne(String s)
    {
        IRI iri = IRIFactory.iriImplementation().create(s) ;
        System.out.println(">> "+iri) ;
        for ( Iterator<Violation> iter = iri.violations(true) ; iter.hasNext() ; )
        {
            Violation v = iter.next();
            System.out.println(v.getShortMessage()) ;
        }
        System.out.println("<< "+iri) ;
        System.out.println() ;
    }
}
