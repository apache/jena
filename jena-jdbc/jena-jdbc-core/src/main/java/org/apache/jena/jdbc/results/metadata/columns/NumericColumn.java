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

/**
 * Abstract column information for numeric columns
 *
 */
public abstract class NumericColumn extends SparqlColumnInfo {

    /**
     * Creates new numeric column information
     * @param label Column Label
     * @param type JDBC type
     * @param nullable Whether the column is nullable
     * @param numericClass Numeric class
     * @param scale Scale
     * @param precision Precision
     * @param signed Whether the column contains signed numbers
     * @throws SQLException Thrown if the column information cannot be created
     */
    public NumericColumn(String label, int type, int nullable, Class<?> numericClass, int scale, int precision, boolean signed) throws SQLException {
        super(label, type, nullable);
        this.setClassName(numericClass.getCanonicalName());
        this.setScale(scale);
        this.setPrecision(precision);
        this.setSigned(signed);
    }

    

}
