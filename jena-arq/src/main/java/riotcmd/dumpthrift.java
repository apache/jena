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

package riotcmd;

import java.io.InputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.riot.RIOT ;
import org.apache.jena.riot.thrift.BinRDF ;

import com.hp.hpl.jena.sparql.util.Utils ;

/** Dump an rdf-thrift file to show structure */ 
public class dumpthrift {
    static { LogCtl.setCmdLogging(); }

    public static void main(String[] args) {
        RIOT.init() ;
        if ( args.length == 0 ) {
            args = new String[] {"-"} ;
        }
        
        if ( args.length != 1 ) {
            System.err.println("Usage: "+Utils.classShortName(dumpthrift.class)+" FILE") ;
            System.exit(2) ;
        }
        
        // Leave a general loop ...
        for ( String fn : args ) {
            InputStream in = IO.openFile(fn) ; 
            BinRDF.dump(System.out, in) ;
        }
    }
}    

