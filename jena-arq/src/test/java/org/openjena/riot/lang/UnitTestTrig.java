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

package org.openjena.riot.lang;

import junit.framework.TestCase ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotParseException ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.lib.DatasetLib ;

public class UnitTestTrig extends TestCase
{
    String input ;
    String output ;
    String baseIRI ;
    
    public UnitTestTrig(String name, String input, String output, String baseIRI)
    {
        super(name) ;
        this.input = input ;
        this.output = output ;
        this.baseIRI = baseIRI ;
    }
    
    @Override
    public void runTest()
    {
        try {
            DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
            RiotLoader.read(input, dsg, Lang.TRIG, baseIRI) ;
            //DatasetLib.dump(dsg) ;
            
            DatasetGraph dsg2 = DatasetLib.createDatasetGraphMem() ;
            RiotLoader.read(output, dsg2, Lang.NQUADS, null) ;

            // Compare with expected.

            boolean b = DatasetLib.isomorphic(dsg, dsg2) ;
            if ( ! b )
            {
                System.out.println("---- Parsed");
                DatasetLib.dump(dsg) ;
                System.out.println("---- Expected");
                DatasetLib.dump(dsg2) ;
                System.out.println("--------");
            }

            assertTrue("Datasets are not isomorphic", b) ;
        } 
        // Catch and retrhow - debugging.
        catch (RiotParseException ex) 
        {
            throw ex ;
        }
        catch (RuntimeException ex) 
        { 
            ex.printStackTrace(System.err) ;
            throw ex ;
        }
    }
}
