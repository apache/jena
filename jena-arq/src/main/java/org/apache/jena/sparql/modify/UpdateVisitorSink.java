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

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.QuadDataAccSink ;
import org.apache.jena.sparql.modify.request.UpdateVisitor ;
import org.apache.jena.update.Update ;

/**
 * UpdateSink that sends every Update to a worker visitor
 * except for    
 */
public class UpdateVisitorSink implements UpdateSink
{
    private final Prologue prologue = new Prologue();
    private final UpdateVisitor worker;
    private final Sink<Quad> addSink;
    private final Sink<Quad> delSink;
    
    public UpdateVisitorSink(UpdateVisitor worker, Sink<Quad> addSink, Sink<Quad> delSink) {
        this.worker = worker;
        this.addSink = addSink;
        this.delSink = delSink;
    }

    @Override
    public Prologue getPrologue() {
        return prologue;
    }

    @Override
    public void send(Update update) {
        update.visit(worker);
    }
    
    // The sink for INSERT DATA, DELETE DATA to go straight to sink handlers.
    @Override
    public QuadDataAccSink createInsertDataSink() {
        return new QuadDataAccSink(addSink);
    }

    @Override
    public QuadDataAccSink createDeleteDataSink() {
        return new QuadDataAccSink(delSink);
    }

    @Override
    public void flush()
    { }

    @Override
    public void close()
    { }
}
