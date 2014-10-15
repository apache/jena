/**
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

package org.apache.jena.jdbc.results.metadata.columns;

import java.sql.SQLException;
import java.sql.Types;

/**
 * Column information for string columns
 * 
 */
public class StringColumn extends SparqlColumnInfo {

    /**
     * Creates new string column information
     * 
     * @param label
     *            Label
     * @param nullable
     *            Nullability
     * @throws SQLException
     */
    public StringColumn(String label, int nullable) throws SQLException {
        this(label, Types.NVARCHAR, nullable);
    }

    /**
     * Creates new string column information
     * 
     * @param label
     *            Label
     * @param sqlType
     *            SQL Type
     * @param nullable
     *            Nullability
     * @throws SQLException
     */
    public StringColumn(String label, int sqlType, int nullable) throws SQLException {
        super(label, sqlType, nullable);
        this.setClassName(String.class.getName());
    }

}
