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

package org.apache.jena.tdb.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue ;
import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue ;
import static org.apache.jena.tdb.assembler.VocabTDB.pName ;
import static org.apache.jena.tdb.assembler.VocabTDB.pSetting ;
import static org.apache.jena.tdb.assembler.VocabTDB.pValue ;
import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.util.Symbol ;

public class SettingAssembler //extends DatasetAssembler
{
    public SettingAssembler() {}
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
