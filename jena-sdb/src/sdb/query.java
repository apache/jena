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

package sdb;

import sdb.cmd.ModDatasetStore;
import arq.cmdline.ModDataset;

import com.hp.hpl.jena.sparql.util.Utils;

public class query extends arq.query
{
    protected ModDatasetStore modDatasetStore = new ModDatasetStore() ;

    public static void main (String... argv)
    {
        new query(argv).mainRun() ;
    }
    
    public query(String[] argv)
    {
        super(argv) ;
    }
        
    @Override
    protected ModDataset setModDataset()
    {
        return new ModDatasetStore() ;
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --sdb=FILE --query=<query>" ; }

}
