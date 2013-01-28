/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package org.apache.jena.riot.system;

public class TestFastPrefixMap extends TestLightweightPrefixMap {

    @Override
    protected LightweightPrefixMap getPrefixMap() {
        return new FastPrefixMap();
    }

}
