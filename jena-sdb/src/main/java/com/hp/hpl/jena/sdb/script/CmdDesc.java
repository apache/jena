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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import arq.cmdline.CmdLineArgs;

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.util.Pair;

/** Java description of a command - class and arguments. The assmbler build one of these */

public class CmdDesc
{
    String classname = null ;
    List<Pair<String, String>> namedArgs = new ArrayList<Pair<String, String>>() ;
    List<String> posnArgs  = new ArrayList<String>() ;
    
    public static CmdDesc read(String filename)
    {
        ScriptVocab.init() ;
        Model m = FileManager.get().loadModel(filename) ;
        
        return worker(m) ;
    }
    
    public static void run(String filename)
    {
        CmdDesc desc = CmdDesc.read(filename) ;
        System.out.println(desc) ;
        try {
            String cmd = desc.getCmd() ;
            Class<?> c = Class.forName(cmd) ;
            Method m = c.getMethod("mainNoExit", new Class[]{String[].class}) ;
            m.invoke(null, new Object[]{desc.asStringArray()}) ;
        } catch (Exception ex) { ex.printStackTrace(System.err) ; }
    }
    
    private static CmdDesc worker(Model m)
    {
        Resource r = GraphUtils.getResourceByType(m, ScriptVocab.CommandLineType) ;
        if ( r == null )
            throw new SDBException("Can't find command line description") ;
        return (CmdDesc)AssemblerBase.general.open(r) ;
    }
    
    public void addNamedArg(String name, String value)
    { namedArgs.add(new Pair<String, String>(name, value)) ; }
    
    public void addNamedArg(String name) { addNamedArg(name, null) ; }

    public void addPosn(String x) { posnArgs.add(x) ; }

    public String getCmd() { return classname ; } 

    public void setCmd(String cname)
    { classname = cname ; }
    
    public void set(CmdLineArgs cmdLineArgs)
    {
        for ( Pair<String, String> p : namedArgs )
            cmdLineArgs.addArg(p.car(), p.cdr()) ;
        for ( String a : posnArgs )
            cmdLineArgs.addPositional(a) ;
    }
    
    public String[] asStringArray()
    {
        List<String> x = new ArrayList<String>() ;
        for ( Pair<String, String> p : namedArgs )
        {
            x.add(p.car()) ;
            x.add(p.cdr()) ;
        }
        
        for ( String a : posnArgs )
            x.add(a) ;

        return x.toArray(new String[x.size()]) ;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        sb.append(getCmd()) ;
        String sep = " " ;
        for ( Pair<String, String> p : namedArgs )
        {
            sb.append(sep).append(p.car()).append("=").append(p.cdr());
            sep = " " ;
        }
        for ( String a : posnArgs )
        {
            sb.append(sep).append(a) ;
            sep = " " ;
        }
        return sb.toString() ;
    }
}
