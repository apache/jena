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

import java.util.function.BiFunction;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Generated from TestRelativeVariants_JenaIRI
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestRelativeVariants {

    private static void executeTest(String iriStr1, String iriStr2, BiFunction<IRI3986, IRI3986, IRI3986> function, String expectedStr) {
        IRI3986 base = IRI3986.create(iriStr1);
        IRI3986 target = IRI3986.create(iriStr2);
        IRI3986 r = function.apply(base, target);
        IRI3986 expected = expectedStr==null ? null : IRI3986.create(expectedStr);
        assertEquals(expected, r);
    }

    // Generated from here to the end of the class

    @Test public void test_rel_100_relativize() {
        executeTest("http://example/dir", "http://example/path", AlgResolveIRI::relativize, "path");
    }

    @Test public void test_rel_100_relativeScheme() {
        executeTest("http://example/dir", "http://example/path", AlgRelativizeIRI::relativeScheme, "//example/path");
    }

    @Test public void test_rel_100_relativeResource() {
        executeTest("http://example/dir", "http://example/path", AlgRelativizeIRI::relativeResource, "/path");
    }

    @Test public void test_rel_100_relativePath() {
        executeTest("http://example/dir", "http://example/path", AlgRelativizeIRI::relativePath, "path");
    }

    @Test public void test_rel_100_relativeParentPath() {
        executeTest("http://example/dir", "http://example/path", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_100_relativeSameDocument() {
        executeTest("http://example/dir", "http://example/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_101_relativize() {
        executeTest("http://example/dir", "http://example/dir/path", AlgResolveIRI::relativize, "dir/path");
    }

    @Test public void test_rel_101_relativeScheme() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativeScheme, "//example/dir/path");
    }

    @Test public void test_rel_101_relativeResource() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativeResource, "/dir/path");
    }

    @Test public void test_rel_101_relativePath() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativePath, "dir/path");
    }

    @Test public void test_rel_101_relativeParentPath() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_101_relativeSameDocument() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_102_relativize() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgResolveIRI::relativize, "path");
    }

    @Test public void test_rel_102_relativeScheme() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativeScheme, "//example/dir/path");
    }

    @Test public void test_rel_102_relativeResource() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativeResource, "/dir/path");
    }

    @Test public void test_rel_102_relativePath() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativePath, "path");
    }

    @Test public void test_rel_102_relativeParentPath() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativeParentPath, "../dir/path");
    }

    @Test public void test_rel_102_relativeSameDocument() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_103_relativize() {
        executeTest("http://example/dir/file", "http://example/path", AlgResolveIRI::relativize, "/path");
    }

    @Test public void test_rel_103_relativeScheme() {
        executeTest("http://example/dir/file", "http://example/path", AlgRelativizeIRI::relativeScheme, "//example/path");
    }

    @Test public void test_rel_103_relativeResource() {
        executeTest("http://example/dir/file", "http://example/path", AlgRelativizeIRI::relativeResource, "/path");
    }

    @Test public void test_rel_103_relativePath() {
        executeTest("http://example/dir/file", "http://example/path", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_103_relativeParentPath() {
        executeTest("http://example/dir/file", "http://example/path", AlgRelativizeIRI::relativeParentPath, "../path");
    }

    @Test public void test_rel_103_relativeSameDocument() {
        executeTest("http://example/dir/file", "http://example/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_200_relativize() {
        executeTest("http://example/z/dir/", "http://example/z/alt/", AlgResolveIRI::relativize, "../alt/");
    }

    @Test public void test_rel_200_relativeScheme() {
        executeTest("http://example/z/dir/", "http://example/z/alt/", AlgRelativizeIRI::relativeScheme, "//example/z/alt/");
    }

    @Test public void test_rel_200_relativeResource() {
        executeTest("http://example/z/dir/", "http://example/z/alt/", AlgRelativizeIRI::relativeResource, "/z/alt/");
    }

    @Test public void test_rel_200_relativePath() {
        executeTest("http://example/z/dir/", "http://example/z/alt/", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_200_relativeParentPath() {
        executeTest("http://example/z/dir/", "http://example/z/alt/", AlgRelativizeIRI::relativeParentPath, "../alt/");
    }

    @Test public void test_rel_200_relativeSameDocument() {
        executeTest("http://example/z/dir/", "http://example/z/alt/", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_201_relativize() {
        executeTest("http://example/z/dir/", "http://example/z/alt/path", AlgResolveIRI::relativize, "../alt/path");
    }

    @Test public void test_rel_201_relativeScheme() {
        executeTest("http://example/z/dir/", "http://example/z/alt/path", AlgRelativizeIRI::relativeScheme, "//example/z/alt/path");
    }

    @Test public void test_rel_201_relativeResource() {
        executeTest("http://example/z/dir/", "http://example/z/alt/path", AlgRelativizeIRI::relativeResource, "/z/alt/path");
    }

    @Test public void test_rel_201_relativePath() {
        executeTest("http://example/z/dir/", "http://example/z/alt/path", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_201_relativeParentPath() {
        executeTest("http://example/z/dir/", "http://example/z/alt/path", AlgRelativizeIRI::relativeParentPath, "../alt/path");
    }

    @Test public void test_rel_201_relativeSameDocument() {
        executeTest("http://example/z/dir/", "http://example/z/alt/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_202_relativize() {
        executeTest("http://example/z/dir/def", "http://example/z/alt/path", AlgResolveIRI::relativize, "../alt/path");
    }

    @Test public void test_rel_202_relativeScheme() {
        executeTest("http://example/z/dir/def", "http://example/z/alt/path", AlgRelativizeIRI::relativeScheme, "//example/z/alt/path");
    }

    @Test public void test_rel_202_relativeResource() {
        executeTest("http://example/z/dir/def", "http://example/z/alt/path", AlgRelativizeIRI::relativeResource, "/z/alt/path");
    }

    @Test public void test_rel_202_relativePath() {
        executeTest("http://example/z/dir/def", "http://example/z/alt/path", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_202_relativeParentPath() {
        executeTest("http://example/z/dir/def", "http://example/z/alt/path", AlgRelativizeIRI::relativeParentPath, "../alt/path");
    }

    @Test public void test_rel_202_relativeSameDocument() {
        executeTest("http://example/z/dir/def", "http://example/z/alt/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_203_relativize() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/", AlgResolveIRI::relativize, "../alt/");
    }

    @Test public void test_rel_203_relativeScheme() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/", AlgRelativizeIRI::relativeScheme, "//example/a/z/alt/");
    }

    @Test public void test_rel_203_relativeResource() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/", AlgRelativizeIRI::relativeResource, "/a/z/alt/");
    }

    @Test public void test_rel_203_relativePath() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_203_relativeParentPath() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/", AlgRelativizeIRI::relativeParentPath, "../alt/");
    }

    @Test public void test_rel_203_relativeSameDocument() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_204_relativize() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/path", AlgResolveIRI::relativize, "../alt/path");
    }

    @Test public void test_rel_204_relativeScheme() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/path", AlgRelativizeIRI::relativeScheme, "//example/a/z/alt/path");
    }

    @Test public void test_rel_204_relativeResource() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/path", AlgRelativizeIRI::relativeResource, "/a/z/alt/path");
    }

    @Test public void test_rel_204_relativePath() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/path", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_204_relativeParentPath() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/path", AlgRelativizeIRI::relativeParentPath, "../alt/path");
    }

    @Test public void test_rel_204_relativeSameDocument() {
        executeTest("http://example/a/z/dir/", "http://example/a/z/alt/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_205_relativize() {
        executeTest("http://example/a/z/dir/def", "http://example/a/z/alt/path", AlgResolveIRI::relativize, "../alt/path");
    }

    @Test public void test_rel_205_relativeScheme() {
        executeTest("http://example/a/z/dir/def", "http://example/a/z/alt/path", AlgRelativizeIRI::relativeScheme, "//example/a/z/alt/path");
    }

    @Test public void test_rel_205_relativeResource() {
        executeTest("http://example/a/z/dir/def", "http://example/a/z/alt/path", AlgRelativizeIRI::relativeResource, "/a/z/alt/path");
    }

    @Test public void test_rel_205_relativePath() {
        executeTest("http://example/a/z/dir/def", "http://example/a/z/alt/path", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_205_relativeParentPath() {
        executeTest("http://example/a/z/dir/def", "http://example/a/z/alt/path", AlgRelativizeIRI::relativeParentPath, "../alt/path");
    }

    @Test public void test_rel_205_relativeSameDocument() {
        executeTest("http://example/a/z/dir/def", "http://example/a/z/alt/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_300_relativize() {
        executeTest("http://example/abc", "http://example/abc#frag", AlgResolveIRI::relativize, "#frag");
    }

    @Test public void test_rel_300_relativeScheme() {
        executeTest("http://example/abc", "http://example/abc#frag", AlgRelativizeIRI::relativeScheme, "//example/abc#frag");
    }

    @Test public void test_rel_300_relativeResource() {
        executeTest("http://example/abc", "http://example/abc#frag", AlgRelativizeIRI::relativeResource, "/abc#frag");
    }

    @Test public void test_rel_300_relativePath() {
        executeTest("http://example/abc", "http://example/abc#frag", AlgRelativizeIRI::relativePath, "#frag");
    }

    @Test public void test_rel_300_relativeParentPath() {
        executeTest("http://example/abc", "http://example/abc#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_300_relativeSameDocument() {
        executeTest("http://example/abc", "http://example/abc#frag", AlgRelativizeIRI::relativeSameDocument, "#frag");
    }

    @Test public void test_rel_301_relativize() {
        executeTest("http://example/abc", "http://example/abc/file", AlgResolveIRI::relativize, "abc/file");
    }

    @Test public void test_rel_301_relativeScheme() {
        executeTest("http://example/abc", "http://example/abc/file", AlgRelativizeIRI::relativeScheme, "//example/abc/file");
    }

    @Test public void test_rel_301_relativeResource() {
        executeTest("http://example/abc", "http://example/abc/file", AlgRelativizeIRI::relativeResource, "/abc/file");
    }

    @Test public void test_rel_301_relativePath() {
        executeTest("http://example/abc", "http://example/abc/file", AlgRelativizeIRI::relativePath, "abc/file");
    }

    @Test public void test_rel_301_relativeParentPath() {
        executeTest("http://example/abc", "http://example/abc/file", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_301_relativeSameDocument() {
        executeTest("http://example/abc", "http://example/abc/file", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_302_relativize() {
        executeTest("http://example/abc", "http://example/abc?query", AlgResolveIRI::relativize, "?query");
    }

    @Test public void test_rel_302_relativeScheme() {
        executeTest("http://example/abc", "http://example/abc?query", AlgRelativizeIRI::relativeScheme, "//example/abc?query");
    }

    @Test public void test_rel_302_relativeResource() {
        executeTest("http://example/abc", "http://example/abc?query", AlgRelativizeIRI::relativeResource, "/abc?query");
    }

    @Test public void test_rel_302_relativePath() {
        executeTest("http://example/abc", "http://example/abc?query", AlgRelativizeIRI::relativePath, "?query");
    }

    @Test public void test_rel_302_relativeParentPath() {
        executeTest("http://example/abc", "http://example/abc?query", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_302_relativeSameDocument() {
        executeTest("http://example/abc", "http://example/abc?query", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_303_relativize() {
        executeTest("http://example/abc", "http://example/abc?query#frag", AlgResolveIRI::relativize, "?query#frag");
    }

    @Test public void test_rel_303_relativeScheme() {
        executeTest("http://example/abc", "http://example/abc?query#frag", AlgRelativizeIRI::relativeScheme, "//example/abc?query#frag");
    }

    @Test public void test_rel_303_relativeResource() {
        executeTest("http://example/abc", "http://example/abc?query#frag", AlgRelativizeIRI::relativeResource, "/abc?query#frag");
    }

    @Test public void test_rel_303_relativePath() {
        executeTest("http://example/abc", "http://example/abc?query#frag", AlgRelativizeIRI::relativePath, "?query#frag");
    }

    @Test public void test_rel_303_relativeParentPath() {
        executeTest("http://example/abc", "http://example/abc?query#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_303_relativeSameDocument() {
        executeTest("http://example/abc", "http://example/abc?query#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_304_relativize() {
        executeTest("http://example/abc", "http://example/abc/#frag", AlgResolveIRI::relativize, "abc/#frag");
    }

    @Test public void test_rel_304_relativeScheme() {
        executeTest("http://example/abc", "http://example/abc/#frag", AlgRelativizeIRI::relativeScheme, "//example/abc/#frag");
    }

    @Test public void test_rel_304_relativeResource() {
        executeTest("http://example/abc", "http://example/abc/#frag", AlgRelativizeIRI::relativeResource, "/abc/#frag");
    }

    @Test public void test_rel_304_relativePath() {
        executeTest("http://example/abc", "http://example/abc/#frag", AlgRelativizeIRI::relativePath, "abc/#frag");
    }

    @Test public void test_rel_304_relativeParentPath() {
        executeTest("http://example/abc", "http://example/abc/#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_304_relativeSameDocument() {
        executeTest("http://example/abc", "http://example/abc/#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_305_relativize() {
        executeTest("http://example/abc", "http://example/abc/?query", AlgResolveIRI::relativize, "abc/?query");
    }

    @Test public void test_rel_305_relativeScheme() {
        executeTest("http://example/abc", "http://example/abc/?query", AlgRelativizeIRI::relativeScheme, "//example/abc/?query");
    }

    @Test public void test_rel_305_relativeResource() {
        executeTest("http://example/abc", "http://example/abc/?query", AlgRelativizeIRI::relativeResource, "/abc/?query");
    }

    @Test public void test_rel_305_relativePath() {
        executeTest("http://example/abc", "http://example/abc/?query", AlgRelativizeIRI::relativePath, "abc/?query");
    }

    @Test public void test_rel_305_relativeParentPath() {
        executeTest("http://example/abc", "http://example/abc/?query", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_305_relativeSameDocument() {
        executeTest("http://example/abc", "http://example/abc/?query", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_306_relativize() {
        executeTest("http://example/abc", "http://example/abc/?query#frag", AlgResolveIRI::relativize, "abc/?query#frag");
    }

    @Test public void test_rel_306_relativeScheme() {
        executeTest("http://example/abc", "http://example/abc/?query#frag", AlgRelativizeIRI::relativeScheme, "//example/abc/?query#frag");
    }

    @Test public void test_rel_306_relativeResource() {
        executeTest("http://example/abc", "http://example/abc/?query#frag", AlgRelativizeIRI::relativeResource, "/abc/?query#frag");
    }

    @Test public void test_rel_306_relativePath() {
        executeTest("http://example/abc", "http://example/abc/?query#frag", AlgRelativizeIRI::relativePath, "abc/?query#frag");
    }

    @Test public void test_rel_306_relativeParentPath() {
        executeTest("http://example/abc", "http://example/abc/?query#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_306_relativeSameDocument() {
        executeTest("http://example/abc", "http://example/abc/?query#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_400_relativize() {
        executeTest("http://example/abc/", "http://example/abc", AlgResolveIRI::relativize, "/abc");
    }

    @Test public void test_rel_400_relativeScheme() {
        executeTest("http://example/abc/", "http://example/abc", AlgRelativizeIRI::relativeScheme, "//example/abc");
    }

    @Test public void test_rel_400_relativeResource() {
        executeTest("http://example/abc/", "http://example/abc", AlgRelativizeIRI::relativeResource, "/abc");
    }

    @Test public void test_rel_400_relativePath() {
        executeTest("http://example/abc/", "http://example/abc", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_400_relativeParentPath() {
        executeTest("http://example/abc/", "http://example/abc", AlgRelativizeIRI::relativeParentPath, "../abc");
    }

    @Test public void test_rel_400_relativeSameDocument() {
        executeTest("http://example/abc/", "http://example/abc", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_401_relativize() {
        executeTest("http://example/abc/", "http://example/abc/", AlgResolveIRI::relativize, "");
    }

    @Test public void test_rel_401_relativeScheme() {
        executeTest("http://example/abc/", "http://example/abc/", AlgRelativizeIRI::relativeScheme, "//example/abc/");
    }

    @Test public void test_rel_401_relativeResource() {
        executeTest("http://example/abc/", "http://example/abc/", AlgRelativizeIRI::relativeResource, "/abc/");
    }

    @Test public void test_rel_401_relativePath() {
        executeTest("http://example/abc/", "http://example/abc/", AlgRelativizeIRI::relativePath, ".");
    }

    @Test public void test_rel_401_relativeParentPath() {
        executeTest("http://example/abc/", "http://example/abc/", AlgRelativizeIRI::relativeParentPath, "../abc/");
    }

    @Test public void test_rel_401_relativeSameDocument() {
        executeTest("http://example/abc/", "http://example/abc/", AlgRelativizeIRI::relativeSameDocument, "");
    }

    @Test public void test_rel_402_relativize() {
        executeTest("http://example/abc/", "http://example/abc/file", AlgResolveIRI::relativize, "file");
    }

    @Test public void test_rel_402_relativeScheme() {
        executeTest("http://example/abc/", "http://example/abc/file", AlgRelativizeIRI::relativeScheme, "//example/abc/file");
    }

    @Test public void test_rel_402_relativeResource() {
        executeTest("http://example/abc/", "http://example/abc/file", AlgRelativizeIRI::relativeResource, "/abc/file");
    }

    @Test public void test_rel_402_relativePath() {
        executeTest("http://example/abc/", "http://example/abc/file", AlgRelativizeIRI::relativePath, "file");
    }

    @Test public void test_rel_402_relativeParentPath() {
        executeTest("http://example/abc/", "http://example/abc/file", AlgRelativizeIRI::relativeParentPath, "../abc/file");
    }

    @Test public void test_rel_402_relativeSameDocument() {
        executeTest("http://example/abc/", "http://example/abc/file", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_403_relativize() {
        executeTest("http://example/abc/", "http://example/abc#frag", AlgResolveIRI::relativize, "/abc#frag");
    }

    @Test public void test_rel_403_relativeScheme() {
        executeTest("http://example/abc/", "http://example/abc#frag", AlgRelativizeIRI::relativeScheme, "//example/abc#frag");
    }

    @Test public void test_rel_403_relativeResource() {
        executeTest("http://example/abc/", "http://example/abc#frag", AlgRelativizeIRI::relativeResource, "/abc#frag");
    }

    @Test public void test_rel_403_relativePath() {
        executeTest("http://example/abc/", "http://example/abc#frag", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_403_relativeParentPath() {
        executeTest("http://example/abc/", "http://example/abc#frag", AlgRelativizeIRI::relativeParentPath, "../abc#frag");
    }

    @Test public void test_rel_403_relativeSameDocument() {
        executeTest("http://example/abc/", "http://example/abc#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_404_relativize() {
        executeTest("http://example/abc/", "http://example/abc?query", AlgResolveIRI::relativize, "/abc?query");
    }

    @Test public void test_rel_404_relativeScheme() {
        executeTest("http://example/abc/", "http://example/abc?query", AlgRelativizeIRI::relativeScheme, "//example/abc?query");
    }

    @Test public void test_rel_404_relativeResource() {
        executeTest("http://example/abc/", "http://example/abc?query", AlgRelativizeIRI::relativeResource, "/abc?query");
    }

    @Test public void test_rel_404_relativePath() {
        executeTest("http://example/abc/", "http://example/abc?query", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_404_relativeParentPath() {
        executeTest("http://example/abc/", "http://example/abc?query", AlgRelativizeIRI::relativeParentPath, "../abc?query");
    }

    @Test public void test_rel_404_relativeSameDocument() {
        executeTest("http://example/abc/", "http://example/abc?query", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_405_relativize() {
        executeTest("http://example/abc/", "http://example/abc?query#frag", AlgResolveIRI::relativize, "/abc?query#frag");
    }

    @Test public void test_rel_405_relativeScheme() {
        executeTest("http://example/abc/", "http://example/abc?query#frag", AlgRelativizeIRI::relativeScheme, "//example/abc?query#frag");
    }

    @Test public void test_rel_405_relativeResource() {
        executeTest("http://example/abc/", "http://example/abc?query#frag", AlgRelativizeIRI::relativeResource, "/abc?query#frag");
    }

    @Test public void test_rel_405_relativePath() {
        executeTest("http://example/abc/", "http://example/abc?query#frag", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_405_relativeParentPath() {
        executeTest("http://example/abc/", "http://example/abc?query#frag", AlgRelativizeIRI::relativeParentPath, "../abc?query#frag");
    }

    @Test public void test_rel_405_relativeSameDocument() {
        executeTest("http://example/abc/", "http://example/abc?query#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_406_relativize() {
        executeTest("http://example/abc/", "http://example/xyz#frag", AlgResolveIRI::relativize, "/xyz#frag");
    }

    @Test public void test_rel_406_relativeScheme() {
        executeTest("http://example/abc/", "http://example/xyz#frag", AlgRelativizeIRI::relativeScheme, "//example/xyz#frag");
    }

    @Test public void test_rel_406_relativeResource() {
        executeTest("http://example/abc/", "http://example/xyz#frag", AlgRelativizeIRI::relativeResource, "/xyz#frag");
    }

    @Test public void test_rel_406_relativePath() {
        executeTest("http://example/abc/", "http://example/xyz#frag", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_406_relativeParentPath() {
        executeTest("http://example/abc/", "http://example/xyz#frag", AlgRelativizeIRI::relativeParentPath, "../xyz#frag");
    }

    @Test public void test_rel_406_relativeSameDocument() {
        executeTest("http://example/abc/", "http://example/xyz#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_407_relativize() {
        executeTest("http://example/abc/", "http://example/xyz?query", AlgResolveIRI::relativize, "/xyz?query");
    }

    @Test public void test_rel_407_relativeScheme() {
        executeTest("http://example/abc/", "http://example/xyz?query", AlgRelativizeIRI::relativeScheme, "//example/xyz?query");
    }

    @Test public void test_rel_407_relativeResource() {
        executeTest("http://example/abc/", "http://example/xyz?query", AlgRelativizeIRI::relativeResource, "/xyz?query");
    }

    @Test public void test_rel_407_relativePath() {
        executeTest("http://example/abc/", "http://example/xyz?query", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_407_relativeParentPath() {
        executeTest("http://example/abc/", "http://example/xyz?query", AlgRelativizeIRI::relativeParentPath, "../xyz?query");
    }

    @Test public void test_rel_407_relativeSameDocument() {
        executeTest("http://example/abc/", "http://example/xyz?query", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_408_relativize() {
        executeTest("http://example/abc/", "http://example/xyz?query#frag", AlgResolveIRI::relativize, "/xyz?query#frag");
    }

    @Test public void test_rel_408_relativeScheme() {
        executeTest("http://example/abc/", "http://example/xyz?query#frag", AlgRelativizeIRI::relativeScheme, "//example/xyz?query#frag");
    }

    @Test public void test_rel_408_relativeResource() {
        executeTest("http://example/abc/", "http://example/xyz?query#frag", AlgRelativizeIRI::relativeResource, "/xyz?query#frag");
    }

    @Test public void test_rel_408_relativePath() {
        executeTest("http://example/abc/", "http://example/xyz?query#frag", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_408_relativeParentPath() {
        executeTest("http://example/abc/", "http://example/xyz?query#frag", AlgRelativizeIRI::relativeParentPath, "../xyz?query#frag");
    }

    @Test public void test_rel_408_relativeSameDocument() {
        executeTest("http://example/abc/", "http://example/xyz?query#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_501_relativize() {
        executeTest("http://example/abc#", "http://example/abc", AlgResolveIRI::relativize, "");
    }

    @Test public void test_rel_501_relativeScheme() {
        executeTest("http://example/abc#", "http://example/abc", AlgRelativizeIRI::relativeScheme, "//example/abc");
    }

    @Test public void test_rel_501_relativeResource() {
        executeTest("http://example/abc#", "http://example/abc", AlgRelativizeIRI::relativeResource, "/abc");
    }

    @Test public void test_rel_501_relativePath() {
        executeTest("http://example/abc#", "http://example/abc", AlgRelativizeIRI::relativePath, "");
    }

    @Test public void test_rel_501_relativeParentPath() {
        executeTest("http://example/abc#", "http://example/abc", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_501_relativeSameDocument() {
        executeTest("http://example/abc#", "http://example/abc", AlgRelativizeIRI::relativeSameDocument, "");
    }

    @Test public void test_rel_502_relativize() {
        executeTest("http://example/abc#", "http://example/abc#", AlgResolveIRI::relativize, "#");
    }

    @Test public void test_rel_502_relativeScheme() {
        executeTest("http://example/abc#", "http://example/abc#", AlgRelativizeIRI::relativeScheme, "//example/abc#");
    }

    @Test public void test_rel_502_relativeResource() {
        executeTest("http://example/abc#", "http://example/abc#", AlgRelativizeIRI::relativeResource, "/abc#");
    }

    @Test public void test_rel_502_relativePath() {
        executeTest("http://example/abc#", "http://example/abc#", AlgRelativizeIRI::relativePath, "#");
    }

    @Test public void test_rel_502_relativeParentPath() {
        executeTest("http://example/abc#", "http://example/abc#", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_502_relativeSameDocument() {
        executeTest("http://example/abc#", "http://example/abc#", AlgRelativizeIRI::relativeSameDocument, "#");
    }

    @Test public void test_rel_503_relativize() {
        executeTest("http://example/abc#", "http://example/abc#frag", AlgResolveIRI::relativize, "#frag");
    }

    @Test public void test_rel_503_relativeScheme() {
        executeTest("http://example/abc#", "http://example/abc#frag", AlgRelativizeIRI::relativeScheme, "//example/abc#frag");
    }

    @Test public void test_rel_503_relativeResource() {
        executeTest("http://example/abc#", "http://example/abc#frag", AlgRelativizeIRI::relativeResource, "/abc#frag");
    }

    @Test public void test_rel_503_relativePath() {
        executeTest("http://example/abc#", "http://example/abc#frag", AlgRelativizeIRI::relativePath, "#frag");
    }

    @Test public void test_rel_503_relativeParentPath() {
        executeTest("http://example/abc#", "http://example/abc#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_503_relativeSameDocument() {
        executeTest("http://example/abc#", "http://example/abc#frag", AlgRelativizeIRI::relativeSameDocument, "#frag");
    }

    @Test public void test_rel_504_relativize() {
        executeTest("http://example/abc#", "http://example/abc?query", AlgResolveIRI::relativize, "?query");
    }

    @Test public void test_rel_504_relativeScheme() {
        executeTest("http://example/abc#", "http://example/abc?query", AlgRelativizeIRI::relativeScheme, "//example/abc?query");
    }

    @Test public void test_rel_504_relativeResource() {
        executeTest("http://example/abc#", "http://example/abc?query", AlgRelativizeIRI::relativeResource, "/abc?query");
    }

    @Test public void test_rel_504_relativePath() {
        executeTest("http://example/abc#", "http://example/abc?query", AlgRelativizeIRI::relativePath, "?query");
    }

    @Test public void test_rel_504_relativeParentPath() {
        executeTest("http://example/abc#", "http://example/abc?query", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_504_relativeSameDocument() {
        executeTest("http://example/abc#", "http://example/abc?query", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_505_relativize() {
        executeTest("http://example/abc#", "http://example/abc?query#frag", AlgResolveIRI::relativize, "?query#frag");
    }

    @Test public void test_rel_505_relativeScheme() {
        executeTest("http://example/abc#", "http://example/abc?query#frag", AlgRelativizeIRI::relativeScheme, "//example/abc?query#frag");
    }

    @Test public void test_rel_505_relativeResource() {
        executeTest("http://example/abc#", "http://example/abc?query#frag", AlgRelativizeIRI::relativeResource, "/abc?query#frag");
    }

    @Test public void test_rel_505_relativePath() {
        executeTest("http://example/abc#", "http://example/abc?query#frag", AlgRelativizeIRI::relativePath, "?query#frag");
    }

    @Test public void test_rel_505_relativeParentPath() {
        executeTest("http://example/abc#", "http://example/abc?query#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_505_relativeSameDocument() {
        executeTest("http://example/abc#", "http://example/abc?query#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_506_relativize() {
        executeTest("http://example/abc#", "http://example/abc/", AlgResolveIRI::relativize, "abc/");
    }

    @Test public void test_rel_506_relativeScheme() {
        executeTest("http://example/abc#", "http://example/abc/", AlgRelativizeIRI::relativeScheme, "//example/abc/");
    }

    @Test public void test_rel_506_relativeResource() {
        executeTest("http://example/abc#", "http://example/abc/", AlgRelativizeIRI::relativeResource, "/abc/");
    }

    @Test public void test_rel_506_relativePath() {
        executeTest("http://example/abc#", "http://example/abc/", AlgRelativizeIRI::relativePath, "abc/");
    }

    @Test public void test_rel_506_relativeParentPath() {
        executeTest("http://example/abc#", "http://example/abc/", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_506_relativeSameDocument() {
        executeTest("http://example/abc#", "http://example/abc/", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_507_relativize() {
        executeTest("http://example/abc#frag", "http://example/abc", AlgResolveIRI::relativize, "");
    }

    @Test public void test_rel_507_relativeScheme() {
        executeTest("http://example/abc#frag", "http://example/abc", AlgRelativizeIRI::relativeScheme, "//example/abc");
    }

    @Test public void test_rel_507_relativeResource() {
        executeTest("http://example/abc#frag", "http://example/abc", AlgRelativizeIRI::relativeResource, "/abc");
    }

    @Test public void test_rel_507_relativePath() {
        executeTest("http://example/abc#frag", "http://example/abc", AlgRelativizeIRI::relativePath, "");
    }

    @Test public void test_rel_507_relativeParentPath() {
        executeTest("http://example/abc#frag", "http://example/abc", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_507_relativeSameDocument() {
        executeTest("http://example/abc#frag", "http://example/abc", AlgRelativizeIRI::relativeSameDocument, "");
    }

    @Test public void test_rel_508_relativize() {
        executeTest("http://example/abc#frag", "http://example/abc#", AlgResolveIRI::relativize, "#");
    }

    @Test public void test_rel_508_relativeScheme() {
        executeTest("http://example/abc#frag", "http://example/abc#", AlgRelativizeIRI::relativeScheme, "//example/abc#");
    }

    @Test public void test_rel_508_relativeResource() {
        executeTest("http://example/abc#frag", "http://example/abc#", AlgRelativizeIRI::relativeResource, "/abc#");
    }

    @Test public void test_rel_508_relativePath() {
        executeTest("http://example/abc#frag", "http://example/abc#", AlgRelativizeIRI::relativePath, "#");
    }

    @Test public void test_rel_508_relativeParentPath() {
        executeTest("http://example/abc#frag", "http://example/abc#", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_508_relativeSameDocument() {
        executeTest("http://example/abc#frag", "http://example/abc#", AlgRelativizeIRI::relativeSameDocument, "#");
    }

    @Test public void test_rel_509_relativize() {
        executeTest("http://example/abc#frag", "http://example/abc#frag", AlgResolveIRI::relativize, "#frag");
    }

    @Test public void test_rel_509_relativeScheme() {
        executeTest("http://example/abc#frag", "http://example/abc#frag", AlgRelativizeIRI::relativeScheme, "//example/abc#frag");
    }

    @Test public void test_rel_509_relativeResource() {
        executeTest("http://example/abc#frag", "http://example/abc#frag", AlgRelativizeIRI::relativeResource, "/abc#frag");
    }

    @Test public void test_rel_509_relativePath() {
        executeTest("http://example/abc#frag", "http://example/abc#frag", AlgRelativizeIRI::relativePath, "#frag");
    }

    @Test public void test_rel_509_relativeParentPath() {
        executeTest("http://example/abc#frag", "http://example/abc#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_509_relativeSameDocument() {
        executeTest("http://example/abc#frag", "http://example/abc#frag", AlgRelativizeIRI::relativeSameDocument, "#frag");
    }

    @Test public void test_rel_510_relativize() {
        executeTest("http://example/abc#frag", "http://example/abc#frag2", AlgResolveIRI::relativize, "#frag2");
    }

    @Test public void test_rel_510_relativeScheme() {
        executeTest("http://example/abc#frag", "http://example/abc#frag2", AlgRelativizeIRI::relativeScheme, "//example/abc#frag2");
    }

    @Test public void test_rel_510_relativeResource() {
        executeTest("http://example/abc#frag", "http://example/abc#frag2", AlgRelativizeIRI::relativeResource, "/abc#frag2");
    }

    @Test public void test_rel_510_relativePath() {
        executeTest("http://example/abc#frag", "http://example/abc#frag2", AlgRelativizeIRI::relativePath, "#frag2");
    }

    @Test public void test_rel_510_relativeParentPath() {
        executeTest("http://example/abc#frag", "http://example/abc#frag2", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_510_relativeSameDocument() {
        executeTest("http://example/abc#frag", "http://example/abc#frag2", AlgRelativizeIRI::relativeSameDocument, "#frag2");
    }

    @Test public void test_rel_511_relativize() {
        executeTest("http://example/abc#frag", "http://example/abc?query", AlgResolveIRI::relativize, "?query");
    }

    @Test public void test_rel_511_relativeScheme() {
        executeTest("http://example/abc#frag", "http://example/abc?query", AlgRelativizeIRI::relativeScheme, "//example/abc?query");
    }

    @Test public void test_rel_511_relativeResource() {
        executeTest("http://example/abc#frag", "http://example/abc?query", AlgRelativizeIRI::relativeResource, "/abc?query");
    }

    @Test public void test_rel_511_relativePath() {
        executeTest("http://example/abc#frag", "http://example/abc?query", AlgRelativizeIRI::relativePath, "?query");
    }

    @Test public void test_rel_511_relativeParentPath() {
        executeTest("http://example/abc#frag", "http://example/abc?query", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_511_relativeSameDocument() {
        executeTest("http://example/abc#frag", "http://example/abc?query", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_512_relativize() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag", AlgResolveIRI::relativize, "?query#frag");
    }

    @Test public void test_rel_512_relativeScheme() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag", AlgRelativizeIRI::relativeScheme, "//example/abc?query#frag");
    }

    @Test public void test_rel_512_relativeResource() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag", AlgRelativizeIRI::relativeResource, "/abc?query#frag");
    }

    @Test public void test_rel_512_relativePath() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag", AlgRelativizeIRI::relativePath, "?query#frag");
    }

    @Test public void test_rel_512_relativeParentPath() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_512_relativeSameDocument() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_513_relativize() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag2", AlgResolveIRI::relativize, "?query#frag2");
    }

    @Test public void test_rel_513_relativeScheme() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag2", AlgRelativizeIRI::relativeScheme, "//example/abc?query#frag2");
    }

    @Test public void test_rel_513_relativeResource() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag2", AlgRelativizeIRI::relativeResource, "/abc?query#frag2");
    }

    @Test public void test_rel_513_relativePath() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag2", AlgRelativizeIRI::relativePath, "?query#frag2");
    }

    @Test public void test_rel_513_relativeParentPath() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag2", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_513_relativeSameDocument() {
        executeTest("http://example/abc#frag", "http://example/abc?query#frag2", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_514_relativize() {
        executeTest("http://example/abc#frag", "http://example/abc/", AlgResolveIRI::relativize, "abc/");
    }

    @Test public void test_rel_514_relativeScheme() {
        executeTest("http://example/abc#frag", "http://example/abc/", AlgRelativizeIRI::relativeScheme, "//example/abc/");
    }

    @Test public void test_rel_514_relativeResource() {
        executeTest("http://example/abc#frag", "http://example/abc/", AlgRelativizeIRI::relativeResource, "/abc/");
    }

    @Test public void test_rel_514_relativePath() {
        executeTest("http://example/abc#frag", "http://example/abc/", AlgRelativizeIRI::relativePath, "abc/");
    }

    @Test public void test_rel_514_relativeParentPath() {
        executeTest("http://example/abc#frag", "http://example/abc/", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_514_relativeSameDocument() {
        executeTest("http://example/abc#frag", "http://example/abc/", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_600_relativize() {
        executeTest("http://example/", "http://example/", AlgResolveIRI::relativize, "");
    }

    @Test public void test_rel_600_relativeScheme() {
        executeTest("http://example/", "http://example/", AlgRelativizeIRI::relativeScheme, "//example/");
    }

    @Test public void test_rel_600_relativeResource() {
        executeTest("http://example/", "http://example/", AlgRelativizeIRI::relativeResource, "/");
    }

    @Test public void test_rel_600_relativePath() {
        executeTest("http://example/", "http://example/", AlgRelativizeIRI::relativePath, ".");
    }

    @Test public void test_rel_600_relativeParentPath() {
        executeTest("http://example/", "http://example/", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_600_relativeSameDocument() {
        executeTest("http://example/", "http://example/", AlgRelativizeIRI::relativeSameDocument, "");
    }

    @Test public void test_rel_601_relativize() {
        executeTest("http://example/", "http://example/#frag", AlgResolveIRI::relativize, "#frag");
    }

    @Test public void test_rel_601_relativeScheme() {
        executeTest("http://example/", "http://example/#frag", AlgRelativizeIRI::relativeScheme, "//example/#frag");
    }

    @Test public void test_rel_601_relativeResource() {
        executeTest("http://example/", "http://example/#frag", AlgRelativizeIRI::relativeResource, "/#frag");
    }

    @Test public void test_rel_601_relativePath() {
        executeTest("http://example/", "http://example/#frag", AlgRelativizeIRI::relativePath, ".#frag");
    }

    @Test public void test_rel_601_relativeParentPath() {
        executeTest("http://example/", "http://example/#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_601_relativeSameDocument() {
        executeTest("http://example/", "http://example/#frag", AlgRelativizeIRI::relativeSameDocument, "#frag");
    }

    @Test public void test_rel_602_relativize() {
        executeTest("http://example/", "http://example/?query", AlgResolveIRI::relativize, "?query");
    }

    @Test public void test_rel_602_relativeScheme() {
        executeTest("http://example/", "http://example/?query", AlgRelativizeIRI::relativeScheme, "//example/?query");
    }

    @Test public void test_rel_602_relativeResource() {
        executeTest("http://example/", "http://example/?query", AlgRelativizeIRI::relativeResource, "/?query");
    }

    @Test public void test_rel_602_relativePath() {
        executeTest("http://example/", "http://example/?query", AlgRelativizeIRI::relativePath, "?query");
    }

    @Test public void test_rel_602_relativeParentPath() {
        executeTest("http://example/", "http://example/?query", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_602_relativeSameDocument() {
        executeTest("http://example/", "http://example/?query", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_603_relativize() {
        executeTest("http://example/", "http://example/?query#frag", AlgResolveIRI::relativize, "?query#frag");
    }

    @Test public void test_rel_603_relativeScheme() {
        executeTest("http://example/", "http://example/?query#frag", AlgRelativizeIRI::relativeScheme, "//example/?query#frag");
    }

    @Test public void test_rel_603_relativeResource() {
        executeTest("http://example/", "http://example/?query#frag", AlgRelativizeIRI::relativeResource, "/?query#frag");
    }

    @Test public void test_rel_603_relativePath() {
        executeTest("http://example/", "http://example/?query#frag", AlgRelativizeIRI::relativePath, "?query#frag");
    }

    @Test public void test_rel_603_relativeParentPath() {
        executeTest("http://example/", "http://example/?query#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_603_relativeSameDocument() {
        executeTest("http://example/", "http://example/?query#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_700_relativize() {
        executeTest("http://example/#", "http://example/", AlgResolveIRI::relativize, "");
    }

    @Test public void test_rel_700_relativeScheme() {
        executeTest("http://example/#", "http://example/", AlgRelativizeIRI::relativeScheme, "//example/");
    }

    @Test public void test_rel_700_relativeResource() {
        executeTest("http://example/#", "http://example/", AlgRelativizeIRI::relativeResource, "/");
    }

    @Test public void test_rel_700_relativePath() {
        executeTest("http://example/#", "http://example/", AlgRelativizeIRI::relativePath, ".");
    }

    @Test public void test_rel_700_relativeParentPath() {
        executeTest("http://example/#", "http://example/", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_700_relativeSameDocument() {
        executeTest("http://example/#", "http://example/", AlgRelativizeIRI::relativeSameDocument, "");
    }

    @Test public void test_rel_701_relativize() {
        executeTest("http://example/#", "http://example/#", AlgResolveIRI::relativize, "#");
    }

    @Test public void test_rel_701_relativeScheme() {
        executeTest("http://example/#", "http://example/#", AlgRelativizeIRI::relativeScheme, "//example/#");
    }

    @Test public void test_rel_701_relativeResource() {
        executeTest("http://example/#", "http://example/#", AlgRelativizeIRI::relativeResource, "/#");
    }

    @Test public void test_rel_701_relativePath() {
        executeTest("http://example/#", "http://example/#", AlgRelativizeIRI::relativePath, ".#");
    }

    @Test public void test_rel_701_relativeParentPath() {
        executeTest("http://example/#", "http://example/#", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_701_relativeSameDocument() {
        executeTest("http://example/#", "http://example/#", AlgRelativizeIRI::relativeSameDocument, "#");
    }

    @Test public void test_rel_702_relativize() {
        executeTest("http://example/#", "http://example/#frag", AlgResolveIRI::relativize, "#frag");
    }

    @Test public void test_rel_702_relativeScheme() {
        executeTest("http://example/#", "http://example/#frag", AlgRelativizeIRI::relativeScheme, "//example/#frag");
    }

    @Test public void test_rel_702_relativeResource() {
        executeTest("http://example/#", "http://example/#frag", AlgRelativizeIRI::relativeResource, "/#frag");
    }

    @Test public void test_rel_702_relativePath() {
        executeTest("http://example/#", "http://example/#frag", AlgRelativizeIRI::relativePath, ".#frag");
    }

    @Test public void test_rel_702_relativeParentPath() {
        executeTest("http://example/#", "http://example/#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_702_relativeSameDocument() {
        executeTest("http://example/#", "http://example/#frag", AlgRelativizeIRI::relativeSameDocument, "#frag");
    }

    @Test public void test_rel_703_relativize() {
        executeTest("http://example/#", "http://example/?query", AlgResolveIRI::relativize, "?query");
    }

    @Test public void test_rel_703_relativeScheme() {
        executeTest("http://example/#", "http://example/?query", AlgRelativizeIRI::relativeScheme, "//example/?query");
    }

    @Test public void test_rel_703_relativeResource() {
        executeTest("http://example/#", "http://example/?query", AlgRelativizeIRI::relativeResource, "/?query");
    }

    @Test public void test_rel_703_relativePath() {
        executeTest("http://example/#", "http://example/?query", AlgRelativizeIRI::relativePath, "?query");
    }

    @Test public void test_rel_703_relativeParentPath() {
        executeTest("http://example/#", "http://example/?query", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_703_relativeSameDocument() {
        executeTest("http://example/#", "http://example/?query", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_704_relativize() {
        executeTest("http://example/#", "http://example/?query#frag", AlgResolveIRI::relativize, "?query#frag");
    }

    @Test public void test_rel_704_relativeScheme() {
        executeTest("http://example/#", "http://example/?query#frag", AlgRelativizeIRI::relativeScheme, "//example/?query#frag");
    }

    @Test public void test_rel_704_relativeResource() {
        executeTest("http://example/#", "http://example/?query#frag", AlgRelativizeIRI::relativeResource, "/?query#frag");
    }

    @Test public void test_rel_704_relativePath() {
        executeTest("http://example/#", "http://example/?query#frag", AlgRelativizeIRI::relativePath, "?query#frag");
    }

    @Test public void test_rel_704_relativeParentPath() {
        executeTest("http://example/#", "http://example/?query#frag", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_704_relativeSameDocument() {
        executeTest("http://example/#", "http://example/?query#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_705_relativize() {
        executeTest("http://example/#", "http://example/c/", AlgResolveIRI::relativize, "c/");
    }

    @Test public void test_rel_705_relativeScheme() {
        executeTest("http://example/#", "http://example/c/", AlgRelativizeIRI::relativeScheme, "//example/c/");
    }

    @Test public void test_rel_705_relativeResource() {
        executeTest("http://example/#", "http://example/c/", AlgRelativizeIRI::relativeResource, "/c/");
    }

    @Test public void test_rel_705_relativePath() {
        executeTest("http://example/#", "http://example/c/", AlgRelativizeIRI::relativePath, "c/");
    }

    @Test public void test_rel_705_relativeParentPath() {
        executeTest("http://example/#", "http://example/c/", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_705_relativeSameDocument() {
        executeTest("http://example/#", "http://example/c/", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_706_relativize() {
        executeTest("http://example/#", "http://example/abc", AlgResolveIRI::relativize, "abc");
    }

    @Test public void test_rel_706_relativeScheme() {
        executeTest("http://example/#", "http://example/abc", AlgRelativizeIRI::relativeScheme, "//example/abc");
    }

    @Test public void test_rel_706_relativeResource() {
        executeTest("http://example/#", "http://example/abc", AlgRelativizeIRI::relativeResource, "/abc");
    }

    @Test public void test_rel_706_relativePath() {
        executeTest("http://example/#", "http://example/abc", AlgRelativizeIRI::relativePath, "abc");
    }

    @Test public void test_rel_706_relativeParentPath() {
        executeTest("http://example/#", "http://example/abc", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_706_relativeSameDocument() {
        executeTest("http://example/#", "http://example/abc", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_707_relativize() {
        executeTest("http://example/#", "http://example/abc#", AlgResolveIRI::relativize, "abc#");
    }

    @Test public void test_rel_707_relativeScheme() {
        executeTest("http://example/#", "http://example/abc#", AlgRelativizeIRI::relativeScheme, "//example/abc#");
    }

    @Test public void test_rel_707_relativeResource() {
        executeTest("http://example/#", "http://example/abc#", AlgRelativizeIRI::relativeResource, "/abc#");
    }

    @Test public void test_rel_707_relativePath() {
        executeTest("http://example/#", "http://example/abc#", AlgRelativizeIRI::relativePath, "abc#");
    }

    @Test public void test_rel_707_relativeParentPath() {
        executeTest("http://example/#", "http://example/abc#", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_707_relativeSameDocument() {
        executeTest("http://example/#", "http://example/abc#", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_800_relativize() {
        executeTest("http://example/dir", "http://example/dir/path", AlgResolveIRI::relativize, "dir/path");
    }

    @Test public void test_rel_800_relativeScheme() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativeScheme, "//example/dir/path");
    }

    @Test public void test_rel_800_relativeResource() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativeResource, "/dir/path");
    }

    @Test public void test_rel_800_relativePath() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativePath, "dir/path");
    }

    @Test public void test_rel_800_relativeParentPath() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_800_relativeSameDocument() {
        executeTest("http://example/dir", "http://example/dir/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_801_relativize() {
        executeTest("http://example/dir1/file2", "http://example/dir1/dir2/path", AlgResolveIRI::relativize, "dir2/path");
    }

    @Test public void test_rel_801_relativeScheme() {
        executeTest("http://example/dir1/file2", "http://example/dir1/dir2/path", AlgRelativizeIRI::relativeScheme, "//example/dir1/dir2/path");
    }

    @Test public void test_rel_801_relativeResource() {
        executeTest("http://example/dir1/file2", "http://example/dir1/dir2/path", AlgRelativizeIRI::relativeResource, "/dir1/dir2/path");
    }

    @Test public void test_rel_801_relativePath() {
        executeTest("http://example/dir1/file2", "http://example/dir1/dir2/path", AlgRelativizeIRI::relativePath, "dir2/path");
    }

    @Test public void test_rel_801_relativeParentPath() {
        executeTest("http://example/dir1/file2", "http://example/dir1/dir2/path", AlgRelativizeIRI::relativeParentPath, "../dir1/dir2/path");
    }

    @Test public void test_rel_801_relativeSameDocument() {
        executeTest("http://example/dir1/file2", "http://example/dir1/dir2/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_802_relativize() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgResolveIRI::relativize, "path");
    }

    @Test public void test_rel_802_relativeScheme() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativeScheme, "//example/dir/path");
    }

    @Test public void test_rel_802_relativeResource() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativeResource, "/dir/path");
    }

    @Test public void test_rel_802_relativePath() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativePath, "path");
    }

    @Test public void test_rel_802_relativeParentPath() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativeParentPath, "../dir/path");
    }

    @Test public void test_rel_802_relativeSameDocument() {
        executeTest("http://example/dir/", "http://example/dir/path", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_803_relativize() {
        executeTest("http://example/dir1", "http://example/dir1/dir2/", AlgResolveIRI::relativize, "dir1/dir2/");
    }

    @Test public void test_rel_803_relativeScheme() {
        executeTest("http://example/dir1", "http://example/dir1/dir2/", AlgRelativizeIRI::relativeScheme, "//example/dir1/dir2/");
    }

    @Test public void test_rel_803_relativeResource() {
        executeTest("http://example/dir1", "http://example/dir1/dir2/", AlgRelativizeIRI::relativeResource, "/dir1/dir2/");
    }

    @Test public void test_rel_803_relativePath() {
        executeTest("http://example/dir1", "http://example/dir1/dir2/", AlgRelativizeIRI::relativePath, "dir1/dir2/");
    }

    @Test public void test_rel_803_relativeParentPath() {
        executeTest("http://example/dir1", "http://example/dir1/dir2/", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_803_relativeSameDocument() {
        executeTest("http://example/dir1", "http://example/dir1/dir2/", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_804_relativize() {
        executeTest("http://example/dir1/", "http://example/dir1/dir2/", AlgResolveIRI::relativize, "dir2/");
    }

    @Test public void test_rel_804_relativeScheme() {
        executeTest("http://example/dir1/", "http://example/dir1/dir2/", AlgRelativizeIRI::relativeScheme, "//example/dir1/dir2/");
    }

    @Test public void test_rel_804_relativeResource() {
        executeTest("http://example/dir1/", "http://example/dir1/dir2/", AlgRelativizeIRI::relativeResource, "/dir1/dir2/");
    }

    @Test public void test_rel_804_relativePath() {
        executeTest("http://example/dir1/", "http://example/dir1/dir2/", AlgRelativizeIRI::relativePath, "dir2/");
    }

    @Test public void test_rel_804_relativeParentPath() {
        executeTest("http://example/dir1/", "http://example/dir1/dir2/", AlgRelativizeIRI::relativeParentPath, "../dir1/dir2/");
    }

    @Test public void test_rel_804_relativeSameDocument() {
        executeTest("http://example/dir1/", "http://example/dir1/dir2/", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_805_relativize() {
        executeTest("http://example/abc/def", "http://example/abc#frag", AlgResolveIRI::relativize, "/abc#frag");
    }

    @Test public void test_rel_805_relativeScheme() {
        executeTest("http://example/abc/def", "http://example/abc#frag", AlgRelativizeIRI::relativeScheme, "//example/abc#frag");
    }

    @Test public void test_rel_805_relativeResource() {
        executeTest("http://example/abc/def", "http://example/abc#frag", AlgRelativizeIRI::relativeResource, "/abc#frag");
    }

    @Test public void test_rel_805_relativePath() {
        executeTest("http://example/abc/def", "http://example/abc#frag", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_805_relativeParentPath() {
        executeTest("http://example/abc/def", "http://example/abc#frag", AlgRelativizeIRI::relativeParentPath, "../abc#frag");
    }

    @Test public void test_rel_805_relativeSameDocument() {
        executeTest("http://example/abc/def", "http://example/abc#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_806_relativize() {
        executeTest("http://example/abc/def", "http://example/abc?query", AlgResolveIRI::relativize, "/abc?query");
    }

    @Test public void test_rel_806_relativeScheme() {
        executeTest("http://example/abc/def", "http://example/abc?query", AlgRelativizeIRI::relativeScheme, "//example/abc?query");
    }

    @Test public void test_rel_806_relativeResource() {
        executeTest("http://example/abc/def", "http://example/abc?query", AlgRelativizeIRI::relativeResource, "/abc?query");
    }

    @Test public void test_rel_806_relativePath() {
        executeTest("http://example/abc/def", "http://example/abc?query", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_806_relativeParentPath() {
        executeTest("http://example/abc/def", "http://example/abc?query", AlgRelativizeIRI::relativeParentPath, "../abc?query");
    }

    @Test public void test_rel_806_relativeSameDocument() {
        executeTest("http://example/abc/def", "http://example/abc?query", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_807_relativize() {
        executeTest("http://example/abc/def", "http://example/abc?query#frag", AlgResolveIRI::relativize, "/abc?query#frag");
    }

    @Test public void test_rel_807_relativeScheme() {
        executeTest("http://example/abc/def", "http://example/abc?query#frag", AlgRelativizeIRI::relativeScheme, "//example/abc?query#frag");
    }

    @Test public void test_rel_807_relativeResource() {
        executeTest("http://example/abc/def", "http://example/abc?query#frag", AlgRelativizeIRI::relativeResource, "/abc?query#frag");
    }

    @Test public void test_rel_807_relativePath() {
        executeTest("http://example/abc/def", "http://example/abc?query#frag", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_807_relativeParentPath() {
        executeTest("http://example/abc/def", "http://example/abc?query#frag", AlgRelativizeIRI::relativeParentPath, "../abc?query#frag");
    }

    @Test public void test_rel_807_relativeSameDocument() {
        executeTest("http://example/abc/def", "http://example/abc?query#frag", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_808_relativize() {
        executeTest("https://example/dir", "http://example/dir", AlgResolveIRI::relativize, null);
    }

    @Test public void test_rel_808_relativeScheme() {
        executeTest("https://example/dir", "http://example/dir", AlgRelativizeIRI::relativeScheme, null);
    }

    @Test public void test_rel_808_relativeResource() {
        executeTest("https://example/dir", "http://example/dir", AlgRelativizeIRI::relativeResource, null);
    }

    @Test public void test_rel_808_relativePath() {
        executeTest("https://example/dir", "http://example/dir", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_808_relativeParentPath() {
        executeTest("https://example/dir", "http://example/dir", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_808_relativeSameDocument() {
        executeTest("https://example/dir", "http://example/dir", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_809_relativize() {
        executeTest("http://example/a", "http://other/a", AlgResolveIRI::relativize, null);
    }

    @Test public void test_rel_809_relativeScheme() {
        executeTest("http://example/a", "http://other/a", AlgRelativizeIRI::relativeScheme, "//other/a");
    }

    @Test public void test_rel_809_relativeResource() {
        executeTest("http://example/a", "http://other/a", AlgRelativizeIRI::relativeResource, null);
    }

    @Test public void test_rel_809_relativePath() {
        executeTest("http://example/a", "http://other/a", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_809_relativeParentPath() {
        executeTest("http://example/a", "http://other/a", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_809_relativeSameDocument() {
        executeTest("http://example/a", "http://other/a", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_900_relativize() {
        executeTest("http:", "http://other/a", AlgResolveIRI::relativize, null);
    }

    @Test public void test_rel_900_relativeScheme() {
        executeTest("http:", "http://other/a", AlgRelativizeIRI::relativeScheme, "//other/a");
    }

    @Test public void test_rel_900_relativeResource() {
        executeTest("http:", "http://other/a", AlgRelativizeIRI::relativeResource, null);
    }

    @Test public void test_rel_900_relativePath() {
        executeTest("http:", "http://other/a", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_900_relativeParentPath() {
        executeTest("http:", "http://other/a", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_900_relativeSameDocument() {
        executeTest("http:", "http://other/a", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_901_relativize() {
        executeTest("http://", "http://other/a", AlgResolveIRI::relativize, null);
    }

    @Test public void test_rel_901_relativeScheme() {
        executeTest("http://", "http://other/a", AlgRelativizeIRI::relativeScheme, "//other/a");
    }

    @Test public void test_rel_901_relativeResource() {
        executeTest("http://", "http://other/a", AlgRelativizeIRI::relativeResource, null);
    }

    @Test public void test_rel_901_relativePath() {
        executeTest("http://", "http://other/a", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_901_relativeParentPath() {
        executeTest("http://", "http://other/a", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_901_relativeSameDocument() {
        executeTest("http://", "http://other/a", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_902_relativize() {
        executeTest("http:", "https://other/a", AlgResolveIRI::relativize, null);
    }

    @Test public void test_rel_902_relativeScheme() {
        executeTest("http:", "https://other/a", AlgRelativizeIRI::relativeScheme, null);
    }

    @Test public void test_rel_902_relativeResource() {
        executeTest("http:", "https://other/a", AlgRelativizeIRI::relativeResource, null);
    }

    @Test public void test_rel_902_relativePath() {
        executeTest("http:", "https://other/a", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_902_relativeParentPath() {
        executeTest("http:", "https://other/a", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_902_relativeSameDocument() {
        executeTest("http:", "https://other/a", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_903_relativize() {
        executeTest("http://", "https://other/a", AlgResolveIRI::relativize, null);
    }

    @Test public void test_rel_903_relativeScheme() {
        executeTest("http://", "https://other/a", AlgRelativizeIRI::relativeScheme, null);
    }

    @Test public void test_rel_903_relativeResource() {
        executeTest("http://", "https://other/a", AlgRelativizeIRI::relativeResource, null);
    }

    @Test public void test_rel_903_relativePath() {
        executeTest("http://", "https://other/a", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_903_relativeParentPath() {
        executeTest("http://", "https://other/a", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_903_relativeSameDocument() {
        executeTest("http://", "https://other/a", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_904_relativize() {
        executeTest("http:", "//other/a", AlgResolveIRI::relativize, null);
    }

    @Test public void test_rel_904_relativeScheme() {
        executeTest("http:", "//other/a", AlgRelativizeIRI::relativeScheme, null);
    }

    @Test public void test_rel_904_relativeResource() {
        executeTest("http:", "//other/a", AlgRelativizeIRI::relativeResource, null);
    }

    @Test public void test_rel_904_relativePath() {
        executeTest("http:", "//other/a", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_904_relativeParentPath() {
        executeTest("http:", "//other/a", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_904_relativeSameDocument() {
        executeTest("http:", "//other/a", AlgRelativizeIRI::relativeSameDocument, null);
    }

    @Test public void test_rel_905_relativize() {
        executeTest("http://", "//other/a", AlgResolveIRI::relativize, null);
    }

    @Test public void test_rel_905_relativeScheme() {
        executeTest("http://", "//other/a", AlgRelativizeIRI::relativeScheme, null);
    }

    @Test public void test_rel_905_relativeResource() {
        executeTest("http://", "//other/a", AlgRelativizeIRI::relativeResource, null);
    }

    @Test public void test_rel_905_relativePath() {
        executeTest("http://", "//other/a", AlgRelativizeIRI::relativePath, null);
    }

    @Test public void test_rel_905_relativeParentPath() {
        executeTest("http://", "//other/a", AlgRelativizeIRI::relativeParentPath, null);
    }

    @Test public void test_rel_905_relativeSameDocument() {
        executeTest("http://", "//other/a", AlgRelativizeIRI::relativeSameDocument, null);
    }
}
