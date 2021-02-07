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

package tdb.bulkloader2;

import java.util.Arrays ;
import java.util.List ;
import java.util.Objects ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.setup.DatasetBuilderStd ;
import org.apache.jena.tdb.store.bulkloader2.ProcNodeTableBuilder ;
import tdb.cmdline.CmdTDB ;

/** Build node table - write triples/quads as text file */
public class CmdNodeTableBuilder extends CmdGeneral
{
    static {
        LogCtl.setLogging();
        JenaSystem.init();
    }

    private static ArgDecl argLocation   = new ArgDecl(ArgDecl.HasValue, "loc", "location");
    private static ArgDecl argTriplesOut = new ArgDecl(ArgDecl.HasValue, "triples");
    private static ArgDecl argQuadsOut   = new ArgDecl(ArgDecl.HasValue, "quads");
    private static ArgDecl argNoStats    = new ArgDecl(ArgDecl.NoValue, "nostats");

    private String         locationString;
    private String         dataFileTriples;
    private String         dataFileQuads;
    private List<String>   datafiles;
    private Location       location;
    private boolean        collectStats  = true;

    public static void main(String... argv) {
        CmdTDB.init();
        DatasetBuilderStd.setOptimizerWarningFlag(false);
        new CmdNodeTableBuilder(argv).mainRun();
    }

    public CmdNodeTableBuilder(String... argv) {
        super(argv);
        super.add(argLocation, "--loc", "Location");
        super.add(argTriplesOut, "--triples", "Output file for triples");
        super.add(argQuadsOut, "--quads", "Output file for quads");
        super.add(argNoStats, "--nostats", "Don't collect stats");
    }
        
    @Override
    protected void processModulesAndArgs() {
        if ( !super.contains(argLocation) ) throw new CmdException("Required: --loc DIR") ;
//        if ( !super.contains(argTriplesOut) ) throw new CmdException("Required: --triples FILE") ;
//        if ( !super.contains(argQuadsOut) ) throw new CmdException("Required: --quads FILE") ;
        
        locationString   = super.getValue(argLocation) ;
        location = Location.create(locationString) ;

        dataFileTriples  = super.getValue(argTriplesOut) ;
        if ( dataFileTriples == null )
            dataFileTriples = location.getPath("triples", "tmp") ;
        
        dataFileQuads    = super.getValue(argQuadsOut) ;
        if ( dataFileQuads == null )
            dataFileQuads = location.getPath("quads", "tmp") ;
        
        if ( Objects.equals(dataFileTriples, dataFileQuads) )
            cmdError("Triples and Quads work files are the same") ;
        
        if ( super.contains(argNoStats) )
            collectStats = false ;
        
        //datafiles  = getPositionalOrStdin() ;
        datafiles  = getPositional() ;
        if ( datafiles.isEmpty() )
            datafiles = Arrays.asList("-") ;
        
        // ---- Checking.
        for ( String filename : datafiles ) {
            Lang lang = RDFLanguages.filenameToLang(filename, RDFLanguages.NQUADS);
            if ( lang == null )
                               // Does not happen due to default above.
                               cmdError("File suffix not recognized: " + filename);
            if ( !filename.equals("-") && !FileOps.exists(filename) )
                cmdError("File does not exist: " + filename);
        }
    }

    @Override
    protected void exec() {
        ProcNodeTableBuilder.exec(location, dataFileTriples, dataFileQuads, datafiles, collectStats);
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " --loc=DIR [--triples=tmpFile1] [--quads=tmpFile2] FILE ...";
    }

    @Override
    protected String getCommandName() {
        return this.getClass().getName();
    }
}
