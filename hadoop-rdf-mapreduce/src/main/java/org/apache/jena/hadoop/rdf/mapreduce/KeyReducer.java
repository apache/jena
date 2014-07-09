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

package org.apache.jena.hadoop.rdf.mapreduce;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer;

/**
 * A reducer that outputs a single pair consists of the key as both fields ignoring the values
 * @author rvesse
 *
 * @param <TKey> Key
 * @param <TValue> Value
 */
public class KeyReducer<TKey, TValue> extends Reducer<TKey, TValue, TKey, TKey> {

    @Override
    protected void reduce(TKey key, Iterable<TValue> values, Context context)
            throws IOException, InterruptedException {
        context.write(key, key);
    }
}
