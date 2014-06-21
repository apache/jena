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

package com.hp.hpl.jena.sparql.graph;

import org.apache.jena.atlas.data.BagFactory ;
import org.apache.jena.atlas.data.DataBag ;
import org.apache.jena.atlas.data.ThresholdPolicy ;
import org.apache.jena.riot.system.SerializationFactoryFinder ;

import com.hp.hpl.jena.graph.Triple ;

/**
 * An implementation of {@link GraphDataBag} that may return duplicate triples from the find() operation.
 */
public class GraphDefaultDataBag extends GraphDataBag
{
    public GraphDefaultDataBag(ThresholdPolicy<Triple> thresholdPolicy)
    {
        super(thresholdPolicy) ;
    }
    
    @Override
    protected DataBag<Triple> createDataBag()
    {
        return BagFactory.newDefaultBag(getThresholdPolicy(), SerializationFactoryFinder.tripleSerializationFactory()) ;
    }
}

