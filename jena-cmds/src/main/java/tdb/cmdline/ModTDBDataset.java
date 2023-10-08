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

package tdb.cmdline;

import java.util.ArrayList;
import java.util.List;

import arq.cmdline.ModDataset;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb1.assembler.VocabTDB;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.transaction.DatasetGraphTransaction;

public class ModTDBDataset extends ModDataset {
    // Mixes assembler, location and "tdb"
    // Can make a single model or a dataset
    
    private ArgDecl argMem                  = new ArgDecl(ArgDecl.HasValue, "mem", "data") ;
    private ModTDBAssembler modAssembler    = new ModTDBAssembler() ;
    private String inMemFile                = null ;
    
    public ModTDBDataset() {}

    @Override
    public void registerWith(CmdGeneral cmdLine) {
        cmdLine.add(argMem, "--mem=FILE", "Execute on an in-memory TDB database (for testing)");
        cmdLine.addModule(modAssembler);
    }

    @Override
    public void processArgs(CmdArgModule cmdLine) {
        inMemFile = cmdLine.getValue(argMem);
        modAssembler.processArgs(cmdLine);
    }

    @Override
    public Dataset createDataset() {
        if ( inMemFile != null ) {
            Dataset ds = TDB1Factory.createDataset();
            RDFDataMgr.read(ds, inMemFile);
            return ds;

        }

        if ( modAssembler.getAssemblerFile() != null ) {
            Dataset thing = null;
            // Two variants: plain dataset with a TDB graph or a TDB dataset.
            try {
                thing = (Dataset)AssemblerUtils.build(modAssembler.getAssemblerFile(), VocabTDB.tDatasetTDB);
                if ( thing != null && !(thing.asDatasetGraph() instanceof DatasetGraphTransaction) )
                    Log.warn(this, "Unexpected: Not a TDB dataset for type DatasetTDB");

                if ( thing == null )
                    // Should use assembler inheritance but how do we assert the
                    // subclass relationship in a program?
                    thing = (Dataset)AssemblerUtils.build(modAssembler.getAssemblerFile(), DatasetAssemblerVocab.tDataset);
            } catch (JenaException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new CmdException("Error creating", ex);
            }
            return thing;
        }

        if ( modAssembler.getLocation() == null )
            throw new CmdException("No assembler file nor location provided");

        // No assembler - use location to find a database.
        Dataset ds = TDB1Factory.createDataset(modAssembler.getLocation());
        return ds;
    }

    public Location getLocation() {
        List<String> x = locations();
        if ( x.size() == 0 )
            return null;
        return Location.create(x.get(0));
    }

    public List<String> locations() {
        List<String> locations = new ArrayList<>();

        if ( modAssembler.getLocation() != null )
            locations.add(modAssembler.getLocation().getDirectoryPath());

        // Extract the location from the assembler file.
        if ( modAssembler.getAssemblerFile() != null ) {
            // Find and clear all locations
            Model m = RDFDataMgr.loadModel(modAssembler.getAssemblerFile());
            Query query = QueryFactory.create("PREFIX tdb:     <http://jena.hpl.hp.com/2008/tdb#> SELECT ?dir { [] tdb:location ?dir FILTER (isURI(?dir)) }");
            try (QueryExecution qExec = QueryExecutionFactory.create(query, m)) {
                for ( ResultSet rs = qExec.execSelect() ; rs.hasNext() ; ) {
                    String x = rs.nextSolution().getResource("dir").getURI();
                    locations.add(x);
                }
            }
        }

        return locations;
    }
}
