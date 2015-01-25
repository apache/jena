/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package quack;

import java.util.List ;

import tdb.cmdline.CmdTDB ;
import arq.cmd.CmdException ;

import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.sys.Names ;

// Nor needed : use tdbindexcopy location1:name1 location2:name2 

public class qupdate extends CmdTDB {

    static public void main(String... argv) {
        TDB.setOptimizerWarningFlag(false) ;
        new qupdate(argv).mainRun() ;
    }

    protected qupdate(String[] argv) {
        super(argv) ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + "-loc DIR" ;
    }

    @Override
    protected void exec() {
        List<String> args = super.getPositional() ;

        if ( args.size() != 0 || args.size() > 2 )
            throw new CmdException("Wrong number of arguments; got "+args.size()+")") ; 
        
        String srcIndexName = "POS" ;
        String destIndexName = "PSO" ;
        Location location = super.getLocation() ;
        // POS -> PSO
        if ( location.exists(destIndexName, Names.bptExtTree) ||
             location.exists(destIndexName, Names.bptExtRecords) ) {
            throw new CmdException("Additional index already exists") ;
        }
        String primaryIndexName = Names.primaryIndexTriples ;
        
        TupleIndex srcIndex = IndexLib.connect(location, primaryIndexName, srcIndexName) ;
        TupleIndex destIndex = IndexLib.connect(location, primaryIndexName, destIndexName) ;
       
        // Monitor
        IndexLib.copyIndex(srcIndex, destIndex) ;
    }
}
