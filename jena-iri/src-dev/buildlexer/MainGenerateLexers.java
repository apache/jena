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

package buildlexer;

import java.io.File ;
import java.lang.reflect.Method ;
import java.util.Iterator ;

import org.apache.jena.iri.IRI ;
import org.apache.jena.iri.IRIFactory ;
import org.apache.jena.iri.Violation ;
import org.apache.jena.iri.impl.BuildViolationCodes ;


/** Misc support for building the java files with JFlex.
 * The distribution includes pre-built files - you do not need to perform this
 * stage to rebuild the IRI library from it's source.
 */
public class MainGenerateLexers
{
    static public void main(String args[]) throws Exception
    {
        /* File built:
        LexerHost.java
        LexerPath.java
        LexerPort.java
        LexerQuery.java
        LexerScheme.java
        LexerUserinfo.java
        LexerXHost.java
        */
        
        // violation.xml ==> ViolationCodes
        BuildViolationCodes.main(args) ;
        
        // host.jflex
        PatternCompilerBuilder.main(args) ;
        
        // Other jflex files
        AbsLexerBuilder.main(args) ;
        
        // Now refresh and rebuild.
        // Need to edit result to remove "private" on yytext in each subparser
        // Then add all the @Overrides (Eclipse quick fix) to remove warnings.
        
        try {
            new File("tmp.jflex").delete() ;
        } catch (Throwable ex) {}
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
    
    public static void runJFlex(String[] strings)
    {
        Method main = null ;
        try
        {
            Class<? > jflex = Class.forName("JFlex.Main") ;
            main = jflex.getMethod("main", new Class[]{strings.getClass()}) ;
        } catch (Exception e)
        {
            System.err.println("Please include JFlex.jar on the classpath.") ;
            System.exit(1) ;
        }
        
        try
        {
            main.invoke(null, new Object[]{strings}) ;
        } catch (Exception e)
        {
            System.err.println("Problem interacting with JFlex") ;
            e.printStackTrace() ;
            System.exit(2) ;
        }
    }
}

