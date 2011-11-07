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

package com.hp.hpl.jena.sdb.assembler;


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.query.QueryFactory;


public class QueryAssembler extends AssemblerBase implements Assembler
{
    @Override
    public Object open(Assembler a, Resource cmd, Mode mode)
    {
        if ( cmd.hasProperty(AssemblerVocab.pQueryFile) && cmd.hasProperty(AssemblerVocab.pQueryString) )
            throw new CommandAssemblerException(cmd, "Both "+FmtUtils.stringForResource(AssemblerVocab.pQueryString)+" and "+FmtUtils.stringForResource(AssemblerVocab.pQueryFile)+" present") ;
        if ( ! cmd.hasProperty(AssemblerVocab.pQueryFile) && ! cmd.hasProperty(AssemblerVocab.pQueryString) )
            throw new CommandAssemblerException(cmd, "No queryString or queryFile present") ;
        
        String queryString = GraphUtils.getStringValue(cmd, AssemblerVocab.pQueryString) ;
        if ( queryString == null )
        {
            String filename = GraphUtils.getStringValue(cmd, AssemblerVocab.pQueryFile) ;
            queryString = FileManager.get().readWholeFileAsUTF8(filename) ;
        }
        // Base??
        // Convert to another exception?
        return QueryFactory.create(queryString) ;
    }
}
