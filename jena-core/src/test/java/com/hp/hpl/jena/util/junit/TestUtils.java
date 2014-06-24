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

package com.hp.hpl.jena.util.junit;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import com.hp.hpl.jena.rdf.model.* ;

public class TestUtils
{
    public static Resource getResource(Resource r, Property p)
    {
        if ( r == null )
            return null ;
        if ( ! r.hasProperty(p) )
            return null ;
        
        RDFNode n = r.getProperty(p).getObject() ;
        if ( n instanceof Resource )
            return (Resource)n ;
        
        throw new TestException("Manifest problem (not a Resource): "+n+" => "+p) ;
    }
    
    public static Collection<Resource> listResources(Resource r, Property p)
    {
        if ( r == null )
            return null ;
        List<Resource> x = new ArrayList<>() ;
        StmtIterator sIter = r.listProperties(p) ;
        for ( ; sIter.hasNext() ; ) {
            RDFNode n = sIter.next().getObject() ;
            if ( ! ( n instanceof Resource ) )
                throw new TestException("Manifest problem (not a Resource): "+n+" => "+p) ;
            x.add((Resource)n) ;
        }
        return x ;
    }
    
    public static String getLiteral(Resource r, Property p)
    {
        if ( r == null )
            return null ;
        if ( ! r.hasProperty(p) )
            return null ;
        
        RDFNode n = r.getProperty(p).getObject() ;
        if ( n instanceof Literal )
            return ((Literal)n).getLexicalForm() ;
        
        throw new TestException("Manifest problem (not a Literal): "+n+" => "+p) ;
    }
    
    public static String getLiteralOrURI(Resource r, Property p)
    {
        if ( r == null )
            return null ;
        
        if ( ! r.hasProperty(p) )
            return null ;
        
        RDFNode n = r.getProperty(p).getObject() ;
        if ( n instanceof Literal )
            return ((Literal)n).getLexicalForm() ;
        
        if ( n instanceof Resource )
        {
            Resource r2 = (Resource)n ; 
            if ( ! r2.isAnon() )
                return r2.getURI() ;
        }
        
        throw new TestException("Manifest problem: "+n+" => "+p) ;
    }
    
    public static String safeName(String s)
    {
        // Safe from Eclipse
        s = s.replace('(','[') ;
        s = s.replace(')',']') ;
        return s ;

    }
}
