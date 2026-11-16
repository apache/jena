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
import { mount } from '@vue/test-utils'
import { vi } from 'vitest'
import Query from '@/views/dataset/Query.vue'

const { createEditor, createHttpExecutor, execute } = vi.hoisted(() => ({
  createEditor: vi.fn(), createHttpExecutor: vi.fn(), execute: vi.fn()
}))
vi.mock('@ldforge/editor', () => ({ createEditor }))
vi.mock('@ldforge/core', () => ({ createHttpExecutor, createQueryEndpointExecutor: ({ executor }) => executor }))

function mountQuery (query) {
  return mount(Query, {
    shallow: true,
    props: { datasetName: 'demo' },
    global: {
      mocks: {
        $router: { resolve: ({ path }) => ({ href: `#${path}` }) },
        $route: { path: '/dataset/demo/query', query: query === undefined ? {} : { query } },
        $fusekiService: { getFusekiUrl: path => `/fuseki/${path.replace(/^\//, '')}` }
      }
    }
  })
}

describe('LDForge query view', () => {
  let editor
  let wrapper
  beforeEach(() => {
    localStorage.clear()
    editor = { destroy: vi.fn(), cancel: vi.fn(), setQuery: vi.fn(), getQuery: vi.fn(() => 'ASK {}'),
      getPrefixes: vi.fn(() => ({})), addPrefix: vi.fn(), removePrefix: vi.fn() }
    createEditor.mockReturnValue(editor)
    createHttpExecutor.mockReturnValue({ execute })
  })
  afterEach(() => wrapper?.unmount())

  it('uses a shared query verbatim, including an empty query', () => {
    wrapper = mountQuery('')
    expect(createEditor.mock.calls[0][1].query).toBe('')
    expect(createEditor.mock.calls[0][1].themeMainColor).toBe('#1a80b9')
  })

  it('resolves the current endpoint under the Fuseki deployment prefix and forwards cancellation', async () => {
    wrapper = mountQuery()
    expect(wrapper.find('#sparql-endpoint').element.value).toBe('/demo/sparql')
    const request = { query: 'ASK {}', signal: new AbortController().signal }
    createEditor.mock.calls[0][1].executor.execute(request)
    expect(createHttpExecutor).toHaveBeenCalledWith({
      endpoint: new URL('/fuseki/demo/sparql', document.baseURI).href
    })
    expect(execute).toHaveBeenCalledWith(request)
    await wrapper.setData({ currentDatasetUrl: '/demo/query' })
    expect(editor.cancel).toHaveBeenCalled()
    createEditor.mock.calls[0][1].executor.execute(request)
    expect(createHttpExecutor).toHaveBeenLastCalledWith({
      endpoint: new URL('/fuseki/demo/query', document.baseURI).href
    })
  })

  it('saves query edits per dataset and restores them on a later visit', () => {
    wrapper = mountQuery()
    createEditor.mock.calls[0][1].onQueryChange('ASK {}')
    wrapper.unmount()
    wrapper = mountQuery()
    expect(createEditor.mock.calls[1][1].query).toBe('ASK {}')
  })

  it('destroys the editor on navigation and isolates another dataset', async () => {
    wrapper = mountQuery()
    createEditor.mock.calls[0][1].onQueryChange('ASK {}')
    await wrapper.setProps({ datasetName: 'other' })
    expect(wrapper.find('#sparql-endpoint').element.value).toBe('/other/sparql')
    expect(editor.destroy).toHaveBeenCalledOnce()
    expect(createEditor.mock.calls[1][1].query).toContain('SELECT ?subject')
    wrapper.unmount()
    expect(editor.destroy).toHaveBeenCalledTimes(2)
  })

  it('toggles prefix declarations through the public API and updates badge state', async () => {
    wrapper = mountQuery()
    await wrapper.vm.$nextTick()
    const rdf = wrapper.findAll('button').find(button => button.text() === 'rdf')
    expect(rdf.classes()).toContain('text-bg-light')
    editor.getPrefixes.mockReturnValue({ rdf: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#' })
    await rdf.trigger('click')
    expect(editor.addPrefix).toHaveBeenCalledWith('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
    expect(rdf.attributes('aria-pressed')).toBe('true')
    expect(rdf.classes()).toContain('text-bg-primary')
    editor.getPrefixes.mockReturnValue({})
    await rdf.trigger('click')
    expect(editor.removePrefix).toHaveBeenCalledWith('rdf')
    expect(rdf.attributes('aria-pressed')).toBe('false')
  })

  it('synchronizes prefixes after manual edits and query tab changes, including alternate aliases', async () => {
    wrapper = mountQuery()
    editor.getPrefixes.mockReturnValue({ custom: 'http://www.w3.org/2000/01/rdf-schema#' })
    createEditor.mock.calls[0][1].onQueryChange('PREFIX custom: <http://www.w3.org/2000/01/rdf-schema#> ASK {}')
    await wrapper.vm.$nextTick()
    const rdfs = wrapper.findAll('button').find(button => button.text() === 'rdfs')
    expect(rdfs.attributes('aria-pressed')).toBe('true')
    await rdfs.trigger('click')
    expect(editor.removePrefix).toHaveBeenCalledWith('custom')
    editor.getPrefixes.mockReturnValue({})
    createEditor.mock.calls[0][1].onQueryChange('ASK {}')
    await wrapper.vm.$nextTick()
    expect(rdfs.attributes('aria-pressed')).toBe('false')
  })

  it('uses the toolbar share action with the Fuseki route and active endpoint', async () => {
    wrapper = mountQuery()
    await wrapper.find('button').trigger('click')
    expect(editor.setQuery).toHaveBeenCalledWith(expect.stringContaining('SELECT ?subject'))
    expect(wrapper.text()).not.toContain('Share query')
    expect(wrapper.find('#share-query-url').exists()).toBe(false)
    const options = createEditor.mock.calls[0][1]
    expect(options.shareBaseUrl).toContain('#/dataset/demo/query')
    expect(options.shareEndpoint()).toBe(new URL('/fuseki/demo/sparql', document.baseURI).href)
    await wrapper.setData({ currentDatasetUrl: '/other/sparql' })
    expect(options.shareEndpoint()).toBe(new URL('/fuseki/other/sparql', document.baseURI).href)
  })
})
