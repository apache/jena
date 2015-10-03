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

package sdb.cmd;

import jena.cmd.ArgDecl;
import jena.cmd.CmdArgModule;
import jena.cmd.CmdGeneral;
import jena.cmd.TerminationException;

import org.apache.jena.query.Dataset ;
import org.apache.jena.sdb.SDBFactory ;

import arq.cmdline.ModDataset;

public class ModDatasetStore extends ModDataset
{
    protected final ArgDecl argDeclSDBdesc       = new ArgDecl(true, "sdb", "store", "desc");
    protected String filename = null ;
    
    @Override
    public Dataset createDataset()
    {
        return SDBFactory.connectDataset(filename) ;
    }

    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.add(argDeclSDBdesc, "--store=FILE", "Dataset assembler for an SDB store") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        // Either a dataset or a store.
        
        if (! cmdLine.contains(argDeclSDBdesc))
        {
            System.err.println("No store description");
            throw new TerminationException(1);
        }
        filename = cmdLine.getValue(argDeclSDBdesc) ;
    }

}
