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

package com.hp.hpl.jena.sparql.modify;

import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAccSink;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataDelete;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateRequestSink implements UpdateSink
{
    final UpdateRequest updateRequest;
    
    public UpdateRequestSink(UpdateRequest updateRequest)
    {
        this.updateRequest = updateRequest;
    }
    
    @Override
    public void send(Update update)
    {
        updateRequest.add(update);
    }
    
    @Override
    public void flush()
    { }
    
    @Override
    public void close()
    { }
    
    @Override
    public Prologue getPrologue()
    {
        return updateRequest;
    }
    
    @Override
    public QuadDataAccSink createInsertDataSink()
    {
        QuadDataAcc quads = new QuadDataAcc();
        send(new UpdateDataInsert(quads));
        
        return quads;
    }
    
    @Override
    public QuadDataAccSink createDeleteDataSink()
    {
        QuadDataAcc quads = new QuadDataAcc();
        send(new UpdateDataDelete(quads));
        
        return quads;
    }
}
