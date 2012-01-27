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

package dev;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDBFactory ;

public class Jena202_TDBDatasetSetChurn
{
    static { Log.setLog4j() ; }

    public static void main(String[] argv)
    {
        String DB = "DB" ;

        FileOps.ensureDir(DB) ;

        Triple t = SSE.parseTriple("(<s> <p> <o>)") ;

        int i ;
        int Chunk = 1000 ;
        int tock = 100 ;
        for ( i = 0 ; i < 1000 ; i ++ )
        {
            if ( i != 0 && i%tock == 0 )
                System.out.println() ;
            System.out.print(".") ;
            
            Dataset ds = TDBFactory.createDataset(DB) ;
            ds.begin(ReadWrite.WRITE) ;
            for ( int j = i*Chunk; j < (i+1)*Chunk ; j++ )
                ds.getDefaultModel().getGraph().add(triple(j)) ;
            ds.commit();
            ds.end() ;
            
            ds.begin(ReadWrite.READ) ;
            Iter.count(ds.getDefaultModel().getGraph().find(t)) ;
            ds.end() ;
            //ds.close() ;
            
            //TDBFactory.release(ds) ;
            
        }
        System.out.println() ;
        System.out.println("DONE") ;
        System.exit(0) ;
    }
    
    static Node s = Node.createURI("s") ;
    static Node p = Node.createURI("s") ;
    static Triple triple(int i)
    {
        Node o = Node.createLiteral("X"+i) ;
        return new Triple(s,p,o) ;
    }
}
