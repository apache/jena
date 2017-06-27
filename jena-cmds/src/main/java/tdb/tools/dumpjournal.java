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

package tdb.tools ;

import java.io.PrintStream ;

import arq.cmdline.CmdARQ;
import jena.cmd.CmdException ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.transaction.Journal ;
import org.apache.jena.tdb.transaction.JournalControl ;
import tdb.cmdline.ModLocation ;

public class dumpjournal extends CmdARQ {
    ModLocation modLocation = new ModLocation() ;

    static public void main(String... argv) {
        LogCtl.setLog4j() ;
        new dumpjournal(argv).mainRun() ;
    }

    protected dumpjournal(String[] argv) {
        super(argv) ;
        super.addModule(modLocation) ;
    }

    @Override
    protected void exec() {
        Location loc = modLocation.getLocation() ;
        Journal journal =  determineJournal(loc);
        dump(System.out, journal) ;
    }
    
    private void dump(PrintStream out, Journal journal) {
        JournalControl.print(journal);
    }

    private Journal determineJournal(Location loc) {
        // Directly open the jounral.
        if ( ! Journal.exists(loc) )
            throw new CmdException("No journal file in "+loc);
        return Journal.create(loc); 
    }

    @Override
    protected void processModulesAndArgs() {
        if ( modVersion.getVersionFlag() )
            modVersion.printVersionAndExit() ;
        if ( modLocation.getLocation() == null )
            cmdError("Location required") ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " --loc=DIR IndexName" ;
    }

    @Override
    protected String getCommandName() {
        return Lib.className(this) ;
    }

}
