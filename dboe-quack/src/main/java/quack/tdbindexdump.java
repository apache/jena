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

package quack ;

import jena.cmd.ArgDecl ;
import jena.cmd.CmdException ;
import arq.cmdline.CmdARQ ;

import org.apache.commons.lang3.NotImplementedException ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.riot.RIOT ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.TDB ;
import org.seaborne.tdb2.store.tupletable.TupleIndex ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Dump a TDB index.
 */
public class tdbindexdump extends CmdARQ {
    static { LogCtl.setCmdLogging() ; }
    static Logger log = LoggerFactory.getLogger(tdbindexcopy.class) ;

    static final ArgDecl argLocation = new ArgDecl(true, "loc", "location") ;

    static public void main(String... argv) {
        new tdbindexdump(argv).mainRun() ;
    }

    protected tdbindexdump(String[] argv) {
        super(argv) ;
        super.modVersion.addClass(ARQ.class) ;
        super.modVersion.addClass(RIOT.class) ;
        super.modVersion.addClass(TDB.class) ;
        super.add(argLocation) ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + "-loc DIR IndexName | location[name]" ;
    }

    IndexRef idx = null ;

    @Override
    protected void processModulesAndArgs() {
        if ( super.getNumPositional() != 1 )
            throw new CmdException("location[name] or --loc=DIR name") ;

        String x = super.getPositionalArg(0) ;

        if ( super.contains(argLocation) ) {
            Location loc = Location.create(super.getValue(argLocation)) ;
            idx = IndexRef.parse(loc, x) ;
        } else {
            idx = IndexRef.parse(x) ;
        }

        int N = idx.getIndexName().length() ;
        if ( N != 3 && N != 4 )
            throw new CmdException("Index must 3 or 4 in length : " + idx.getIndexName()) ;
        
        if ( ! idx.exists() )
            throw new CmdException("No such index: "+ idx) ;
    }

    @Override
    protected void exec() {   
            throw new NotImplementedException(cmdName) ;
//        //FmtLog.info(log, "dump %s", idx);
//
//        int N = idx.getIndexName().length() ;
//        String primaryIndex = IndexLib.choosePrimary(idx) ;
//
//        if ( ! idx.exists() )
//            throw new CmdException("No such index: "+idx) ;
//        
//        TupleIndex tupleIndex = IndexLib.connect(idx, primaryIndex) ;
//
//        IndexLib.dumpTupleIndex(tupleIndex) ;
////        System.out.println() ;
////        TupleIndexRecord tupleIndexRecord = (TupleIndexRecord)tupleIndex ;
////        IndexLib.dumpRangeIndex(tupleIndexRecord.getRangeIndex()) ;
    }

    private static TupleIndex find(TupleIndex[] indexes, String srcIndex) {
        for ( TupleIndex idx : indexes ) {
            // Index named by simple "POS"
            if ( idx.getName().equals(srcIndex) )
                return idx ;

            // Index named by column mapping "SPO->POS"
            // This is silly.
            int i = idx.getColumnMap().getLabel().indexOf('>') ;
            String name = idx.getMapping().substring(i + 1) ;
            if ( name.equals(srcIndex) )
                return idx ;
        }
        return null ;
    }
}
