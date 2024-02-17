/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import Pagination from '@/components/dataset/Pagination.vue'


describe('Pagination', () => {
  // Disabled first, disabled back, disabled next, disabled last.
  const MINIMUM_PAGE_ITEMS = 4
  /**
   * @param options
   * @returns {VueWrapper<Pagination>}
   */
  const mountFunction = options => {
    return mount(Pagination, {
      ...options,
      global: {
        mocks: {
          $fusekiService: {}
        }
      }
    })
  }
  it('works when there are no rows, no pages', () => {
    const component = mountFunction({
      propsData: {
        totalRows: 0,
        value: 0
      }
    })
    expect(component.find('.pagination')).to.exist
    expect(component.findAll('.page-item')).toHaveLength(MINIMUM_PAGE_ITEMS)
    expect(component.findAll('.page-summary-item')).toHaveLength(0)
  })
  it('works when there is a single page', () => {
    // Given we have a maximum of seven items, we test that it works
    // as expected for 1 row, 2 rows, 3 rows, ..., 7 rows. For all
    // these cases we must have the same number of pages in this
    // Pagination component: just one.
    const maxDisplayed = 7
    for (let totalRows = 1; totalRows <= maxDisplayed; ++totalRows) {
      const component = mountFunction({
        propsData: {
          totalRows: totalRows,
          perPage: totalRows,
          maxDisplayed: maxDisplayed,
          value: 1
        }
      })
      expect(component.find('.pagination')).to.exist
      expect(component.findAll('.page-item')).toHaveLength(1 + MINIMUM_PAGE_ITEMS)
      expect(component.findAll('.page-summary-item')).toHaveLength(0)
    }
  })
  it('works when there is more than 1 page', () => {
    // We display one item per page.
    const perPage = 1
    // We display a maximum of 1 page.
    const maxDisplayed = 1
    // We have 3 items (rows).
    const totalRows = 3

    // This covers all cases, with no back summary (...), with back and next summary, and with the next summary only.
    for (let currentPage = 1; currentPage <= totalRows; ++currentPage) {
      const component = mountFunction({
        propsData: {
          totalRows: totalRows,
          perPage: perPage,
          value: currentPage,
          maxDisplayed: maxDisplayed
        }
      })
      expect(component.find('.pagination')).to.exist
      // We will display the maxDisplayed (2) plus the minimum page items.
      expect(component.findAll('.page-item')).toHaveLength(maxDisplayed + MINIMUM_PAGE_ITEMS)
      // For the first page we will display the back summary.
      if (currentPage === 1) {
        expect(component.findAll('.page-summary-item')).toHaveLength(1)
      }
      // The second page will show the summary for back and next.
      if (currentPage === 2) {
        expect(component.findAll('.page-summary-item')).toHaveLength(2)
      }
      // Lastly, the third page shows only the back summary.
      if (currentPage === 3) {
        expect(component.findAll('.page-summary-item')).toHaveLength(1)
      }
    }
  })
  it('fires an event when you go to another page', async () => {
    const component = mountFunction({
      propsData: {
        totalRows: 1
      }
    })

    const theAnswer = 42

    component.vm.goToPage(theAnswer)

    // Wait until $emits have been handled
    await component.vm.$nextTick()

    const inputEventsEmitted = component.emitted().input[0]
    const singleEventEmitted = inputEventsEmitted[0]

    expect(singleEventEmitted).toBe(theAnswer)
  })
})
