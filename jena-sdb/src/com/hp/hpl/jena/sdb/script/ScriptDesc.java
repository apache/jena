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

package com.hp.hpl.jena.sdb.script;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;

/** Java description a script : the assmbler build one of these */ 

public class ScriptDesc
{
    List<CmdDesc> steps = new ArrayList<CmdDesc>() ;
    
    public static ScriptDesc read(String filename)
    {
        AssemblerVocab.init() ;
        Model m = FileManager.get().loadModel(filename) ;
        
        return worker(m) ;
    }
    
    public static void run(String filename)
    {
        ScriptDesc desc = ScriptDesc.read(filename) ;
//        System.out.println(desc) ;
//        try {
//            String cmd = desc.getCmd() ;
//            Class c = Class.forName(cmd) ;
//            Method m = c.getMethod("main", new Class[]{String[].class}) ;
//            m.invoke(null, new Object[]{desc.asStringArray()}) ;
//        } catch (Exception ex) { ex.printStackTrace(System.err) ; }
    }
    
    public void add(CmdDesc step) { steps.add(step) ; }
    public List<CmdDesc> getSteps() { return steps ; } // Temp
    
    private static ScriptDesc worker(Model m)
    {
        Resource r = GraphUtils.getResourceByType(m, ScriptVocab.ScriptType) ;
        if ( r == null )
            throw new SDBException("Can't find command line description") ;
        return (ScriptDesc)AssemblerBase.general.open(r) ;
    }
    
}
