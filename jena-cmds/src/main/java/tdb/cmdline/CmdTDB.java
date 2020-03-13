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

import arq.cmdline.CmdARQ ;
import org.apache.jena.Jena ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.setup.DatasetBuilderStd ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.sys.TDBInternal ;

public abstract class CmdTDB extends CmdARQ
{
    protected final ModTDBDataset tdbDatasetAssembler   = new ModTDBDataset() ;

    private static boolean initialized = false ;
    
    protected CmdTDB(String[] argv) {
        super(argv) ;
        init() ;
        super.addModule(tdbDatasetAssembler) ;
        super.modVersion.addClass(Jena.class) ;
        super.modVersion.addClass(ARQ.class) ;
        super.modVersion.addClass(TDB.class) ;
    }

    public static synchronized void init() {
        // In case called from elsewhere.
        JenaSystem.init() ;
        if (initialized)
            return ;
        // attempt once.
        initialized = true ;
        DatasetBuilderStd.setOptimizerWarningFlag(false) ;
    }

    @Override
    protected void processModulesAndArgs() {
        super.processModulesAndArgs() ;
    }

    protected Location getLocation() {
        return tdbDatasetAssembler.getLocation() ;
    }

    protected DatasetGraph getDatasetGraph() {
        return getDataset().asDatasetGraph() ;
    }

    protected DatasetGraphTDB getDatasetGraphTDB() {
        DatasetGraph dsg = getDatasetGraph() ;
        return TDBInternal.getBaseDatasetGraphTDB(dsg) ;
    }

    protected Dataset getDataset() {
        return tdbDatasetAssembler.getDataset() ;
    }

    @Override
    protected String getCommandName() {
        return Lib.className(this) ;
    }
}
