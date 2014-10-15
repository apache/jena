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

package tdb;

import tdb.cmdline.CmdTDB ;

import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.transaction.JournalControl ;

public class tdbrecovery extends CmdTDB
{
    static public void main(String... argv)
    { 
        CmdTDB.init() ;
        TDB.setOptimizerWarningFlag(false) ;
        new tdbrecovery(argv).mainRun() ;
    }

    protected tdbrecovery(String[] argv)
    {
        super(argv) ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" --loc DIRECTORY\nRun database journal recovery." ;
    }

    @Override
    protected void exec()
    {
        DatasetGraphTDB dsg = super.getDatasetGraphTDB() ;
        // Just creating the DSG does a recovery so this is not (currently) necessary:
        // This may change (not immediately recovering on start up).
        JournalControl.recovery(dsg) ;
    }
}
