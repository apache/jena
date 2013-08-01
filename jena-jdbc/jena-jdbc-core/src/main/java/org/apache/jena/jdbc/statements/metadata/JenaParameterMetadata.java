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

package org.apache.jena.jdbc.statements.metadata;

import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.jdbc.statements.JenaPreparedStatement;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ParameterizedSparqlString;

/**
 * Implementation of {@link ParameterMetaData} for {@link ParameterizedSparqlString} instances to support {@link JenaPreparedStatement}
 *
 */
public class JenaParameterMetadata implements ParameterMetaData {
    
    private ParameterizedSparqlString sparqlStr;
    private int paramCount;
    
    /**
     * Creates new parameter metadata
     * @param sparqlStr Parameterized SPARQL String
     * @throws SQLException
     */
    public JenaParameterMetadata(ParameterizedSparqlString sparqlStr) throws SQLException {
        if (sparqlStr == null) throw new SQLException("Parameterized SPARQL String cannot be null");
        this.sparqlStr = sparqlStr;
        this.paramCount = (int) Iter.count(this.sparqlStr.getEligiblePositionalParameters());
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public String getParameterClassName(int param) throws SQLException {
        // Remember that JDBC used a 1 based index
        if (param < 1 || param > this.paramCount) throw new SQLException("Parameter Index is out of bounds");
        // All parameters are typed as Node
        return Node.class.getCanonicalName();
    }

    @Override
    public int getParameterCount() throws SQLException {
        return this.paramCount;
    }

    @Override
    public int getParameterMode(int param) throws SQLException {
        // Remember that JDBC used a 1 based index
        if (param < 1 || param > this.paramCount) throw new SQLException("Parameter Index is out of bounds");
        return parameterModeIn;
    }

    @Override
    public int getParameterType(int param) throws SQLException {
        // Remember that JDBC used a 1 based index
        if (param < 1 || param > this.paramCount) throw new SQLException("Parameter Index is out of bounds");
        // Treat all parameters as being typed as Java Objects
        return Types.JAVA_OBJECT;
    }

    @Override
    public String getParameterTypeName(int param) throws SQLException {
        // Remember that JDBC used a 1 based index
        if (param < 1 || param > this.paramCount) throw new SQLException("Parameter Index is out of bounds");
        // All parameters are typed as Node
        return Node.class.getCanonicalName();
    }

    @Override
    public int getPrecision(int param) throws SQLException {
        // Remember that JDBC used a 1 based index
        if (param < 1 || param > this.paramCount) throw new SQLException("Parameter Index is out of bounds");
        // Return zero since parameters aren't typed as numerics
        return 0;
    }

    @Override
    public int getScale(int param) throws SQLException {
        // Remember that JDBC used a 1 based index
        if (param < 1 || param > this.paramCount) throw new SQLException("Parameter Index is out of bounds");
        // Return zero since parameters aren't typed as numerics
        return 0;
    }

    @Override
    public int isNullable(int param) throws SQLException {
        // Remember that JDBC used a 1 based index
        if (param < 1 || param > this.paramCount) throw new SQLException("Parameter Index is out of bounds");
        // Parameters are not nullable
        return parameterNoNulls;
    }

    @Override
    public boolean isSigned(int param) throws SQLException {
        // Remember that JDBC used a 1 based index
        if (param < 1 || param > this.paramCount) throw new SQLException("Parameter Index is out of bounds");
        // Return false since parameters aren't typed as numerics
        return false;
    }

}
