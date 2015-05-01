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

import java.util.Objects ;

import arq.cmd.ArgDecl ;
import arq.cmd.CmdException ;
import arq.cmdline.CmdARQ ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.riot.RIOT ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.store.tupletable.TupleIndex ;
import org.apache.jena.tdb.sys.Names ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Copy a TDB index.
 * <pre>
     tdbindexcopy location1[name1] location2[name2]
     tdbindexcopy --loc location name1 name2 
   </pre>  
 */
public class tdbindexcopy extends CmdARQ {
    static { LogCtl.setCmdLogging(); }
    static Logger log = LoggerFactory.getLogger(tdbindexcopy.class) ;
    
    static final ArgDecl argLocation = new ArgDecl(true, "loc", "location") ;
    
    static public void main(String... argv) {
        TDB.setOptimizerWarningFlag(false) ;
        new tdbindexcopy(argv).mainRun() ;
    }

    protected tdbindexcopy(String[] argv) {
        super(argv) ;
        super.modVersion.addClass(ARQ.class) ;
        super.modVersion.addClass(RIOT.class) ;
        super.modVersion.addClass(TDB.class) ;
        super.add(argLocation) ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + "[ -loc DIR srcIndex] destIndex |  location1/name1 location2/name2 ]" ;
    }
    
    IndexRef idx1 = null ;
    IndexRef idx2 = null ;
    
    @Override
    protected void processModulesAndArgs() {
        if ( super.getNumPositional() != 2 )
            throw new CmdException("location1/name1 location2/name2 or --loc= name1 name2") ;
        
        String x1 = super.getPositionalArg(0) ;
        String x2 = super.getPositionalArg(1) ;
        
        if ( super.contains(argLocation) ) {
            Location loc = Location.create(super.getValue(argLocation)) ;
            idx1 = new IndexRef(loc, x1) ;
            idx2 = new IndexRef(loc, x2) ;
        } else {
            idx1 = IndexRef.parse(x1) ;
            idx2 = IndexRef.parse(x2) ;
        }
        
        if ( Objects.equals(idx1, idx2) )
            throw new CmdException("Can't copy an index to itself: "+idx1+" -> "+idx2);
        
        if ( idx1.getIndexName().length() != idx2.getIndexName().length() ) {
            throw new CmdException("Indexes must the same lengh : "+idx1.getIndexName()+" -> "+idx2.getIndexName());
        }
        int N = idx1.getIndexName().length() ;
        if ( N != 3 && N != 4 )
            throw new CmdException("Indexes must 3 or 4 in length : "+idx1.getIndexName()+" -> "+idx2.getIndexName());
        
        if ( ! idx1.exists() )
            throw new CmdException("No such index: "+ idx1) ;
        if ( idx2.exists() )
            throw new CmdException("Already exists: "+ idx2+ " (delete first to copy)" ) ;
    }
    
    @Override
    protected void exec() {
        if ( idx1 == null || idx2 == null )
            throw new CmdException("Null index: "+idx1+", "+idx2) ;

        FmtLog.info(log, "copy %s -> %s", idx1, idx2);

        String srcIndexName = idx1.getIndexName() ;
        String destIndexName = idx2.getIndexName() ;
        
        int N = idx1.getIndexName().length() ;
        String primaryIndexName ;

        if ( N == 3 )
            primaryIndexName = Names.primaryIndexTriples ;
        else if ( N == 4 )         
            primaryIndexName = Names.primaryIndexQuads ;
        else
            throw new InternalErrorException("Index length") ;
        
        srcIndexName = srcIndexName.toUpperCase() ;
        destIndexName = destIndexName.toUpperCase() ;
        
        Location location1 = idx1.getLocation() ;
        Location location2 = idx2.getLocation() ;

        TupleIndex srcIndex = IndexLib.connect(location1, primaryIndexName, srcIndexName) ;
        TupleIndex destIndex = IndexLib.connect(location2, primaryIndexName, destIndexName) ;
        IndexLib.copyIndex(log, srcIndex, destIndex) ;
    }
}
