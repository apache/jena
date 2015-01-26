/*
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

package quack;

import com.hp.hpl.jena.tdb.base.file.Location ;

import org.junit.Assert ;
import org.junit.Test ;

public class TestIndexRef extends Assert {
    @Test public void index_ref_1() {
        IndexRef ref = IndexRef.parse("SPO") ;
        assertEquals("SPO", ref.getBaseFileName()) ;
        assertEquals("SPO", ref.getIndexName()) ;
    }
    
    @Test public void index_ref_2() {
        IndexRef ref = IndexRef.parse("DB/SPO") ;
        assertEquals("SPO", ref.getBaseFileName()) ;
        assertEquals("SPO", ref.getIndexName()) ;
        assertEquals(Location.create("DB"), ref.getLocation()) ; 
    }

    @Test public void index_ref_3() {
        IndexRef ref = IndexRef.parse("DB/SPO-00") ;
        assertEquals("SPO-00", ref.getBaseFileName()) ;
        assertEquals("SPO", ref.getIndexName()) ;
        assertEquals(Location.create("DB"), ref.getLocation()) ; 
    }
    
    @Test public void index_ref_4() {
        IndexRef ref = IndexRef.parse("/SPO-00") ;
        assertEquals("SPO-00", ref.getBaseFileName()) ;
        assertEquals("SPO", ref.getIndexName()) ;
        assertEquals(Location.create("/"), ref.getLocation()) ; 
    }

}

