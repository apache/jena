/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
// And don't import any "dboe"

public class AdapterRangeIndex implements RangeIndex {
    private final org.seaborne.dboe.index.RangeIndex rIndex ;
    private final org.seaborne.dboe.base.record.RecordFactory dboeFactory ;
    private final RecordFactory tdbFactory ;

    public AdapterRangeIndex(org.seaborne.dboe.index.RangeIndex rangeIndex) {
        rIndex = rangeIndex ;
        dboeFactory = rIndex.getRecordFactory() ;
        tdbFactory = new RecordFactory(dboeFactory.keyLength(), dboeFactory.valueLength()) ;
    }

    private Record convertToTDB(org.seaborne.dboe.base.record.Record r) {
        return tdbFactory.create(r.getKey(), r.getValue()) ;
    }
    
    private org.seaborne.dboe.base.record.Record convertToMantis(Record r) {
        return dboeFactory.create(r.getKey(), r.getValue()) ;
    }

    @Override
    public Record find(Record record)
    { return convertToTDB(rIndex.find(convertToMantis(record))) ; }
    
    @Override
    public boolean contains(Record record)
    { return rIndex.contains(convertToMantis(record)) ; }
    
    @Override
    public Record minKey()
    { return convertToTDB(rIndex.minKey()) ; }
    
    @Override
    public Record maxKey()
    { return convertToTDB(rIndex.maxKey()) ; }
    
    @Override
    public boolean add(Record record)
    { return rIndex.insert(convertToMantis(record)) ; }
    
    @Override
    public boolean delete(Record record)
    { return rIndex.delete(convertToMantis(record)) ; }
    
//  public Record addAndReturnOld(Record record)
//  { return convertToTDB(bpt.addAndReturnOld(convertToMantis(record)) ; }
  
//    public Record deleteAndReturnOld(Record record)
//    { return convertToTDB(bpt.deleteAndReturnOld(convertToMantis(record)) ; }
    
    @Override
    public Iterator<Record> iterator()
    { return convertIterToTDB(rIndex.iterator()) ; }
    
    @Override
    public Iterator<Record> iterator(Record minRec, Record maxRec)
    { return convertIterToTDB(rIndex.iterator(convertToMantis(minRec), convertToMantis(maxRec))) ; }
    
    private Iterator<Record> convertIterToTDB(Iterator<org.seaborne.dboe.base.record.Record> iter) {
        return Iter.map(iter, r->convertToTDB(r)) ;
    }
    
    @Override
    public boolean isEmpty()
    { return rIndex.isEmpty() ; }
    
    @Override
    public void clear()
    { rIndex.clear() ; }
    
    @Override
    public void sync()
    { rIndex.sync() ; }
    
    @Override
    public void close()
    { rIndex.close() ; }

    public org.seaborne.dboe.index.RangeIndex getWrapped()
    { return rIndex ; }
    
    @Override
    public RecordFactory getRecordFactory()
    { return tdbFactory ; }

    @Override
    public void check()
    { rIndex.check() ; }

    @Override
    public long size()
    { return rIndex.size() ; }
}

