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

package org.apache.jena.sparql.modify;

import java.util.function.Consumer;

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.modify.request.UpdateVisitor ;
import org.apache.jena.sparql.util.Context ;

/**
 * Default implementation of an update engine based on stream updates to a worker
 * function. In addition, it applies INSERT DATA and DELETE DATA driven off the updates,
 * which may be directly from the parser.
 * <p>
 * Developers who only want to change/extend the processing of individual updates can
 * easily
 * </p>
 * <p>
 * See {@link UpdateEngineNonStreaming} for a subclass that accumulates updates, including  during
 * parsing then executes the operation.
 */
public class UpdateEngineMain extends UpdateEngineBase 
{
    /**
     * Creates a new Update Engine
     * @param datasetGraph DatasetGraph the updates operate over
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding (i.e. UpdateDeleteWhere, UpdateModify)
     * @param context Execution Context
     */
    public UpdateEngineMain(DatasetGraph datasetGraph, Binding inputBinding, Context context)
    {
        super(datasetGraph, inputBinding, context) ;
    }

    @Override
    public void startRequest() {}
    
    @Override
    public void finishRequest() {}
    
    private UpdateSink updateSink = null ;
    
    /*
     * Returns the {@link UpdateSink}. In this implementation, this is done by with
     * an {@link UpdateVisitor} which will visit each update operation and send the
     * operation to the associated {@link UpdateEngineWorker}. The quads in INSERT
     * DATA and DELETE DATA will be passed to the respective sink handlers so they may
     * act immediately and not accumulate during parsing, and then be acted upon. See
     */
    @Override
    public UpdateSink getUpdateSink()
    {
        if ( updateSink == null )
            updateSink = new UpdateVisitorSink(this.prepareWorker(),
                                               sink(q->datasetGraph.add(q)), 
                                               sink(q->datasetGraph.delete(q)));
        return updateSink ;
    }
    
    /**
     * Creates the {@link UpdateVisitor} which will do the work of applying the updates
     * @return The update visitor to be used to apply the updates
     */
    protected UpdateVisitor prepareWorker() {
        return new UpdateEngineWorker(datasetGraph, inputBinding, context) ;
    }

    /** Direct a sink to a Consumer. */ 
    private <X> Sink<X> sink(Consumer<X> action) {
        return new Sink<X>() {
            @Override
            public void send(X item) { action.accept(item); }

            @Override public void close() {} 

            @Override public void flush() {}
        }; 
    }
    
    private static UpdateEngineFactory factory = new UpdateEngineFactory()
    {
        @Override
        public boolean accept(DatasetGraph datasetGraph, Context context) {
            return true ;
        }

        @Override
        public UpdateEngine create(DatasetGraph datasetGraph, Binding inputBinding,
                                   Context context) {
            return new UpdateEngineMain(datasetGraph, inputBinding, context) ;
        }
    } ;

    public static UpdateEngineFactory getFactory() { return factory ; }
}
