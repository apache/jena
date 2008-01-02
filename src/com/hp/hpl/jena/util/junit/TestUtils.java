/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.util.junit;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/** com.hp.hpl.jena.query.test.TestUtils
 * 
 * @author Andy Seaborne
 * @version $Id: TestUtils.java,v 1.2 2008-01-02 12:11:12 andy_seaborne Exp $
 */

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

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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