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

package org.apache.jena.riot.lang;

import org.apache.jena.atlas.lib.Sink ;

import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/**
 * Send quads to a dataset. This Sink must be closed after use.
 */
public class SinkQuadsToDataset implements Sink<Quad>
{
    private final DatasetGraph dataset ;

    public SinkQuadsToDataset(boolean x , DatasetGraph dataset)
    {
        this.dataset = dataset ;
    }
    
    @Override
    public void send(Quad quad)
    {
        if ( quad.isTriple() )
            dataset.getDefaultGraph().add(quad.asTriple()) ;
        else
            dataset.add(quad) ;
    }

    @Override
    public void flush()
    {
        SystemARQ.sync(dataset) ;
    }

    @Override
    public void close()
    {}
}
