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

const mountFunction = (options = {}) => {
  const { mocks, ...mountOptions } = options
  return mount(Query, {
    ...mountOptions,
    shallow: true,
    global: {
      mocks: {
        $route: $routeMock,
        $fusekiService: {
          getFusekiUrl () {
            return FAKE_FUSEKI_URL
          }
        },
        $toast: {
          error () {},
          notification () {}
        },
        ...mocks
      }
    }
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

  describe('prefixes service', () => {
    const datasetName = 'test'
    const querySvc = { 'srv.type': 'query', 'srv.endpoints': ['sparql'] }
    const prefixesR = { 'srv.type': 'prefixes-r', 'srv.endpoints': ['prefixes'] }
    const prefixesRW = { 'srv.type': 'prefixes-rw', 'srv.endpoints': ['updatePrefixes'] }
    const serverDataWith = services => ({
      datasets: [
        {
          'ds.name': `/${datasetName}`,
          'ds.services': services
        }
      ]
    })
    const fusekiServiceMock = getPrefixes => ({
      getFusekiUrl () {
        return FAKE_FUSEKI_URL
      },
      getPrefixes
    })

    it('detects no prefixes service when the dataset does not declare one', async () => {
      const wrapper = mountFunction({
        props: { datasetName }
      })
      expect(wrapper.vm.prefixesService).equals(null)
      wrapper.vm.serverData = serverDataWith([querySvc])
      await nextTick()
      expect(wrapper.vm.prefixesService).equals(null)
    })

    it('detects a read-only prefixes service', async () => {
      const wrapper = mountFunction({
        props: { datasetName }
      })
      wrapper.vm.serverData = serverDataWith([querySvc, prefixesR])
      await nextTick()
      expect(wrapper.vm.prefixesService).deep.equals({
        endpoint: 'prefixes',
        writable: false
      })
    })

    it('prefers the read-write prefixes service over the read-only one', async () => {
      const wrapper = mountFunction({
        props: { datasetName }
      })
      wrapper.vm.serverData = serverDataWith([querySvc, prefixesR, prefixesRW])
      await nextTick()
      expect(wrapper.vm.prefixesService).deep.equals({
        endpoint: 'updatePrefixes',
        writable: true
      })
    })

    it('does not fetch prefixes when there is no prefixes service', async () => {
      const getPrefixes = vi.fn()
      const wrapper = mountFunction({
        props: { datasetName },
        mocks: { $fusekiService: fusekiServiceMock(getPrefixes) }
      })
      const defaults = JSON.parse(JSON.stringify(wrapper.vm.prefixes))
      wrapper.vm.serverData = serverDataWith([querySvc])
      await nextTick()
      await flushPromises()
      expect(getPrefixes.mock.calls.length).equals(0)
      expect(wrapper.vm.prefixes).deep.equals(defaults)
    })

    it('replaces the default prefixes with the dataset prefixes', async () => {
      const getPrefixes = vi.fn().mockResolvedValue({
        data: [
          { prefix: 'foaf', uri: 'http://xmlns.com/foaf/0.1/' }
        ]
      })
      const wrapper = mountFunction({
        props: { datasetName },
        mocks: { $fusekiService: fusekiServiceMock(getPrefixes) }
      })
      wrapper.vm.serverData = serverDataWith([querySvc, prefixesR])
      await nextTick()
      await flushPromises()
      expect(getPrefixes.mock.calls.length).equals(1)
      expect(getPrefixes.mock.calls[0]).deep.equals([datasetName, 'prefixes'])
      expect(wrapper.vm.prefixes).deep.equals([
        { text: 'foaf', uri: 'http://xmlns.com/foaf/0.1/' }
      ])
    })

    it('keeps the default prefixes and reports the error when fetching fails', async () => {
      const getPrefixes = vi.fn().mockRejectedValue(new Error('403 Forbidden'))
      const toastError = vi.fn()
      const wrapper = mountFunction({
        props: { datasetName },
        mocks: {
          $fusekiService: fusekiServiceMock(getPrefixes),
          $toast: { error: toastError, notification () {} }
        }
      })
      const defaults = JSON.parse(JSON.stringify(wrapper.vm.prefixes))
      wrapper.vm.serverData = serverDataWith([querySvc, prefixesR])
      await nextTick()
      await flushPromises()
      expect(getPrefixes.mock.calls.length).equals(1)
      expect(toastError.mock.calls.length).equals(1)
      expect(wrapper.vm.prefixes).deep.equals(defaults)
    })

    it('falls back to the default prefixes when the prefix store is empty', async () => {
      const getPrefixes = vi.fn().mockResolvedValue({ data: [] })
      const wrapper = mountFunction({
        props: { datasetName },
        mocks: { $fusekiService: fusekiServiceMock(getPrefixes) }
      })
      const defaults = JSON.parse(JSON.stringify(wrapper.vm.prefixes))
      wrapper.vm.serverData = serverDataWith([querySvc, prefixesR])
      await nextTick()
      await flushPromises()
      expect(getPrefixes.mock.calls.length).equals(1)
      expect(wrapper.vm.prefixes).deep.equals(defaults)
    })

    it('restores the default prefixes when the dataset has no prefixes service', async () => {
      const getPrefixes = vi.fn().mockResolvedValue({
        data: [
          { prefix: 'foaf', uri: 'http://xmlns.com/foaf/0.1/' }
        ]
      })
      const wrapper = mountFunction({
        props: { datasetName },
        mocks: { $fusekiService: fusekiServiceMock(getPrefixes) }
      })
      const defaults = JSON.parse(JSON.stringify(wrapper.vm.prefixes))
      wrapper.vm.serverData = serverDataWith([querySvc, prefixesR])
      await nextTick()
      await flushPromises()
      expect(wrapper.vm.prefixes).deep.equals([
        { text: 'foaf', uri: 'http://xmlns.com/foaf/0.1/' }
      ])
      // Simulate navigating to a dataset that declares no prefixes service.
      wrapper.vm.serverData = serverDataWith([querySvc])
      await nextTick()
      await flushPromises()
      expect(wrapper.vm.prefixes).deep.equals(defaults)
    })

    it('does not reload prefixes when the SPARQL endpoint input changes', async () => {
      const getPrefixes = vi.fn().mockResolvedValue({
        data: [
          { prefix: 'foaf', uri: 'http://xmlns.com/foaf/0.1/' }
        ]
      })
      const wrapper = mountFunction({
        props: { datasetName },
        mocks: { $fusekiService: fusekiServiceMock(getPrefixes) }
      })
      wrapper.vm.serverData = serverDataWith([querySvc, prefixesR])
      await nextTick()
      await flushPromises()
      expect(getPrefixes.mock.calls.length).equals(1)
      // Editing the free-text SPARQL Endpoint field must not refetch.
      wrapper.vm.currentDatasetUrl = '/other/sparql'
      await nextTick()
      await flushPromises()
      expect(getPrefixes.mock.calls.length).equals(1)
    })
  })
})
