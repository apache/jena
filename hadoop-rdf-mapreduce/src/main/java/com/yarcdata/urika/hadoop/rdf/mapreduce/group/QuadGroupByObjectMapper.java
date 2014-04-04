/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * A mapper which assists in grouping quads by object by reassigning their keys
 * to be their objects
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public class QuadGroupByObjectMapper<TKey> extends AbstractQuadGroupingMapper<TKey> {

    @Override
    protected NodeWritable selectKey(Quad quad) {
        return new NodeWritable(quad.getObject());
    }
}
