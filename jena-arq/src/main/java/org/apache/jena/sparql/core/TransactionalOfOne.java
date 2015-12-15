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

package org.apache.jena.sparql.core;

import org.apache.jena.query.ReadWrite ;
import static java.lang.ThreadLocal.withInitial; 

public class TransactionalOfOne implements Transactional
{
    private TransactionalComponent component;
    private ThreadLocal<ReadWrite> state = withInitial(()->null) ;
    
    public TransactionalOfOne(TransactionalComponent component) { this.component = component ; }
    
    @Override
    public void begin(ReadWrite readWrite) {
        state.set(readWrite) ;
        component.begin(readWrite);
    }
    
    @Override
    public void commit() {
        component.commit();
        end() ;
    }
    
    @Override
    public void abort() {
        component.abort();
        end() ;
    }
    
    @Override
    public boolean isInTransaction() {
        return state.get() != null ;
    }
    
    @Override
    public void end() {
        if ( isInTransaction() ) {
            component.end();
            state.set(null) ;
        }
    }
}
