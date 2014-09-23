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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpPath ;
import com.hp.hpl.jena.sparql.core.PathBlock ;
import com.hp.hpl.jena.sparql.path.PathCompiler ;
import com.hp.hpl.jena.sparql.path.PathLib ;

/**
 * Perform the equivalent of the transactions in the SPARQL 1.1 spec. ARQ
 * regards path transformation as an optimization. ARQ does not execute the
 * exact transformation as per spec as there are better ways to do it for ARQ.
 * For example, 
 * <ul>
 * <li>Path seq -> BGPs or a (sequence)
 * <li>"|" is not expanded into a union.
 * </ul>
 */

public class TransformPathFlattern extends TransformCopy
{
    // This transform is also used so programmtically built queries also get converted.  
    // Need previous BGP for merging?  Do as a separate pass (sequence, BGP collapse)
    
    private PathCompiler pathCompiler ;

    public TransformPathFlattern() { this(new PathCompiler()) ; }
    
    public TransformPathFlattern(PathCompiler pathCompiler)
    {
        this.pathCompiler = pathCompiler ;
    }
    
    @Override
    public Op transform(OpPath opPath)
    {
        // Flatten down to triples where possible.
        PathBlock pattern = pathCompiler.reduce(opPath.getTriplePath()) ;
        // Any generated paths of exactly one to triple; convert to Op.
        return PathLib.pathToTriples(pattern) ;
    }
}
