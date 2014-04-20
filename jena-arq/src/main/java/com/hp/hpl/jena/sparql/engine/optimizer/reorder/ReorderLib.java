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

package com.hp.hpl.jena.sparql.engine.optimizer.reorder ;

import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.engine.optimizer.StatsMatcher ;

public class ReorderLib
{
    private static class ReorderProcIdentity implements ReorderProc
    {
        @Override
        public BasicPattern reorder(BasicPattern pattern) {
            return pattern ;
        }

        @Override
        public String toString() {
            return "identity reorder" ;
        }
    }
    private static ReorderProc _identityProc = new ReorderProcIdentity() ;

    private static class ReorderTransformationIdentity implements ReorderTransformation
    {
        @Override
        public BasicPattern reorder(BasicPattern pattern) {
            return pattern ;
        }

        @Override
        public ReorderProc reorderIndexes(BasicPattern pattern) {
            return _identityProc ;
        }
    }
    private static ReorderTransformation _identity = new ReorderTransformationIdentity() ;

    /**
     * Return a ReorderProc that does no reordering (leaving the query writer
     * in-control)
     */
    public static ReorderProc identityProc() {
        return _identityProc ;
    }

    /**
     * Return a ReorderTransformation that maps directly to the original
     * (leaving the query writer in-control)
     */
    public static ReorderTransformation identity() {
        return _identity ;
    }

    public static ReorderTransformation fixed() {
        return new ReorderFixed() ;
    }

    public static ReorderTransformation weighted(String filename) {
        StatsMatcher stats = new StatsMatcher(filename) ;
        return new ReorderWeighted(stats) ;
    }

}
