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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.PrintUtil;

public class TestPrintUtil extends TestCase
{    
    
    public TestPrintUtil(String name) {
        super( name );
    }
     
     public static TestSuite suite() {
         return new TestSuite( TestPrintUtil.class );
     }   

     // Minimal test of formating a URI with prefixes
     public void testPrefixUse() {
         String NS = "http://jena.hpl.hp.com/example#";
         String name = "r1";
         String uri = NS + name;
         String shortform = "p:" + name;
         Resource r = ResourceFactory.createResource(uri);
         assertEquals(uri, PrintUtil.print(r));
         
         PrintUtil.registerPrefix("p", NS);
         assertEquals(shortform, PrintUtil.print(r));
         
         PrintUtil.removePrefix("p");
         assertEquals(uri, PrintUtil.print(r));
         
         Map<String, String> map = new HashMap<>();
         map.put("p", NS);
         PrintUtil.registerPrefixMap(map);
         assertEquals(shortform, PrintUtil.print(r));

         PrintUtil.removePrefixMap( map );
         assertEquals(uri, PrintUtil.print(r));
     }
}
