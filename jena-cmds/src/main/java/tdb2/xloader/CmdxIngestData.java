/*
6 * Licensed to the Apache Software Foundation (ASF) under one
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

package tdb2.xloader;

import java.util.Arrays;
import java.util.Objects;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.tdb2.xloader.ProcIngestDataX;

public class CmdxIngestData extends AbstractCmdxLoad {

    private static ArgDecl argTriplesOut = new ArgDecl(ArgDecl.HasValue, "triples");
    private static ArgDecl argQuadsOut   = new ArgDecl(ArgDecl.HasValue, "quads");

    private String dataFileTriples;
    private String dataFileQuads;
    private boolean collectStats = false;

    public static void main(String... args) {
        new CmdxIngestData(args).mainRun();
    }

    protected CmdxIngestData(String[] argv) {
        super("Data", argv);
        super.add(argTriplesOut);
        super.add(argQuadsOut);
    }

    @Override
    protected void setCmdArgs() {
        super.add(argLocation,   "--loc=",     "Database location");
        super.add(argTmpdir,     "--tmpdir=",  "Temporary directory (defaults to --loc)");
//        super.add(argTriplesOut, "--triples=", "Triples temporary file");
//        super.add(argQuadsOut,   "--quads=",   "Quads temporary file");
    }

    @Override
    protected String getCommandName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    protected String getSummary() {
        return getCommandName()+" "+getArgsSummary();
    }

    @Override
    protected String getArgsSummary() {
        return super.getArgsSummary();
    }

    @Override
    protected void subCheckArgs() {}

    @Override
    protected void processModulesAndArgs() {
        if ( !super.contains(argLocation) ) throw new CmdException("Required: --loc DIR") ;
//        if ( !super.contains(argTriplesOut) ) throw new CmdException("Required: --triples FILE") ;
//        if ( !super.contains(argQuadsOut) ) throw new CmdException("Required: --quads FILE") ;

        super.processModulesAndArgs();

        Location tmp = Location.create(tmpdir);

        dataFileTriples = super.getValue(argTriplesOut);
        if ( dataFileTriples == null )
            dataFileTriples = tmp.getPath("triples", "tmp");

        dataFileQuads = super.getValue(argQuadsOut);
        if ( dataFileQuads == null )
            dataFileQuads = tmp.getPath("quads", "tmp");

        if ( Objects.equals(dataFileTriples, dataFileQuads) )
            cmdError("Triples and Quads work files are the same");

//        if ( super.contains(argNoStats) )
//            collectStats = false ;

        if ( filenames.isEmpty() )
            filenames = Arrays.asList("-");

        // ---- Checking.
        for ( String filename : filenames ) {
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
        ProcIngestDataX.exec(location, loaderFiles, filenames, collectStats);
    }
}
