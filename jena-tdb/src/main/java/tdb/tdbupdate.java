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
import tdb.cmdline.ModTDBGraphStore ;
import arq.cmdline.ModGraphStore ;

import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.transaction.TransactionManager ;

public class tdbupdate extends arq.update
{
    // Inherits from arq.update so is not a CmdTDB.  Mixins for Java!
    public static void main(String...argv)
    {
        CmdTDB.init() ;
        // Do everything with flushing transactions.
        TransactionManager.QueueBatchSize = 0 ;
        new tdbupdate(argv).mainRun() ;
    }
    
    public tdbupdate(String[] argv)
    {
        super(argv) ;
        // Because this inherits from an ARQ command
        CmdTDB.init() ;
        super.modVersion.addClass(TDB.class) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
    }
    
    @Override
    protected ModGraphStore setModGraphStore()
    {
        return new ModTDBGraphStore();
    }
    
    
}
