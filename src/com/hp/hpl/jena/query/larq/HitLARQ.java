/**
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

package com.hp.hpl.jena.query.larq;

import org.apache.lucene.search.Hit ;

import com.hp.hpl.jena.graph.Node ;

public class HitLARQ
{
    protected Node node ;
    protected float score ;
    protected int docId ;

    public HitLARQ(Hit hit)
    {
        try {
            node = LARQ.build(hit.getDocument()) ;
            score = hit.getScore() ;
            docId = hit.getId() ;
        }
        catch (Exception e)
        { throw new ARQLuceneException("node conversion error", e) ; }
    }

    public Node getNode()
    {
        return node ;
    }

    public float getScore()
    {
        return score ;
    }
    
    public int getLuceneDocId()
    {
        return docId ;
    }
}
