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

import arq.cmdline.ArgDecl ;
import arq.cmdline.ArgModuleGeneral ;
import arq.cmdline.CmdArgModule ;
import arq.cmdline.CmdGeneral ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.util.FileManager ;

/** Name a model */
public class ModModel implements ArgModuleGeneral
{
    protected ArgDecl modelArgDecl = null ;
    private Model model = null ;
    
    //public ModModel() { this("model") ; }
    public ModModel(String argName, String ... altNames)
    {
        modelArgDecl = new ArgDecl(ArgDecl.HasValue, argName) ;
        for ( String x : altNames )
            modelArgDecl.addName(x) ;
    }

    public ArgDecl getArg() 
    {
        return modelArgDecl ;
    }
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.add(modelArgDecl, "--"+modelArgDecl.getKeyName()+"=filename", "Filename for a model") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(modelArgDecl) )
        {
            String filename = cmdLine.getValue(modelArgDecl) ;
            model = FileManager.get().loadModel(filename) ;
        }
    }
    
    public Model getModel() { return model ; }
    
}
