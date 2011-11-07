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

package com.hp.hpl.jena.sdb.layout1;

import com.hp.hpl.jena.sdb.compiler.QueryCompiler;
import com.hp.hpl.jena.sdb.compiler.QueryCompilerFactory;
import com.hp.hpl.jena.sdb.core.SDBRequest;


public class QueryCompilerFactory1 implements QueryCompilerFactory
{
    private EncoderDecoder codec ;

    public QueryCompilerFactory1(EncoderDecoder codec)
    {
        this.codec = codec ;
    }

    @Override
    public QueryCompiler createQueryCompiler(SDBRequest request)
    {
        return new QueryCompiler1(request, codec) ;
    }
}
