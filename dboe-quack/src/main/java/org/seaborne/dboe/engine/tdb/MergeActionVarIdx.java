/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine.tdb;

import com.hp.hpl.jena.sparql.core.Var ;

/** One-sided merge into an existing stream */
public class MergeActionVarIdx
{
    private Var var ;
    private IndexAccess indexAccess ;
    
    public MergeActionVarIdx(Var var, IndexAccess indexAccess)
    {
        this.var = var ;
        this.indexAccess = indexAccess ;
    }

    public Var getVar()
    {
        return var ;
    }

    public IndexAccess getIndexAccess()
    {
        return indexAccess ;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder() ;
        builder.append("Merge [") ;
        builder.append(var) ;
        builder.append(",") ;
        builder.append(indexAccess) ;
        builder.append("]") ;
        return builder.toString() ;
    }
}

