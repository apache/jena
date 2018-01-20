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

package tdb2;

import arq.cmdline.ModLangOutput ;
import jena.cmd.CmdException ;
import org.apache.jena.system.Txn;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.sparql.core.DatasetGraph ;
import tdb2.cmdline.CmdTDB;

public class tdbdump extends CmdTDB
{
    static ModLangOutput modLangOutput = new ModLangOutput() ;
    
    static public void main(String... argv) {
        CmdTDB.init() ;
        new tdbdump(argv).mainRun() ;
    }

    protected tdbdump(String[] argv) {
        super(argv) ;
        addModule(modLangOutput) ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " : Write a dataset to stdout (defaults to N-Quads)" ;
    }

    @Override
    protected void exec() {
        DatasetGraph dsg = getDatasetGraph() ;
        // Prefer stream over fully pretty output formats.
        RDFFormat fmt = modLangOutput.getOutputStreamFormat() ;
        // Stream writing happens naturally - no need to call StreamRDFWriter.
        //if ( fmt != null && StreamRDFWriter.registered(fmt) )
        if ( fmt == null )
            fmt = modLangOutput.getOutputFormatted() ;
        if ( fmt == null )
            // Default.
            fmt = RDFFormat.NQUADS ;
        if ( ! RDFLanguages.isQuads(fmt.getLang() ))
            throw new CmdException("Databases can be dumped only in quad formats (e.g. Trig, N-Quads), not "+fmt.getLang()) ;
        RDFFormat fmtFinal = fmt ;
        Txn.executeRead(dsg, ()->RDFDataMgr.write(System.out, dsg, fmtFinal));
    }
}
