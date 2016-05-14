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

package org.apache.jena.riot.lang ;

import org.apache.jena.riot.system.StreamRDF ;

/**
 * Abstract implementation of a producer class that implements {@code StreamRDF};
 * use one of the concrete implementations that match the RDF primitive you are using.
 * @param <T> Type corresponding to a supported RDF primitive
 * 
 * @see PipedTriplesStream
 * @see PipedQuadsStream
 * @see PipedTuplesStream
 */
public abstract class PipedRDFStream<T> implements StreamRDF
{
    private final PipedRDFIterator<T> sink ;

    protected PipedRDFStream(PipedRDFIterator<T> sink)
    {
        this.sink = sink ;
        this.sink.connect();
    }

    protected void receive(T t)
    {
        sink.receive(t) ;
    }

    @Override
    public void base(String base)
    {
        sink.base(base) ;
    }

    @Override
    public void prefix(String prefix, String iri)
    {
        sink.prefix(prefix, iri) ;
    }

    @Override
    public void start()
    {
        sink.start() ;
    }

    @Override
    public void finish()
    {
        sink.finish() ;
    }
}
