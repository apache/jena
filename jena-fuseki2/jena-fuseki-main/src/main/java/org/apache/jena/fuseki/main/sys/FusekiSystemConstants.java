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

package org.apache.jena.fuseki.main.sys;

import org.apache.jena.atlas.lib.PropertyUtils;
import org.apache.jena.fuseki.Fuseki;
import org.eclipse.jetty.server.FormFields;

public class FusekiSystemConstants {

    private static final int jettyMaxFormContentSizeDefault = 25 * 1024 * 1024;

    /**
     * Jetty: ServletContextHandler.setMaxFormContentSize (set in
     * {@link org.apache.jena.fuseki.main.FusekiServer.Builder#buildServletContext})
     * (Jetty 12 default is 200k.)
     */
    public static final int jettyMaxFormContentSize = getValueInt(FormFields.MAX_LENGTH_ATTRIBUTE, jettyMaxFormContentSizeDefault);

    /** Get an integer-valued property */
    private static int getValueInt(String propertyKey, int defaultValue) {
        // Respect jetty constant:
        // "org.eclipse.jetty.server.Request.maxFormContentSize";
        // When set by API, that takes precedence over system property setting.
        try {
            return PropertyUtils.getPropertyAsInteger(System.getProperties(), propertyKey, defaultValue);
        } catch (NumberFormatException ex) {
            Fuseki.configLog.warn("Bad number format for " + propertyKey);
            return defaultValue;
        }
    }

    /**
     * Jetty output buffer size.
     * <p>
     * Setting for HttpConfiguration.setOutputBufferSize (set in
     * {@link JettyLib#httpConfiguration}).
     */
    public static final int jettyOutputBufferSize = 5 * 1024 * 1024;

    /**
     * JettyrRequest header size.
     * <p>
     * Setting for HttpConfiguration.setRequestHeaderSize (set in
     * {@link JettyLib#httpConfiguration}). Space is used "on demand" while reading
     * the HTTP header. (Jetty 12 default is 8k.)
     */
    public static final int jettyRequestHeaderSize = 512 * 1024;

    /**
     * Jetty response header size.
     * <p>
     * Setting for HttpConfiguration.setResponseHeaderSize (set in
     * {@link JettyLib#httpConfiguration}). This is not an "upper bound maximum
     * limit" - the buffer is always allocated at full size pre-allocated, not
     * on-demand.
     * <p>
     * Fuseki does not generate large headers. 16k is the general level of proxy
     * support. (Jetty 12 default is 8k.)
     */
    public static final int jettyResponseHeaderSize = 16 * 1024;
}
