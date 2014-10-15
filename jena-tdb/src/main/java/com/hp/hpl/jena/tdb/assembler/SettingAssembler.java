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

package com.hp.hpl.jena.tdb.assembler;

import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.getAsStringValue ;
import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.getResourceValue ;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pName ;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pSetting ;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pValue ;
import tdb.cmdline.CmdTDB ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class SettingAssembler //extends DatasetAssembler
{
    static { CmdTDB.init() ; }
    
    /* 
     *  :setting [ :name tdbsym:name ; :value "SPO.idx" ]
     */
    
    //@Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        Resource r = getResourceValue(root, pSetting ) ;
        String k = getAsStringValue(r, pName) ;
        String v = getAsStringValue(r, pValue) ;
        Symbol symbol = Symbol.create(k) ;
        ARQ.getContext().set(symbol, v) ;
        return r ;
    }
}
