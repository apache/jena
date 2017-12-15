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
package org.apache.jena.obfuscate.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.obfuscate.AbstractObfuscationProvider;
import org.apache.jena.sparql.ARQInternalErrorException;

/**
 * Abstract obfuscation provider that is backed by a cryptographic hash
 */
public class AbstractHashObfuscationProvider extends AbstractObfuscationProvider {

    private final String digestName;
    private MessageDigest digestCache;

    public AbstractHashObfuscationProvider(String digestName) {
        this.digestName = digestName;
        try {
            digestCache = MessageDigest.getInstance(digestName);
        } catch (NoSuchAlgorithmException e) {
            throw new ARQInternalErrorException("Digest not provided in this Java system: " + digestName);
        }
    }

    private MessageDigest getDigest() {
        if (digestCache != null) {
            MessageDigest digest2 = null;
            try {
                digest2 = (MessageDigest) digestCache.clone();
                return digest2;
            } catch (CloneNotSupportedException ex) {
                // Can't clone - remove cache copy.
                digestCache = null;
            }
        }
        return createDigest();
    }

    private MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance(digestName);
        } catch (Exception ex2) {
            throw new ARQInternalErrorException(ex2);
        }
    }

    private String lastSeen, lastCalc;

    @Override
    protected String obfuscate(String value) {
        if (StringUtils.equals(lastSeen, value))
            return lastCalc;

        try {
            MessageDigest digest = getDigest();
            byte b[] = value.getBytes(StandardCharsets.UTF_8);
            byte d[] = digest.digest(b);
            String hash = Bytes.asHexLC(d);

            // Cache
            lastSeen = value;
            lastCalc = hash;

            return hash;

        } catch (Exception ex2) {
            throw new ARQInternalErrorException(ex2);
        }
    }

}
