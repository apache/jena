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

package org.apache.jena.jdbc.remote;

import java.sql.SQLException;
import java.util.Properties;

import org.apache.jena.jdbc.AbstractJenaDriverTests;
import org.apache.jena.jdbc.JenaDriver;
import org.apache.jena.jdbc.remote.connections.RemoteEndpointConnection;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link RemoteEndpointDriver}
 * 
 */
public class TestRemoteEndpointDriver extends AbstractJenaDriverTests {

    @Override
    protected JenaDriver getDriver() {
        return new RemoteEndpointDriver();
    }

    @Override
    protected String getConnectionUrl() {
        return JenaDriver.DRIVER_PREFIX + RemoteEndpointDriver.REMOTE_DRIVER_PREFIX + RemoteEndpointDriver.PARAM_QUERY_ENDPOINT
                + "=http://example.org/query&" + RemoteEndpointDriver.PARAM_UPDATE_ENDPOINT + "=http://example.org/update";
    }

    @Override
    protected String getBadConnectionUrl() {
        return JenaDriver.DRIVER_PREFIX + RemoteEndpointDriver.REMOTE_DRIVER_PREFIX;
    }

    /**
     * Tests the different ways of specifying multiple values for a parameter
     * @throws SQLException 
     */
    @Test
    public void remote_driver_graph_uris_01() throws SQLException {
        // May specify key=value pairs multiple times
        String url = JenaDriver.DRIVER_PREFIX + RemoteEndpointDriver.REMOTE_DRIVER_PREFIX + RemoteEndpointDriver.PARAM_QUERY_ENDPOINT + "=http://example.org/query&" + RemoteEndpointDriver.PARAM_DEFAULT_GRAPH_URI + "=http://graph/1&" + RemoteEndpointDriver.PARAM_DEFAULT_GRAPH_URI + "=http://graph/2";
        RemoteEndpointDriver driver = (RemoteEndpointDriver)this.getDriver();
        RemoteEndpointConnection conn = (RemoteEndpointConnection) driver.connect(url, new Properties());
        
        Assert.assertEquals(2, conn.getDefaultGraphURIs().size());
        conn.close();
    }
    
    /**
     * Tests the different ways of specifying multiple values for a parameter
     * @throws SQLException 
     */
    @Test
    public void remote_driver_graph_uris_02() throws SQLException {
        // May specify key=value,value as comma separated list
        String url = JenaDriver.DRIVER_PREFIX + RemoteEndpointDriver.REMOTE_DRIVER_PREFIX + RemoteEndpointDriver.PARAM_QUERY_ENDPOINT + "=http://example.org/query&" + RemoteEndpointDriver.PARAM_DEFAULT_GRAPH_URI + "=http://graph/1,http://graph/2";
        RemoteEndpointDriver driver = (RemoteEndpointDriver)this.getDriver();
        RemoteEndpointConnection conn = (RemoteEndpointConnection) driver.connect(url, new Properties());
        
        Assert.assertEquals(2, conn.getDefaultGraphURIs().size());
        conn.close();
    }
    
    /**
     * Tests the different ways of specifying multiple values for a parameter
     * @throws SQLException 
     */
    @Test
    public void remote_driver_graph_uris_03() throws SQLException {
        // May specify combination of multiple key=value pairs and key=value,value comma separated list(s)
        String url = JenaDriver.DRIVER_PREFIX + RemoteEndpointDriver.REMOTE_DRIVER_PREFIX + RemoteEndpointDriver.PARAM_QUERY_ENDPOINT + "=http://example.org/query&" + RemoteEndpointDriver.PARAM_DEFAULT_GRAPH_URI + "=http://graph/1,http://graph/2&" + RemoteEndpointDriver.PARAM_DEFAULT_GRAPH_URI + "=http://graph/3";
        RemoteEndpointDriver driver = (RemoteEndpointDriver)this.getDriver();
        RemoteEndpointConnection conn = (RemoteEndpointConnection) driver.connect(url, new Properties());
        
        Assert.assertEquals(3, conn.getDefaultGraphURIs().size());
        conn.close();
    }
}
