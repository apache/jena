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

package org.seaborne.dboe.engine;

import org.junit.Assert ;
import org.junit.Test ;
import org.seaborne.dboe.engine.Join ;
import org.seaborne.dboe.engine.JoinKey ;
import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.join.HashJoin ;
import org.seaborne.dboe.engine.join.HashJoin.Hasher ;
import org.seaborne.dboe.engine.row.RowBuilderBase ;

import com.hp.hpl.jena.sparql.core.Var ;

public class TestJoinMisc extends Assert {
    // Various of these tests check to see if two unrelated hashes
    // are not equal, which is not guaranteed.  Changes to the hash function
    // may require test tweaking. 
    
    @Test public void hash_01() {
        Hasher<Integer> hasher = HashJoin.hash() ;
        long x = hasher.hash(null, 1) ;
    }
    
    @Test public void hash_02() {
        Hasher<Integer> hasher = HashJoin.hash() ;
        long x1 = hasher.hash(null, 1) ;
        long x2 = hasher.hash(null, 2) ;
        assertNotEquals(x1, x2);
    }

    @Test public void hash_04() {
        Hasher<Integer> hasher = HashJoin.hash() ;
        Var v = Var.alloc("a") ;
        long x1 = hasher.hash(null, 1) ;
        long x2 = hasher.hash(v, 1) ;
        assertNotEquals(x1, x2);
    }

    @Test public void hash_05() {
        Hasher<Integer> hasher = HashJoin.hash() ;
        Var v1 = Var.alloc("a") ;
        Var v2 = Var.alloc("b") ;
        long x1 = hasher.hash(v1, 1) ;
        long x2 = hasher.hash(v2, 1) ;
        assertNotEquals(x1, x2);
    }

    @Test public void hash_06() {
        Hasher<Integer> hasher = HashJoin.hash() ;
        Var v1 = Var.alloc("a") ;
        Var v2 = Var.alloc("b") ;
        long x1 = hasher.hash(v1, 1) ;
        long x2 = hasher.hash(v2, 2) ;
        assertNotEquals(x1, x2);
    }
    
    static Var var_a = Var.alloc("a") ;
    static Var var_b = Var.alloc("b") ;
    
    static Row<String> row0 = QJT.parseRowString("(row)") ; 
    static Row<String> row1 = QJT.parseRowString("(row (?a '1'))") ;
    static Row<String> row2 = QJT.parseRowString("(row (?a '1') (?b '1'))") ;
    static Row<String> row3 = QJT.parseRowString("(row (?a '2') (?b '1'))") ;
    
    static JoinKey jk_null = QJT.parseJoinKey("(key)") ;
    static JoinKey jk_a =    QJT.parseJoinKey("(key ?a)") ;
    static JoinKey jk_ab =   QJT.parseJoinKey("(key ?a ?b)") ;
    
    @Test public void hash_10() {
        Hasher<String> hasher = HashJoin.hash() ;
        Object h1 = HashJoin.hash(hasher, jk_a, row1) ;
        assertNotNull(h1) ;
    }
    
    @Test public void hash_11() {
        // The "?b" in the join key perturbs the hash. 
        Hasher<String> hasher = HashJoin.hash() ;
        Object h1 = HashJoin.hash(hasher, jk_a, row1) ;
        Object h2 = HashJoin.hash(hasher, jk_ab, row1) ;
        assertNotNull(h1);
        assertNotNull(h2);
        assertNotEquals(h1, h2);
    }

    @Test public void hash_12() {
        Hasher<String> hasher = HashJoin.hash() ;
        Object h1 = HashJoin.hash(hasher, jk_a, row1) ;
        Object h2 = HashJoin.hash(hasher, jk_a, row2) ;
        assertNotNull(h1);
        assertNotNull(h2);
        assertEquals(h1, h2);
    }

    @Test public void hash_13() {
        Hasher<String> hasher = HashJoin.hash() ;
        Object h1 = HashJoin.hash(hasher, jk_ab, row1) ;
        Object h2 = HashJoin.hash(hasher, jk_ab, row2) ;
        assertNotNull(h1);
        assertNotNull(h2);
        assertNotEquals(h1, h2);
    }

    @Test public void hash_20() {
        Hasher<String> hasher = HashJoin.hash() ;
        Object h1 = HashJoin.hash(hasher, jk_null, row1) ;
        assertNotNull(h1);
        assertEquals(HashJoin.noKeyHash, h1) ;
    }
    
    @Test public void hash_21() {
        Hasher<String> hasher = HashJoin.hash() ;
        Object h1 = HashJoin.hash(hasher, jk_null, row0) ;
        assertNotNull(h1);
        assertEquals(HashJoin.noKeyHash, h1) ;
    }
    
    @Test public void hash_22() {
        Hasher<String> hasher = HashJoin.hash() ;
        Object h1 = HashJoin.hash(hasher, jk_a, row0) ;
        assertNotNull(h1);
        assertEquals(HashJoin.noKeyHash, h1) ;
    }
    
    private static void compatible(String s1, String s2, boolean expected) {
        Row<Integer> row1 = QJT.parseRowInt(s1) ;
        Row<Integer> row2 = QJT.parseRowInt(s2) ;
        boolean actual = Join.compatible(row1, row2) ;
        assertEquals(expected, actual) ;
    }
    
    private static void merge(String s1, String s2, String expected) {
        Row<Integer> row1 = QJT.parseRowInt(s1) ;
        Row<Integer> row2 = QJT.parseRowInt(s2) ;
        Row<Integer> rowOut = null ;
        if ( expected != null )
            rowOut = QJT.parseRowInt(expected) ;
        RowBuilderBase<Integer> builder = new RowBuilderBase<>() ;
        Row<Integer> rowResult = Join.merge(row1, row2, builder) ;
        if ( rowResult != null )
            assertTrue("Not compatible but merge defined: "+row1+", "+row2, Join.compatible(row1, row2)) ;
        assertTrue("Compatible but unexpected merge: "+row1+", "+row2, QJT.equal(rowOut, rowResult)) ;
    }

    @Test public void compatible_01() { compatible("(row)", "(row)", true) ; }
    @Test public void compatible_02() { compatible("(row)", "(row (?a 1))", true) ; }
    @Test public void compatible_03() { compatible("(row (?a 1))", "(row)", true) ; }
    
    @Test public void compatible_21() { compatible("(row (?a 1))", "(row (?a 1))", true) ; }
    @Test public void compatible_22() { compatible("(row (?a 1))", "(row (?a 2))", false) ; }
    
    @Test public void merge_01() { merge("(row)", "(row)", "(row)") ; }
    @Test public void merge_02() { merge("(row)", "(row (?a 1))", "(row (?a 1))") ; }
    @Test public void merge_03() { merge("(row (?a 1))", "(row)", "(row (?a 1))") ; }
    @Test public void merge_04() { merge("(row (?a 1))", "(row (?b 9))", "(row (?a 1) (?b 9))") ; }
    @Test public void merge_05() { merge("(row (?a 1))", "(row (?b 9))", "(row (?b 9) (?a 1))") ; }
    
    @Test public void merge_21() { merge("(row (?a 1))", "(row (?a 1))", "(row (?a 1))") ; }
    @Test public void merge_22() { merge("(row (?a 1))", "(row (?a 2))", null) ; }
    
    @Test public void merge_23() { merge("(row (?a 1))", "(row (?a 1) (?b 9))", "(row (?a 1) (?b 9))") ; }
    @Test public void merge_24() { merge("(row (?a 1) (?b 9))", "(row (?a 1) (?b 9))", "(row (?a 1) (?b 9))") ; }
    @Test public void merge_25() { merge("(row (?a 1) (?b 9))", "(row (?a 1))", "(row (?a 1) (?b 9))") ; }
    
    @Test public void merge_26() { merge("(row (?a 1) (?b 9))", "(row (?a 1) (?b 8))", null) ; }
}