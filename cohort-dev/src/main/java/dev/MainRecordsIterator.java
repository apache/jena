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

package dev;

import static dev.RecordLib.r ;
import static dev.RecordLib.recordFactory ;

import java.util.Iterator ;
import java.util.stream.IntStream ;

import org.apache.jena.atlas.logging.LogCtl ;
import org.seaborne.dboe.base.buffer.RecordBuffer ;
import org.seaborne.dboe.base.record.Record ;

public class MainRecordsIterator {
    static { LogCtl.setLog4j(); }
    
    public static void main(String...argv) {
        
        RecordBuffer rb = new RecordBuffer(recordFactory, 10) ;
        IntStream.range(1, 5).forEach(x->{
            rb.add(r(x)) ;
        }) ;
        
        Iterator<Record> iter = rb.iterator(r(2), r(99)) ;
        iter.forEachRemaining(System.out::println) ;
        
    }

}

