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

package com.hp.hpl.jena.util;

import junit.framework.JUnit4TestAdapter;
import org.junit.Test;

import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.impl.Util;
public class TestUtil
{
     public static junit.framework.Test suite() {
         return new JUnit4TestAdapter(TestUtil.class) ;
     }
    
    // Intermediate : JUnit 3 and JUnit 4.
     
    @Test public void splitNS_01()
    { split("http://example/xyz", "http://example/", "xyz") ; }
    
    @Test public void splitNS_02()
    { split("http://example/ns#xyz", "http://example/ns#", "xyz") ; }
    
    @Test public void splitNS_03()
    { no_split("http://example/ns#") ; }
    
    @Test public void splitNS_04()
    { no_split("http://example/") ; }
    
    @Test public void splitNS_05()  // Illegal URI
    { split("http://example", "http://", "example") ; }
    
    @Test public void splitNS_06()   // localname must be at least the NCStartChar - not empty
    { split("mailto:me", "mailto:m", "e") ; }

    @Test public void splitNS_07()
    { split("urn:abc:xyz","urn:abc:", "xyz") ; } 

    @Test public void splitNS_08()
    { no_split("urn:abc:xyz:") ; } 

    @Test public void splitNS_09()
    { split("http://bio2rdf.org/pdb:Pentane-3%2C4-diol-5-phosphate", "http://bio2rdf.org/pdb:Pentane-3%2C4-", "diol-5-phosphate") ; }

    @Test public void splitNS_10()
    { split("http://bio2rdf.org/pdb:Pentane-3,4-diol-5-phosphate", "http://bio2rdf.org/pdb:Pentane-3,4-", "diol-5-phosphate") ; }

    // Don't split inside a %encoding. 
    @Test public void splitNS_11()
    { split("http://host/abc%AAdef", "http://host/abc%AA", "def") ; } 

    @Test public void splitNS_12()
    { split("http://host/abc%1Adef", "http://host/abc%1A", "def") ; } 
    
    @Test public void splitNS_13()
    { split("http://host/abc%A1def", "http://host/abc%A1", "def") ; } 
    
    @Test public void splitNS_14()
    { split("http://host/abc%AA22def", "http://host/abc%AA22", "def") ; } 

    @Test public void splitNS_15()
    { no_split("http://host/abc%AA22") ; } 

    // Other schemes
    
    @Test public void splitNS_50()
    { split("file:///x/y", "file:///x/", "y") ; } 

    @Test public void splitNS_51()
    { split("file:///x", "file:///", "x") ; } 

    @Test public void splitNS_52()
    { split("file:x", "file:", "x") ; } 

    @Test public void splitNS_53()
    // Not ideal but some URI schemes dislike a URI with just the scheme
    { split("file:foo", "file:", "foo") ; } 

    @Test public void splitNS_54()
    { split("file:c:/foo", "file:c:/", "foo") ; } 
    
    // urn:uuid:d871c7f4-2926-11b2-8073-a5e169788449 - legal type 1 uuid as urn
    // uuid:3cf3e43a-3a5d-40d8-a93c-8697b162a1c0 - legal type 4 uuid as uri
    
    @Test public void splitNS_55()
    { split("urn:uuid:d871c7f4-2926-11b2-8073-a5e169788449", "urn:uuid:", "d871c7f4-2926-11b2-8073-a5e169788449") ; }

    @Test public void splitNS_56()
    { split("uuid:3cf3e43a-3a5d-40d8-a93c-8697b162a1c0", "uuid:3", "cf3e43a-3a5d-40d8-a93c-8697b162a1c0") ; }
    
    @Test public void splitNS_57()
    { split("urn:abc:def", "urn:abc:", "def") ; }

    // --------
    
    static void  no_split(String string)
    { split(string, null, null) ; }

    static void split(String uriStr, String namespace, String localname)
    {
        if ( namespace == null && localname != null )
            fail("Bad test - namespace is null but local name is not") ;
        if ( namespace != null && localname == null )
            fail("Bad test - namespace is not null but local name is") ;
        
        int idx = Util.splitNamespace(uriStr) ;
        if ( idx == uriStr.length() ) 
        {
            // No split.
            if ( namespace != null )
                fail("Expected a split ("+namespace+","+localname+") - but none found") ;
            return ;
            
        }
        // Split
        String ns = uriStr.substring(0,idx) ;
        String ln = uriStr.substring(idx) ;
        assertEquals(namespace, ns) ;
        assertEquals(localname, ln) ;
    }
    
    
}
