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

package tdb;

import org.openjena.atlas.logging.Log ;
import tdb.cmdline.CmdTDBGraph ;

import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.solver.stats.Stats ;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollector ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;


public class tdbstats extends CmdTDBGraph
{
    // tdbconfig?
    static public void main(String... argv)
    { 
        TDB.setOptimizerWarningFlag(false) ;
        Log.setLog4j() ;
        new tdbstats(argv).mainRun() ;
    }

    protected tdbstats(String[] argv)
    {
        super(argv) ;
    }
    
    @Override
    protected String getSummary()
    {
        return null ;
    }

    @Override
    protected void exec()
    {
        GraphTDB graph = getGraph() ;
        StatsCollector stats = Stats.gatherTDB(graph) ;
        Stats.write(System.out, stats) ;
    }
}
