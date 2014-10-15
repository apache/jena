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

import java.util.Arrays ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.apache.jena.atlas.lib.Bytes ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;

public class IndexMap implements Index
{
    private final Map<ByteArray, ByteArray> index = new HashMap<>() ;
    private final RecordFactory recordFactory ;
    
    public IndexMap(RecordFactory recordFactory)
    {
        this.recordFactory = recordFactory ;
    }
    
    @Override
    public Record find(Record record)
    {
        ByteArray k = wrap(record.getKey()) ;
        ByteArray v = index.get(k) ;
        if ( v == null )
            return null ;
        return record(k, v) ;
    }

    @Override
    public boolean contains(Record record)
    {
        Record r = find(record) ;
        if ( r == null )
            return false ;
        if ( ! recordFactory.hasValue() )
            return true ;
        return Bytes.compare(record.getValue(), r.getValue()) == 0 ;
    }

    @Override
    public boolean add(Record record)
    {
        Record r = find(record) ;
        if ( r != null && r.equals(record) )
            return false ;
        index.put(wrap(record.getKey()), wrap(record.getValue())) ;
        return true ;
    }

    @Override
    public boolean delete(Record record)
    {
        ByteArray x = index.remove(wrap(record.getKey())) ;
        if ( x == null )
            return false ;
        return true ;
    }

    @Override
    public Iterator<Record> iterator()
    {
        return new Iterator<Record>() {
            
            Iterator<Map.Entry<ByteArray, ByteArray>> iter = index.entrySet().iterator() ;
            
            @Override
            public boolean hasNext()
            {
                return iter.hasNext() ;
            }

            @Override
            public Record next()
            {
                Map.Entry<ByteArray, ByteArray> e = iter.next() ;
                return record(e.getKey(), e.getValue()) ; 
            }

            @Override
            public void remove()
            { throw new UnsupportedOperationException() ;}
        } ; 
        
    }

    @Override
    public RecordFactory getRecordFactory()
    {
        return recordFactory ;
    }

    @Override
    public boolean isEmpty()
    {
        return index.isEmpty() ;
    }

    @Override
    public void clear()
    {
        index.clear() ;
    }

    @Override
    public void check()
    {}

    @Override
    public long size()
    {
        return index.size() ;
    }

    @Override
    public void sync()
    {}

    @Override
    public void close()
    {}

    private static ByteArray wrap(byte[] b) { return new ByteArray(b) ; }
    private Record record(ByteArray k, ByteArray v) { return recordFactory.create(k.bytes, v.bytes) ; }
    
    private static class ByteArray
    {
        byte[] bytes ;
        ByteArray(byte[] bytes) { this.bytes = bytes ; }
        
        @Override
        public int hashCode()
        {
            final int prime = 31 ;
            int result = 1 ;
            result = prime * result + Arrays.hashCode(bytes) ;
            return result ;
        }
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true ;
            if (obj == null) return false ;
            if (getClass() != obj.getClass()) return false ;
            ByteArray other = (ByteArray)obj ;
            if (!Arrays.equals(bytes, other.bytes)) return false ;
            return true ;
        }
    }

}
