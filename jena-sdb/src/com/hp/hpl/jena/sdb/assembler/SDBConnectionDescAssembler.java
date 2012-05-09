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
import com.hp.hpl.jena.vocabulary.RDFS;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.shared.Access;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;

public class SDBConnectionDescAssembler extends AssemblerBase implements Assembler
{

    @Override
    public SDBConnectionDesc open(Assembler a, Resource root, Mode mode)
    {
        SDBConnectionDesc sDesc = SDBConnectionDesc.blank() ;
        
        sDesc.setType(     GraphUtils.getStringValue(root, AssemblerVocab.pSDBtype) ) ;
        sDesc.setHost(     GraphUtils.getStringValue(root, AssemblerVocab.pSDBhost) ) ;
        sDesc.setName(     GraphUtils.getStringValue(root, AssemblerVocab.pSDBname) ) ;
        sDesc.setUser(     GraphUtils.getStringValue(root, AssemblerVocab.pSDBuser) ) ;
        sDesc.setPassword( GraphUtils.getStringValue(root, AssemblerVocab.pSDBpassword) ) ;
        sDesc.setDriver(   GraphUtils.getStringValue(root, AssemblerVocab.pDriver) ) ;
        sDesc.setJdbcURL(  GraphUtils.getStringValue(root, AssemblerVocab.pJDBC) ) ;
        sDesc.setPoolSize( GraphUtils.getStringValue(root, AssemblerVocab.pPoolSize) ) ;
        sDesc.setLabel(    GraphUtils.getStringValue(root, RDFS.label) ) ;
        
        if ( sDesc.getUser() == null )
            sDesc.setUser(Access.getUser()) ;
        if ( sDesc.getPassword() == null )
            sDesc.setPassword(Access.getPassword()) ;
        return sDesc ;
    }
}
