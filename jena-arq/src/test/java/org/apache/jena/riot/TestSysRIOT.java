package org.apache.jena.riot;

import org.junit.Assert ;
import org.junit.Test ;

public class TestSysRIOT {
    @Test public void chooseBaseIRI_1() {
        testChooseBaseIRI("http://example/foo/bar", "http://example/foo/bar") ;
    }

    @Test public void chooseBaseIRI_2() {
        testChooseBaseIRI("-", "http://localhost/stdin/") ;
    }

    @Test public void chooseBaseIRI_10() {
        String x = SysRIOT.chooseBaseIRI(null, "foo") ;
        Assert.assertTrue(x.startsWith("file:///"));
    }

    private void testChooseBaseIRI(String input, String expected) {
        String x = SysRIOT.chooseBaseIRI(null, input) ;
        Assert.assertEquals(expected, x) ;
    }
}
