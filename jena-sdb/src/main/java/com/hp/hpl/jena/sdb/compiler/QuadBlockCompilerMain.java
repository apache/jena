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

package com.hp.hpl.jena.sdb.compiler;

import com.hp.hpl.jena.sparql.core.Quad;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;

public class QuadBlockCompilerMain implements QuadBlockCompiler
{
    SDBRequest request ;
    SlotCompiler slotCompiler ;
    static public PatternTable patternTable = null ;

    public QuadBlockCompilerMain(SDBRequest request, SlotCompiler slotCompiler)
    {
        //super(request, slotCompiler) ;
        this.request = request ;
        this.slotCompiler = slotCompiler ;
    }
    
    @Override
    public SlotCompiler getSlotCompiler()
    { return slotCompiler ; }

    //@Override
    @Override
    final
    public SqlNode compile(QuadBlock quads)
    {
        SqlNode sqlNode = slotCompiler.start(quads) ;
        quads = new QuadBlock(quads) ;          // Copy it because it's modified.

        // ---- Stage builder 
        SqlStageList sList = new SqlStageList() ;
        // Potential concurrent modification - need to use an explicit index.
        for ( int i = 0 ; i < quads.size() ; )
        {
            Quad q = quads.get(i) ;
            if ( patternTable != null && patternTable.trigger(q) )
            {
                // Removes current quad
                SqlStage stage = patternTable.process(i, quads) ;
                if ( stage != null )
                {
                    if ( quads.get(i) == q )
                        throw new SDBInternalError("Pattern table returned a stage but did not remove the first quad") ;
                    sList.add(stage) ;
                    continue ;
                }
            }
            sList.add(new SqlStageBasicQuad(q)) ; 
            i++ ;
        }

        // ---- and now turn the stages into SqlNodes  
        SqlNode sqlStages = sList.build(request, slotCompiler) ;
        
        // --- Join the initial node (constants). 
        sqlNode = SqlBuilder.innerJoin(request, sqlNode, sqlStages) ;
        sqlNode = slotCompiler.finish(sqlNode, quads) ;
        
        // Insert DISTINCT if accessing the RDF merge of all named graphs
        // An RDF Merge is the DISTINCT results of query over the union of all graphs.
        // Or in TransformSDB

        boolean needDistinct = false ;
        // Either it's the uniongraph ...
        if ( quads.getGraphNode().equals(Quad.unionGraph) )
            needDistinct = true ;
        // Or it's the union graph via redirected defaultGraph
        else if ( Quad.isDefaultGraphGenerated(quads.getGraphNode()) &&
                  request.getContext().isTrue(SDB.unionDefaultGraph))
            needDistinct = true ;
        
        if ( needDistinct )
        {
            // DISTINCT -- over the named variables but not * (which includes the graph node).
            String renameName = request.genId("A") ;
            //sqlNode = SqlRename.view(renameName, sqlNode) ;
            sqlNode = SqlBuilder.view(request, sqlNode) ;
            sqlNode = SqlBuilder.distinct(request, sqlNode) ;
        }
        
        return sqlNode ;
    }
}
