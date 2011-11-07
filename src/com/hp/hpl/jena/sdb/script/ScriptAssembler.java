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


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;

//EXPERIMENTAL - Move to ARQ?

public class ScriptAssembler extends AssemblerBase implements Assembler
{
    // A script is a number of command descriptions (CmdDesc)
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        ScriptDesc sd = new ScriptDesc() ;
        Resource x = GraphUtils.getResourceValue(root, AssemblerVocab.pSteps) ;
        if ( x != null )
        {
            for (; !x.equals(RDF.nil); )
            {
                Resource e = x.getRequiredProperty(RDF.first).getResource();
                // Move to next list item
                x = x.getRequiredProperty(RDF.rest).getResource();
                // Process this item.
                try {
                    CmdDesc cd = (CmdDesc)a.open(e) ;
                    sd.add(cd) ;
                } catch (ClassCastException ex)
                {
                    System.err.println("Not a command description : "+ex.getMessage()) ;
                }
            }
        }
        return sd ;
    }

}
