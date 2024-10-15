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

package org.apache.jena.rfc3986;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test relativization of paths
 * See {@link TestRelativeVariants}
 */
public class TestRelativePaths {
    @Test public void relative_path_101() { test_relative_path("/a", "/a/b",  "a/b"); }
    @Test public void relative_path_102() { test_relative_path("/a", "/a/b/", "a/b/"); }
    @Test public void relative_path_103() { test_relative_path("/a", "/a", ""); }
    @Test public void relative_path_104() { test_relative_path("/a", "/a/", "a/"); }
    @Test public void relative_path_105() { test_relative_path("/a", "/z", "z"); }
    @Test public void relative_path_106() { test_relative_path("/a", "/z/", "z/"); }
    @Test public void relative_path_107() { test_relative_path("/a", "/z/y", "z/y"); }
    @Test public void relative_path_108() { test_relative_path("/a", "/a/z", "a/z"); }
    @Test public void relative_path_109() { test_relative_path("/a", "/a/z/", "a/z/"); }

    @Test public void relative_path_200() { test_relative_path("/a/", "/a/b",  "b"); }
    @Test public void relative_path_201() { test_relative_path("/a/", "/a/b/", "b/"); }

    @Test public void relative_path_202() { test_relative_path("/a/", "/a", "/a"); }

    @Test public void relative_path_203() { test_relative_path("/a/", "/a/", ""); }
    @Test public void relative_path_204() { test_relative_path("/a/", "/z", "/z"); }
    @Test public void relative_path_205() { test_relative_path("/a/", "/z/", "/z/"); }
    @Test public void relative_path_206() { test_relative_path("/a/", "/z/y", "/z/y"); }
    @Test public void relative_path_207() { test_relative_path("/a/", "/a/z", "z"); }
    @Test public void relative_path_208() { test_relative_path("/a/", "/a/z/", "z/"); }

    @Test public void relative_path_300() { test_relative_path("/a/b", "/a/b",  ""); }
    @Test public void relative_path_301() { test_relative_path("/a/b", "/a/b/", "b/"); }
    @Test public void relative_path_302() { test_relative_path("/a/b", "/a", "/a"); }
    @Test public void relative_path_303() { test_relative_path("/a/b", "/a/", "."); }
    @Test public void relative_path_304() { test_relative_path("/a/b", "/a/z", "z"); }
    @Test public void relative_path_305() { test_relative_path("/a/b", "/a/z/", "z/"); }
    @Test public void relative_path_306() { test_relative_path("/a/b", "/z", "/z"); }
    @Test public void relative_path_307() { test_relative_path("/a/b", "/z/", "/z/"); }
    @Test public void relative_path_308() { test_relative_path("/a/b/", "/a/z/e", "../z/e"); }

    // Special handling case 1
    @Test public void relative_path_400() { test_relative_path("/a/b/", "/a/b", "../b");}
    @Test public void relative_path_401() { test_relative_path("/a/b/", "/a/b/", ""); }
    @Test public void relative_path_402() { test_relative_path("/a/b/", "/a", "/a"); }
    @Test public void relative_path_403() { test_relative_path("/a/b/", "/a/", ".."); }
    @Test public void relative_path_404() { test_relative_path("/a/b/", "/z", "/z"); }
    @Test public void relative_path_405() { test_relative_path("/a/b/", "/z/", "/z/"); }

    @Test public void relative_path_500() { test_relative_path("/", "/a",   "a"); }
    @Test public void relative_path_501() { test_relative_path("/", "/a/",  "a/"); }
    @Test public void relative_path_502() { test_relative_path("/", "/a/b", "a/b"); }
    @Test public void relative_path_503() { test_relative_path("/", "/a/b/", "a/b/"); }
    @Test public void relative_path_504() { test_relative_path("/a/b/", "/a/z/e", "../z/e"); }

    @Test public void relative_path_600() { test_relative_path("/a/b/c", "/a/z", "../z"); }
    // Grandparent
    @Test public void relative_path_601() { test_relative_path("/a/b/c/", "/a/z", "/a/z");}
    @Test public void relative_path_602() { test_relative_path("/a/b/c", "/a", "/a"); }
    @Test public void relative_path_603() { test_relative_path("/a/b/c", "/a/", ".."); }

    // #45 jena-iri ../../c
    @Test public void relative_path_700() { test_relative_path("/a/b/c/d/", "/a/b/c", "/a/b/c"); }
    // Special handling case 2
    @Test public void relative_path_701() { test_relative_path("/a/b/c/d/", "/a/b/c/", ".."); }
    // jena-iri does grandparent.
    @Test public void relative_path_702() { test_relative_path("/a/b/c/d/", "/a/b/e", "/a/b/e"); }
    @Test public void relative_path_703() { test_relative_path("/a/b/c/d/", "/a/b", "/a/b"); }
    // jena-iri does grandparent.
    @Test public void relative_path_704() { test_relative_path("/a/b/c/d/", "/a/b/", "/a/b/");}
    @Test public void relative_path_705() { test_relative_path("/a/b/c/d/", "/a/", "/a/"); }
    @Test public void relative_path_706() { test_relative_path("/a/b/c/d/", "/a", "/a"); }

    // Special handling case 1
    @Test public void relative_path_800() { test_relative_path("/a/b/c/d", "/a/b/c", "../c");}
    @Test public void relative_path_801() { test_relative_path("/a/b/c/d", "/a/b/c/", "."); }
    @Test public void relative_path_802() { test_relative_path("/a/b/c/d", "/a/b/e", "../e"); }
    // jena-iri does grandparent.
    @Test public void relative_path_803() { test_relative_path("/a/b/c/d", "/a/b", "/a/b"); }
    // Special handling case 2
    @Test public void relative_path_804() { test_relative_path("/a/b/c/d", "/a/b/", ".."); }
    // jena-iri does grandparent.
    @Test public void relative_path_805() { test_relative_path("/a/b/c/d", "/a/", "/a/");}
    @Test public void relative_path_806() { test_relative_path("/a/b/c/d", "/a", "/a"); }
    @Test public void relative_path_807() { test_relative_path("/a/b/c/d", "/a/b/x", "../x"); }
    @Test public void relative_path_808() { test_relative_path("/a/b/c/d", "/a/b/x/", "../x/"); }
    // jena-iri does grandparent.
    @Test public void relative_path_809() { test_relative_path("/a/b/c/d", "/a/x/", "/a/x/"); }
    // jena-iri does grandparent.
    @Test public void relative_path_810() { test_relative_path("/a/b/c/d", "/a/x", "/a/x"); }

    @Test public void relative_path_900() { test_relative_path("/a/b/c/d/e", "/a/b", "/a/b"); }
    // jena-iri does grandparent.
    @Test public void relative_path_901() { test_relative_path("/a/b/c/d/e", "/a/b/", "/a/b/");}
    @Test public void relative_path_902() { test_relative_path("/a/b/c/d/e", "/a/", "/a/"); }
    @Test public void relative_path_903() { test_relative_path("/a/b/c/d/e", "/a", "/a"); }

    private static void test_relative_path(String basePath, String targetPath, String expected) {
        // Make into an absolute IRI.
        String baseIRI = asIRI(basePath);
        String targetIRI = asIRI(targetPath);

        IRI3986 base = IRI3986.create(baseIRI);
        IRI3986 target = IRI3986.create(targetIRI);
        IRI3986 rel = base.relativize(target);
        String relStr = (rel != null ) ? rel.str() : null;

        if ( ! Objects.equals(relStr, expected) ) {
            System.out.printf("Fail: base=%-20s : iri=%-20s => got: %s, expected: %s\n", base, target, enclose(relStr), enclose(expected));
            assertEquals(relStr, expected);
            return;
        }
        // And back again.
        IRI3986 iri = base.resolve(rel);
        assertEquals(iri, target);
    }

    private static String asIRI(String path) {
        return "http://example"+path;
    }

    private static String enclose(String x) {
        if ( x == null )
            return "<null>";
        return "|"+x+"|";
    }
}
