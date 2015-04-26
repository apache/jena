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

package org.seaborne.dboe.engine.general;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.engine.main.OpExecutorFactory ;

/** OpExecutor in Node space; direct graph matcher; quads iterated over to make graph calls.
 *  Old style - not quack/rows (see OpExecutorRowsMain for that). 
 */
public class OpExecutorStageMain extends OpExecutorStage {
    
    public final static Logger log = LoggerFactory.getLogger(OpExecutorStageMain.class) ;

    public static final OpExecutorFactory factoryMain = new OpExecutorFactory() {
        @Override
        public OpExecutor create(ExecutionContext execCxt) {
            return new OpExecutorStageMain(execCxt) ;
        }
    } ;

    public OpExecutorStageMain(ExecutionContext execCxt) {
        super(execCxt) ;
    }

    @Override
    protected boolean isForThisExecutor(DatasetGraph dsg, Graph activeGraph, ExecutionContext execCxt) {
        return true ;
    }

    @Override
    protected QueryIterator match(Graph graph, BasicPattern bgp) {
        return OpExecLib.solvePattern(graph, bgp) ;
    }

}
