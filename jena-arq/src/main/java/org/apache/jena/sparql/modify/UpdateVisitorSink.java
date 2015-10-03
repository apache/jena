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

import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.modify.request.QuadDataAccSink ;
import org.apache.jena.sparql.modify.request.UpdateVisitor ;
import org.apache.jena.update.Update ;

public class UpdateVisitorSink implements UpdateSink
{
    private final Prologue prologue;
    private final UpdateVisitor worker;
    
    public UpdateVisitorSink(UpdateVisitor worker)
    {
        this.prologue = new Prologue();
        this.worker = worker;
    }
    
    @Override
    public Prologue getPrologue()
    {
        return prologue;
    }
    
    @Override
    public void send(Update update)
    {
        update.visit(worker);
    }
    
    @Override
    public QuadDataAccSink createInsertDataSink()
    {
        return new QuadDataAccSink(worker.createInsertDataSink());
    }
    
    @Override
    public QuadDataAccSink createDeleteDataSink()
    {
        return new QuadDataAccSink(worker.createDeleteDataSink());
    }

    @Override
    public void flush()
    { }

    @Override
    public void close()
    { }
}
