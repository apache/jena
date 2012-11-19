/*
 * Copyright 2012 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.reasoner.rulesys.test;

import org.junit.Test;

import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.shared.RulesetNotFoundException;
import com.hp.hpl.jena.shared.WrappedIOException;

/**
 * Tests for the rule loader
 * @author rvesse
 *
 */
public class TestRuleLoader  {
    
    @Test(expected=RulesetNotFoundException.class)
    public void load_from_file_uri_non_existent() {
        Rule.rulesFromURL("file:///no-such-file.txt");
    }
    
    @Test(expected=WrappedIOException.class)
    public void load_from_file_bad_encoding() {
        Rule.rulesFromURL("testing/reasoners/bugs/bad-encoding.rules");
    }
}
