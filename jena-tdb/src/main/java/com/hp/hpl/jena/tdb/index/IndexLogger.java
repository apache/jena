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

package com.hp.hpl.jena.tdb.index;

import java.util.Iterator ;

import org.slf4j.Logger ;

import com.hp.hpl.jena.tdb.base.record.Record ;

public final class IndexLogger extends IndexWrapper
{
    private final Logger log ;

    public IndexLogger(RangeIndex rIdx, Logger log)
    {
        super(rIdx) ;
        this.log = log ;
    }

    @Override
    public boolean add(Record record)
    { 
        log.info("Add: "+record) ;
        return super.add(record) ; 
    }

    @Override
    public boolean delete(Record record)
    { 
        log.info("Delete: "+record) ;
        return super.delete(record) ; 
    }

    @Override
    public Record find(Record record)
    {
        log.info("Find: "+record) ;
        Record r2 = super.find(record) ;
        log.info("Find: "+record+" ==> "+r2) ;
        return r2 ;
    }

    @Override
    public Iterator<Record> iterator()
    {
        log.info("iterator()") ;
        return super.iterator() ;
    }
}
