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

package com.hp.hpl.jena.sdb.compiler.rewrite;

import com.hp.hpl.jena.sdb.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.compiler.QuadBlockCompiler;
import com.hp.hpl.jena.sdb.compiler.SlotCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;

public class QuadBlockRewriteCompiler implements QuadBlockCompiler
{
    QuadBlockRewrite qbr1 = new QBR_SubType() ;
    QuadBlockRewrite qbr2 = new QBR_SubProperty() ;
    private SDBRequest request ;
    private QuadBlockCompiler baseCompiler ;
    
    public QuadBlockRewriteCompiler(SDBRequest request, QuadBlockCompiler baseCompiler)
    {
        this.request = request ;
        this.baseCompiler = baseCompiler ;
    }
    
    @Override
    public SqlNode compile(QuadBlock quads)
    {
        quads = qbr1.rewrite(request, quads) ;
        if ( false )
            quads = qbr2.rewrite(request, quads) ;
        return baseCompiler.compile(quads) ;
        
    }

    @Override
    public SlotCompiler getSlotCompiler()
    { return baseCompiler.getSlotCompiler() ; }

}
