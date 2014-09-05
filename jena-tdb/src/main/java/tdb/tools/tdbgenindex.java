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

package tdb.tools;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** copy one index to another, possibly chnagign the order */ 
public class tdbgenindex
{
    public static void main(String...argv)
    {
        // Usage: srcLocation indexName dstLocation indexName
        if ( argv.length != 4 )
        {
            System.err.println("Usage: "+Utils.classShortName(tdbgenindex.class)+" srcLocation srcIndex dstLocation dstIndex") ;
            System.exit(1) ;
        }
        
        Location srcLoc = new Location(argv[0]) ;
        String srcIndexName = argv[1] ;
        
        Location dstLoc = new Location(argv[2]) ;
        String dstIndexName = argv[3] ;
        
        int readCacheSize = 0 ;
        int writeCacheSize = -1 ;
        
        if ( srcIndexName.length() != dstIndexName.length() )
        {
            System.err.println("srcIndexName.length() != dstIndexName.length() "+srcIndexName+" :: "+dstIndexName ) ;
            System.exit(1) ;
        }
            
        String primary ;
        int dftKeyLength ;
        int dftValueLength ;
        
        if ( srcIndexName.length() == 3 )
        {
            primary = Names.primaryIndexTriples ;
            dftKeyLength = SystemTDB.LenIndexTripleRecord ;
            dftValueLength = 0 ;
        }
        else if ( srcIndexName.length() == 4 )
        {
            primary = Names.primaryIndexQuads ;
            dftKeyLength = SystemTDB.LenIndexQuadRecord ;
            dftValueLength = 0 ;
        }
        else
        {
            System.err.println("indexlength != 3 or 4") ;
            System.exit(1) ;
            primary = null ;
            dftKeyLength = 0 ;
            dftValueLength = 0 ;
        }
        
        TupleIndex srcIdx = SetupTDB.makeTupleIndex(srcLoc, primary, srcIndexName, srcIndexName, dftKeyLength) ;
        TupleIndex dstIdx = SetupTDB.makeTupleIndex(dstLoc, primary, dstIndexName, dstIndexName, dftKeyLength) ;
        
        Iterator<Tuple<NodeId>> iter = srcIdx.all() ;
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> tuple = iter.next() ;
            dstIdx.add(tuple) ;
        }
        srcIdx.close() ;
        dstIdx.close() ;
    }
}

