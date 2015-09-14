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

package dev;

public class PROJECT_Quack {
    // Java 8 cleanups.
    // AccessorParallel
    
    // QueryIterator as a wrapper always on a normal Iterator.
    
    // Backport LeftJoin code.
    // Conditions for plain join
    // 
    
    // Sophisticated iterator handling.
    //   Peek and push back.
    //   Is empty.
    //   Is one row.
    //   RowIterator c.f QueryIteratorBase
    //   Cheap and easy to wrap an existing Iterator.
    //   Back and forth to streams.
    
    /*
     * Where should filter placement happen? BGP reordering?
     * 
     *  Define a lifecycle.
     *    "reorder" BGP -> BGP
     *    Place filters.
     *    Reorder
     *    Place filters.
     *  
OpExecutor
  OpExecutorBlockFilter         <<- rename?
    + OpExecutorBlock           <<-  
    |   OpExecutorStage
    |     OpExecutorStageMain   <<- Node space, old style
    + OpExecutorRowsMain        <<- Node space, rows
    + OpExecutorTDBBase
    |   OpExecutorQuackTDB    

    When to go to X space?

    BlockExecutors for BGP excution (replaces StageGenerator
    public interface BlockExecutor
    {
        public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt) ;
    }
    
    Where does reordering go?
    Needs a PeekIterator. 
    
    */
    
    /* 
     * OpExecLib
     */
    
    // Incorportate reordering of BGPs.  Add a defualt of "fixed"
    
    // Detailed tests for Accessor(TBD)
    // Benchmark setup.

    // Creating ExecutionContexts
    //    QC.createExecutionContext(dsg)
    
    // Iterator -> RowSource = Iterator + isReady() 
    
    // Java8-isation.
    
    // Caching in substitution
    //   Same binding (row) -> same output
    //   Use array for output, sized to max prev seen.

    // BindingBuilder
    
    // Scope tracker.
    
    // Drop support for TDB/OpExecutorQuack on anything but datasets - not individual graphs.
    // OpExecutorTDBBase.isForThisExecutor
    // EecutionContext.getExecutor() is not a factory.
    
    // BSBM Q5 ****
    // Reorder: adjacent same-subject
    //   Do and don't break up by filter reorder.
    //   StepPredicateObjectList is simpler.
    // * TransformReorder
    // * TransformFilterPlacement <-- Spilt this
    // What does Quack do current?
    // What does TDB do current?
    
    // Design:
    //   Extend Quack to Sequence/Join/LeftJoin/Conditional
    //    AccessorGraph not trigger fully by test suite.
    //    OpExecutorRowsMain not using PredObjList

    // TDB
    // Setting factory needs to set the global choice
    //   QC.execute in: QueryIterUnion, QueryIterOptionalIndex,
    //                  ExprFunctionOp,
    //                  QueryIterGraph.QueryIterGraphInner, 
    // Only need change at OpGraph boundaries.
    // ExecutionContext.getCurrentOpExec(), not factory
    
    // ** Sort out wiring.
    //   OpExecutorQuackTDB has an indirection.
    //   QueryEngineQuack does set the context factory.
    //   Add getOpExecutor to ExecutionContext?  Currently return the factory.
    
    // ** Initial inputs and accessors
    // RowVarBinding - a fixed array version.
    
    // Quack: Tuple<Slot> == Slots? Pattern? TuplePattern? TPattern 
    
    // BGP reorder by Transform.
    // Not working for graphs ?g ? 

    // Optimizer
    //   reorder BGPs then do filter placement optimization.
    // Scan with filter (between X Y)
    //   Links to jump start on merge join?
    // Do we need a bottom up executor c.f. OpExecutor and Evaluator (ref engine).
    
    // OpExecutorGeneral == OpExecutorMain2?
    //   Document as extension point - roll out to ARQ, remove StageGenerators. 
    
    // BindingNodeId (read-only part) as interface so a Row<NodeId> can project it. 
    // RowBuilder that is Tuples<NodeId> to Row<NodeId>
    // RowBuilder fixed width rows.
    
    // Fixed width Rows 
    // Row implementation which is a a fixed width, fixed variable (for small N)
    // e.g. a [] of Var and a [] of results.
    
    // Efficient Tuples and Bindings.
    //   Test whether fixed width whole pattern bindings make sense.
    //   Tuple.make/1,2,3
    //   Binding.make/1,2,3 or Binding Builder
    
    // MergeJoin
    //   Jumpstart right. Needs per X code via Accessor? 
    //   Small left, large right ==> merge or substitute?

    // == TDB
    // StorageTDB = indexes + node table.
    // + SystemParams2 / SystemParamsBuilder
    //     + Setting default system params or building dataset
    //     + Location sensitive?

    // + idx tools
    //   + Index copy to do location to location
    //    DBcopy to do location to location (compression!)
    //   + idxinfo
    //   + idxdump
}
