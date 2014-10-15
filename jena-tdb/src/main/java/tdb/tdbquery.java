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
import tdb.cmdline.ModTDBDataset ;
import arq.cmdline.ModDataset ;

import com.hp.hpl.jena.tdb.TDB ;


public class tdbquery extends arq.query
{
    // Inherits from arq.query so is not a CmdTDB.  Mixins for Java!
    public static void main(String...argv)
    {
        CmdTDB.init() ;
        new tdbquery(argv).mainRun() ;
    }
    
    public tdbquery(String[] argv)
    {
        super(argv) ;
        // Because this inherits from an ARQ command
        super.modVersion.addClass(TDB.class) ;
    }

    @Override
    protected String getSummary() 
    { 
        return getCommandName()+" --loc=<path> --query=<query>" ; 
    }
    
    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
    }
    
    @Override
    protected ModDataset setModDataset()
    {
        return new ModTDBDataset() ;
    }
}
