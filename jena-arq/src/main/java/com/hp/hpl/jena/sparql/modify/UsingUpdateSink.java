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

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAccSink ;
import com.hp.hpl.jena.sparql.modify.request.UpdateWithUsing ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateException ;

/**
 * Adds using clauses from the UsingList to UpdateWithUsing operations; will throw an UpdateException if the modify operation already contains a using clause. 
 */
public class UsingUpdateSink implements UpdateSink
{
    private final UpdateSink sink; 
    private final UsingList usingList;
    
    public UsingUpdateSink(UpdateSink sink, UsingList usingList)
    {
        this.sink = sink;
        this.usingList = usingList;
    }
    
    @Override
    public void send(Update update)
    {
        // ---- check USING/USING NAMED/WITH not used.
        // ---- update request to have USING/USING NAMED 
        if ( null != usingList && usingList.usingIsPresent() )
        {
            if ( update instanceof UpdateWithUsing )
            {
                UpdateWithUsing upu = (UpdateWithUsing)update ;
                if ( upu.getUsing().size() != 0 || upu.getUsingNamed().size() != 0 || upu.getWithIRI() != null )
                    throw new UpdateException("SPARQL Update: Protocol using-graph-uri or using-named-graph-uri present where update request has USING, USING NAMED or WITH") ;
                for ( Node node : usingList.getUsing() )
                    upu.addUsing(node) ;
                for ( Node node : usingList.getUsingNamed() )
                    upu.addUsingNamed(node) ;
            }
        }
        
        sink.send(update);
    }

    @Override
    public QuadDataAccSink createInsertDataSink()
    {
        return sink.createInsertDataSink();
    }
    
    @Override
    public QuadDataAccSink createDeleteDataSink()
    {
        return sink.createDeleteDataSink();
    }
    
    @Override
    public void flush()
    {
        sink.flush();
    }

    @Override
    public void close()
    {
        sink.close();
    }

    @Override
    public Prologue getPrologue()
    {
        return sink.getPrologue();
    }
}
