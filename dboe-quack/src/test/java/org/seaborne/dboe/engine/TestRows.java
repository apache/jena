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

import java.util.Iterator ;

import org.junit.Assert ;
import org.junit.Test ;
import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.RowBuilder ;
import org.seaborne.dboe.engine.RowLib ;
import org.seaborne.dboe.engine.RowList ;
import org.seaborne.dboe.engine.row.RowBase ;
import org.seaborne.dboe.engine.row.RowVarBinding ;

import org.apache.jena.sparql.core.Var ;

public class TestRows extends Assert
{
    static Var var_x = Var.alloc("x") ;
    static Var var_y = Var.alloc("y") ;
    static Var var_z = Var.alloc("z") ;

    @Test public void row_00() {
        RowBase<String> rb = new RowBase<>() ;
        assertFalse(rb.contains(var_x)) ;
        assertEquals(0, rb.vars().size()) ;
        assertTrue(rb.isEmpty()) ;
    }

    @Test public void row_01() {
        RowBase<String> rb = new RowBase<>() ;
        rb.set(Var.alloc("x"), "X");
        assertTrue(rb.contains(var_x)) ;
        assertEquals("X", rb.get(var_x)) ;
        assertEquals(1, rb.vars().size()) ;
    }
    
    @Test public void row_03() {
        RowBuilder<String> rb = RowLib.createRowBuilder() ;
        rb.add(var_x, "X") ;
        assertTrue(rb.contains(var_x)) ;
        assertEquals("X", rb.get(var_x)) ;
        Row<String> r = rb.build() ;
        assertTrue(r.contains(var_x)) ;
        assertEquals("X", r.get(var_x)) ;
        assertFalse(r.isEmpty()) ;
    }
    
    @Test public void row_10() {
        Row<String> r = RowLib.identityRow() ;
        assertTrue(r.isEmpty()) ;
        assertTrue(r.isIdentity()) ;
    }

    @Test public void row_11() {
        RowList<String> rows = RowLib.identityRowList() ;
        assertFalse(rows.isEmpty()) ;
        assertTrue(rows.isIdentity()) ;
        Iterator<Row<String>> iter = rows.iterator() ;
        assertTrue(iter.hasNext()) ;
        Row<String> r = iter.next() ;
        assertFalse(iter.hasNext()) ;
        assertTrue(r.isIdentity()) ;
    }

    @Test public void row_12() {
        RowList<String> rows = RowLib.emptyRowList() ;
        assertTrue(rows.isEmpty()) ;
        assertFalse(rows.isIdentity()) ;
        assertFalse(rows.iterator().hasNext()) ;
    }

    @Test public void rowVarBinding_00() {
        RowVarBinding<String> r2 = new RowVarBinding<>(null, var_x, "X") ;
        assertTrue(r2.contains(var_x)) ;
        r2.contains(var_y) ;
        assertFalse(r2.contains(var_y)) ;
        assertEquals(r2.get(var_x), "X") ;
    }

    @Test public void rowVarBinding_01() {
        RowBase<String> rb = null ;
        RowVarBinding<String> r2 = new RowVarBinding<>(rb, var_x, "X") ;
        RowVarBinding<String> r3 = new RowVarBinding<>(rb, var_y, "Y") ;
        
        assertFalse(r3.contains(var_x)) ;
        assertTrue(r3.contains(var_y)) ;
        
        assertTrue(r2.contains(var_x)) ;
        assertFalse(r2.contains(var_y)) ;

        assertFalse(r3.contains(var_z)) ;
        assertEquals(null, r3.get(var_x)) ;
        assertEquals("Y", r3.get(var_y)) ;
        assertEquals("X", r2.get(var_x)) ;
        assertEquals(null, r2.get(var_y)) ;
        
        assertEquals(1, r2.vars().size() ) ;
        assertEquals(1, r3.vars().size() ) ;
    }

    @Test public void rowVarBinding_02() {
        RowBase<String> rb = new RowBase<>() ;
        rb.set(Var.alloc("x"), "X"); 
        RowVarBinding<String> r2 = new RowVarBinding<>(rb, Var.alloc("y"), "Y") ;
        RowVarBinding<String> r3 = new RowVarBinding<>(r2, Var.alloc("z"), "Z") ;
        assertNotEquals(r2, r3) ;

        assertEquals(2, r2.vars().size() ) ;
        assertEquals(3, r3.vars().size() ) ;

        assertEquals("X", r3.get(var_x)) ;
        assertEquals("Y", r3.get(var_y)) ;
        assertEquals("Z", r3.get(var_z)) ;
        assertNotEquals("Z", r2.get(var_z)) ;
    }
}
