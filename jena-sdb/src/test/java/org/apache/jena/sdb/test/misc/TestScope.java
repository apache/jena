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

package org.apache.jena.sdb.test.misc;

import org.apache.jena.sdb.core.ScopeBase;
import org.apache.jena.sdb.core.ScopeEntry;
import org.apache.jena.sdb.core.sqlexpr.SqlColumn;
import org.apache.jena.sdb.core.sqlnode.SqlTable;
import org.apache.jena.sparql.core.Var;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class TestScope {
    @Test
    public void testScopeOrder() {
        ScopeBase base = new ScopeBase();

        SqlTable quads = new SqlTable("quads");
        base.setColumnForVar(Var.alloc("s"), new SqlColumn(quads, "s"));
        base.setColumnForVar(Var.alloc("p"), new SqlColumn(quads, "p"));
        base.setColumnForVar(Var.alloc("o"), new SqlColumn(quads, "o"));

        Iterator<Var> vars = base.getVars().iterator();
        Var var;

        Assert.assertTrue(vars.hasNext());
        var  = vars.next();
        Assert.assertTrue("s".equals(var.getVarName()));

        Assert.assertTrue(vars.hasNext());
        var  = vars.next();
        Assert.assertTrue("p".equals(var.getVarName()));

        Assert.assertTrue(vars.hasNext());
        var  = vars.next();
        Assert.assertTrue("o".equals(var.getVarName()));

        Assert.assertFalse(vars.hasNext());

        Iterator<ScopeEntry>  scopes = base.findScopes().iterator();
        ScopeEntry entry;

        Assert.assertTrue(scopes.hasNext());
        entry = scopes.next();
        Assert.assertTrue("s".equals(entry.getVar().getVarName()));
        Assert.assertTrue("s".equals(entry.getColumn().getColumnName()));

        Assert.assertTrue(scopes.hasNext());
        entry = scopes.next();
        Assert.assertTrue("p".equals(entry.getVar().getVarName()));
        Assert.assertTrue("p".equals(entry.getColumn().getColumnName()));

        Assert.assertTrue(scopes.hasNext());
        entry = scopes.next();
        Assert.assertTrue("o".equals(entry.getVar().getVarName()));
        Assert.assertTrue("o".equals(entry.getColumn().getColumnName()));

        Assert.assertFalse(scopes.hasNext());
    }
}
