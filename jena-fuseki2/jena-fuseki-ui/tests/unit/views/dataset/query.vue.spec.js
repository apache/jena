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
import { flushPromises, mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import Query from '@/views/dataset/Query.vue'
import {  vi } from 'vitest'

const FAKE_FUSEKI_URL = 'https://localhost:1234/fuseki/'

const $routeMock = {
  query: {}
}

const mountFunction = options => {
  const mountOptions = Object.assign(options || {}, {
    shallow: true,
    global: {
      mocks: {
        $route: $routeMock,
        $fusekiService: {
          getFusekiUrl () {
            return FAKE_FUSEKI_URL
          }
        }
      }
    }
  })
  return mount(Query, {
    ...mountOptions
  })
}

describe('Query view', () => {
  let yasrDiv
  let yasqeDiv
  beforeEach(() => {
    // DOM elements required by YASQE/YASR.
    yasrDiv = document.createElement('div')
    yasrDiv.setAttribute('id', 'yasr')
    yasqeDiv = document.createElement('div')
    yasqeDiv.setAttribute('id', 'yasqe')
    document.body.append(yasrDiv)
    document.body.append(yasqeDiv)
    // we will have to mock setTimeout and nextTick at least, for the component with DOM
    vi.useFakeTimers({
      toFake: [
        'Date',
        'nextTick',
        'setTimeout'
      ],
      shouldAdvanceTime: true
    })
    // jsdom doesn't have getBoundingClientRect
    document.createRange = () => {
      const range = new Range();

      range.getBoundingClientRect = () => {
        return {
          x: 0,
          y: 0,
          bottom: 0,
          height: 0,
          left: 0,
          right: 0,
          top: 0,
          width: 0,
          toJSON: () => {}
        };
      };

      range.getClientRects = () => {
        return {
          // eslint-disable-next-line no-unused-vars
          item: (index) => null,
          length: 0,
          *[Symbol.iterator](){}
        };
      };

      return range;
    }
  })
  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })
  it('is created with the correct initial values', async () => {
    expect(vi.isFakeTimers()).equals(true)
    const datasetName = 'test'
    const wrapper = mountFunction({
      props: {
        datasetName: datasetName
      }
    })

    // Test the prop value.
    expect(wrapper.vm.$props.datasetName).equals(datasetName)

    // The component needs to interface with DOM due to YASQE, and it contains
    // a `nextTick`, that calls `setTimeout` (this is what worked in the end,
    // although probably a `Teleport` could replace it...). So we need to mock
    // that here. The timeout is of `300ms`, so we move the clock by `400ms`.
    await nextTick()
    await vi.advanceTimersByTime(400)
    await flushPromises()

    // Now YASQE and YASR must have been initialized.
    expect(wrapper.vm.yasqe).not.equals(null)

    // Test the initial values.
    const yasqeOptions = wrapper.vm.yasqe.options
    expect(yasqeOptions.showQueryButton).true
    expect(yasqeOptions.resizeable).true

    const requestConfig = yasqeOptions.requestConfig
    expect(await requestConfig.endpoint).equals(FAKE_FUSEKI_URL)
    // See issue https://github.com/apache/jena/issues/1611
    expect(requestConfig.acceptHeaderGraph).equals(wrapper.vm.$data.contentTypeGraph)
  })
})
