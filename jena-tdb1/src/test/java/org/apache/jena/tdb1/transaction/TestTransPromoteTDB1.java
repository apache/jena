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

package org.apache.jena.tdb1.transaction ;

import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.transaction.AbstractTestTransPromote ;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.tdb1.sys.SystemTDB;

/** Tests for transactions that start read and then promote to write -- TDB */
@SuppressWarnings("removal")
public class TestTransPromoteTDB1 extends AbstractTestTransPromote {
    public TestTransPromoteTDB1() {
        super(getLoggers()) ;
    }

    @Override
    protected DatasetGraph create() {
        return TDB1Factory.createDatasetGraph() ;
    }

    private static String[] getLoggers() {
        return new String[]{
            SystemTDB.errlog.getName(),
            TDB1.logInfoName
        } ;
    }

    @Override
    protected Class<JenaTransactionException> getTransactionExceptionClass() {
        return JenaTransactionException.class ;
    }
}
