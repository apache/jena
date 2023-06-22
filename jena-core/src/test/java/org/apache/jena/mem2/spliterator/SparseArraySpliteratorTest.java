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
package org.apache.jena.mem2.spliterator;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Spliterator;

import static java.util.Spliterator.*;
import static org.junit.Assert.*;

public class SparseArraySpliteratorTest {

    @Test
    public void tryAdvanceEmpty() {
        {
            Integer[] array = new Integer[0];
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            assertFalse(spliterator.tryAdvance((i) -> {
                fail("Should not have advanced");
            }));
        }
        {
            Integer[] array = new Integer[1];
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            assertFalse(spliterator.tryAdvance((i) -> {
                fail("Should not have advanced");
            }));
        }
    }

    @Test
    public void tryAdvanceOne() {
        {
            Integer[] array = new Integer[]{1};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(1);
            })) ;
            assertEquals(1, itemsFound.size());
            itemsFound.contains(1);
        }
        {
            Integer[] array = new Integer[]{1, null};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(1);
            })) ;
            assertEquals(1, itemsFound.size());
            itemsFound.contains(1);
        }
        {
            Integer[] array = new Integer[]{null, 1};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(1);
            })) ;
            assertEquals(1, itemsFound.size());
            itemsFound.contains(1);
        }
    }

    @Test
    public void tryAdvanceTwo() {
        {
            Integer[] array = new Integer[]{1, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
        {
            Integer[] array = new Integer[]{1, null, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
        {
            Integer[] array = new Integer[]{1, null, null, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
        {
            Integer[] array = new Integer[]{null, 1, null, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
        {
            Integer[] array = new Integer[]{null, 1, null, null, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
    }

    @Test
    public void tryAdvanceThree() {
        {
            Integer[] array = new Integer[]{1, 2, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(3, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
            itemsFound.contains(3);
        }
        {
            Integer[] array = new Integer[]{1, null, 2, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(3, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
            itemsFound.contains(3);
        }
        {
            Integer[] array = new Integer[]{1, null, null, 2, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(3, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
            itemsFound.contains(3);
        }
        {
            Integer[] array = new Integer[]{null, 1, null, 2, null, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(3, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
            itemsFound.contains(3);
        }
        {
            Integer[] array = new Integer[]{null, 1, null, null, 2, null, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            while (spliterator.tryAdvance((i) -> {
                itemsFound.add(i);
            })) ;
            assertEquals(3, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
            itemsFound.contains(3);
        }
    }

    @Test
    public void forEachRemainingEmpty() {
        {
            Integer[] array = new Integer[]{};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(0, itemsFound.size());
        }
        {
            Integer[] array = new Integer[]{null};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(0, itemsFound.size());
        }
    }

    @Test
    public void forEachRemainingOne() {
        {
            Integer[] array = new Integer[]{1};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(1, itemsFound.size());
            itemsFound.contains(1);
        }
        {
            Integer[] array = new Integer[]{null, 1};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(1, itemsFound.size());
            itemsFound.contains(1);
        }
        {
            Integer[] array = new Integer[]{1, null};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(1, itemsFound.size());
            itemsFound.contains(1);
        }
    }

    @Test
    public void forEachRemainingTwo() {
        {
            Integer[] array = new Integer[]{1, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
        {
            Integer[] array = new Integer[]{1, null, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
        {
            Integer[] array = new Integer[]{1, null, null, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
        {
            Integer[] array = new Integer[]{null, 1, null, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
        {
            Integer[] array = new Integer[]{null, 1, null, null, 2};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(2, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
        }
    }

    @Test
    public void forEachRemainingThree() {
        {
            Integer[] array = new Integer[]{1, 2, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(3, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
            itemsFound.contains(3);
        }
        {
            Integer[] array = new Integer[]{1, null, 2, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(3, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
            itemsFound.contains(3);
        }
        {
            Integer[] array = new Integer[]{1, null, null, 2, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(3, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
            itemsFound.contains(3);
        }
        {
            Integer[] array = new Integer[]{null, 1, null, 2, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(3, itemsFound.size());
            itemsFound.contains(1);
            itemsFound.contains(2);
            itemsFound.contains(3);
        }
        {
            Integer[] array = new Integer[]{null, 1, null, null, 2, 3};
            Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
            });
            var itemsFound = new ArrayList<>();
            spliterator.forEachRemaining((i) -> {
                itemsFound.add(i);
            });
            assertEquals(3, itemsFound.size());
        }
    }

    @Test
    public void trySplitEmpty() {
        Integer[] array = new Integer[]{};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        assertNull(spliterator.trySplit());
    }

    @Test
    public void trySplitOne() {
        Integer[] array = new Integer[]{1};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        assertNull(spliterator.trySplit());
    }

    @Test
    public void trySplitTwo() {
        Integer[] array = new Integer[]{1, 2};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        // Estimated size is not exact
        assertBetween(2, 3, spliterator.estimateSize());
        Spliterator<Integer> split = spliterator.trySplit();
        assertBetween(1, 2, spliterator.estimateSize());
        assertBetween(1, 3, split.estimateSize());
    }

    @Test
    public void trySplitThree() {
        Integer[] array = new Integer[]{1, 2, 3};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        // Estimated size is not exact
        assertBetween(3, 4, spliterator.estimateSize());
        Spliterator<Integer> split = spliterator.trySplit();
        assertBetween(1, 2, spliterator.estimateSize());
        assertBetween(2, 3, split.estimateSize());
    }

    @Test
    public void trySplitFour() {
        Integer[] array = new Integer[]{1, 2, 3, 4};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        // Estimated size is not exact
        assertBetween(4, 5, spliterator.estimateSize());
        Spliterator<Integer> split = spliterator.trySplit();
        assertBetween(2, 3, spliterator.estimateSize());
        assertBetween(2, 4, split.estimateSize());
    }

    @Test
    public void trySplitFive() {
        Integer[] array = new Integer[]{1, 2, 3, 4, 5};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        // Estimated size is not exact
        assertBetween(5, 6, spliterator.estimateSize());
        Spliterator<Integer> split = spliterator.trySplit();
        assertBetween(2, 3, spliterator.estimateSize());
        assertBetween(3, 4, split.estimateSize());
    }

    @Test
    public void trySplitOneHundred() {
        Integer[] array = new Integer[200];
        for (int i = 0; i < array.length; i++) {
            if (i % 2 == 0) {
                array[i] = i;
            }
        }
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        // Estimated size is not exact
        assertEquals(array.length, spliterator.estimateSize());
        Spliterator<Integer> split = spliterator.trySplit();
        assertEquals(array.length / 2, spliterator.estimateSize());
        assertEquals(array.length / 2, split.estimateSize());
    }

    private void assertBetween(long min, long max, long estimateSize) {
        assertTrue("estimateSize=" + estimateSize + " min=" + min + " max=" + max, estimateSize >= min);
        assertTrue("estimateSize=" + estimateSize + " min=" + min + " max=" + max, estimateSize <= max);
    }

    @Test
    public void estimateSizeZero() {
        Integer[] array = new Integer[]{};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        assertBetween(0, 1, spliterator.estimateSize());
    }

    @Test
    public void estimateSizeOne() {
        Integer[] array = new Integer[]{1};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        assertBetween(1, 2, spliterator.estimateSize());
    }

    @Test
    public void estimateSizeTwo() {
        Integer[] array = new Integer[]{1, 2};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        assertBetween(2, 3, spliterator.estimateSize());
    }

    @Test
    public void estimateSizeFive() {
        Integer[] array = new Integer[]{1, 2, 3, 4, 5};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        assertBetween(5, 6, spliterator.estimateSize());
    }

    @Test
    public void characteristics() {
        Integer[] array = new Integer[]{1, 2, 3, 4, 5};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        assertEquals(DISTINCT | NONNULL | IMMUTABLE, spliterator.characteristics());
    }

    @Test
    public void splitWithOneElementNull() {
        Integer[] array = new Integer[]{1};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        assertNull(spliterator.trySplit());
    }

    @Test
    public void splitWithOneRemainingElementNull() {
        Integer[] array = new Integer[]{1, 2};
        Spliterator<Integer> spliterator = new SparseArraySpliterator<>(array, () -> {
        });
        spliterator.tryAdvance((i) -> {
        });
        assertNull(spliterator.trySplit());
    }
}