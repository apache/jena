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

package org.apache.jena.util;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.shared.JenaException;

/** Create XML Outptut methods. */
public class JenaXMLOutput {
    // JenaXMLOutput.

    /**
     * Create a new {@link Transformer}.
     * <p>
     * This transformer has "feature secure processing" enabled and also does not
     * allow external DTDs or external stylesheets.
     */
    public static Transformer xmlTransformer() {
        try {
            return xmlTransformerFactory.newTransformer();
        } catch (TransformerConfigurationException ex) {
            Log.error(JenaXMLOutput.class, "Failed to build a javax.xml.transform.Transformer", ex);
            throw new JenaException(ex);
        }
    }

    private static TransformerFactory xmlTransformerFactory = xmlTransformerFactory();
    private static TransformerFactory xmlTransformerFactory() {
        try {
            TransformerFactory xmlTransformerFactory = TransformerFactory.newInstance();
            xmlTransformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            attempt(()->xmlTransformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""));
            // Not implemented by com.sun.org.apache.xalan.internal (OpenJDK)
            //attempt(()->xmlTransformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""));
            attempt(()->xmlTransformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""));
            return xmlTransformerFactory;
        } catch (TransformerConfigurationException ex) {
            Log.error(JenaXMLOutput.class, "Failed to build a javax.xml.transform.TransformerFactory", ex);
            return null;
        }
    }

    /** Attempt to run a {@link Runnable}. Ignore {@link IllegalArgumentException}. */
    private static void attempt(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException ex) {}
    }
}
