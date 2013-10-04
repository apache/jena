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

package arq.examples;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Simple example to show parsing a query and producing the
 *  SPARQL agebra expression for the query. */
public class AlgebraEx
{
    public static void main(String []args)
    {
        String s = "SELECT DISTINCT ?s { ?s ?p ?o }";
        
        // Parse
        Query query = QueryFactory.create(s) ;
        System.out.println(query) ;
        
        // Generate algebra
        Op op = Algebra.compile(query) ;
        op = Algebra.optimize(op) ;
        System.out.println(op) ;
        
        // Execute it.
        QueryIterator qIter = Algebra.exec(op, ExQuerySelect1.createModel()) ;
        
        // Results
        for ( ; qIter.hasNext() ; )
        {
            Binding b = qIter.nextBinding() ;
            System.out.println(b) ;
        }
        qIter.close() ;
    }
}
