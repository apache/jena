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

package org.apache.jena.larq.examples;

import java.io.StringReader;

import org.apache.jena.larq.IndexBuilderNode;
import org.apache.jena.larq.IndexLARQ;
import org.apache.jena.atlas.lib.StrUtils;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.util.Utils;

/** Example code to index subjects by some external content.
 *  Pattern 3. 
 */

public class ExLucene5
{
    
    public static void main(String[] a) throws Exception
    {
        System.out.println("ARQ Example: "+Utils.classShortName(ExLucene5.class)) ;
        System.out.println("ARQ: "+ARQ.VERSION) ;
        System.out.println() ;
        
        Model model = ModelFactory.createDefaultModel() ;

        IndexLARQ index = buildIndexExternalContent(model) ;
        
        // Search for string 
        String searchString = "+document" ;
        
        // This time, find documents with a matching DC title. 
        String queryString = StrUtils.strjoin("\n", 
            "PREFIX pf:     <http://jena.hpl.hp.com/ARQ/property#>",
            "SELECT ?doc {" ,
            "    ?doc pf:textMatch '"+searchString+"'.",
            "}") ;
        
        // Two of three docuemnts should match. 
        ExLucene1.performQuery(model, index, queryString) ;
        index.close() ;
    }
    
    static IndexLARQ buildIndexExternalContent(Model model)
    {
        // ---- Create index builder
        IndexBuilderNode larqBuilder = new IndexBuilderNode() ;
        
        Resource r1 = ResourceFactory.createResource("http://example/r1") ;
        Resource r2 = ResourceFactory.createResource("http://example/r2") ;
        Resource r3 = ResourceFactory.createResource("http://example/r3") ;
        Resource r4 = ResourceFactory.createResource("http://example/r4") ;
        Literal  lit1 = ResourceFactory.createPlainLiteral("doc") ;
        
        // ---- Index based on some external content.  Teh content can be any string of words. 
        
        larqBuilder.index(r1, new StringReader("document")) ;   // Just to show a Stringreader is possible
        larqBuilder.index(r2, "document") ;
        larqBuilder.index(r3, "slideshow") ;
        larqBuilder.index(r4, "codebase") ;
        larqBuilder.index(lit1, "document") ;
        
        // Note that the model is untouched - the index exists outside of any model statements.
        // The application is responsible for keeping 
        // ---- 
        
        larqBuilder.closeWriter() ;
        IndexLARQ index = larqBuilder.getIndex() ;
        
//        NodeIterator iter = index.searchModelByIndex(model, "document") ;
//        for ( ; iter.hasNext() ; )
//            System.out.println("Found: "+FmtUtils.stringForRDFNode((RDFNode)iter.next())) ;
        
        return index ;
    }

}
