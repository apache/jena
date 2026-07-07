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
import { nextTick } from 'vue'
import Upload from '@/views/dataset/Upload.vue'
import { describe, it, expect, vi } from 'vitest'

const mountFunction = (options) => {
  return mount(Upload, Object.assign(options || {}, {
    shallow: true,
    props: {
      datasetName: 'test-ds'
    },
    global: {
      mocks: {
        $route: { params: { datasetName: 'test-ds' } },
        $fusekiService: {
          getFusekiUrl: (path) => `http://localhost:3030${path}`,
          getDatasetServices: vi.fn().mockResolvedValue({ 'gsp-rw': { 'srv.endpoints': ['data'] } })
        }
      }
    }
  }))
}

describe('Upload.vue', () => {
  describe('getFileStatus', () => {
    it('returns "danger" for a file with an error', () => {
      const wrapper = mountFunction()
      expect(wrapper.vm.getFileStatus({ error: 'some error', success: false, active: false })).toBe('danger')
    })

    it('returns "success" for a successfully uploaded file', () => {
      const wrapper = mountFunction()
      expect(wrapper.vm.getFileStatus({ error: null, success: true, active: false })).toBe('success')
    })

    it('returns "primary" for an active upload', () => {
      const wrapper = mountFunction()
      expect(wrapper.vm.getFileStatus({ error: null, success: false, active: true })).toBe('primary')
    })

    it('returns empty string for a file with no status', () => {
      const wrapper = mountFunction()
      expect(wrapper.vm.getFileStatus({ error: null, success: false, active: false })).toBe('')
    })
  })

  describe('getUploadErrorMessage', () => {
    it('returns the server\'s detailed plain-text error response, when present', () => {
      const wrapper = mountFunction()
      const file = { error: 'denied', response: '  Parse Error: [line: 3, col: 5] Triples not terminated by \'.\'  ' }
      expect(wrapper.vm.getUploadErrorMessage(file)).toBe('Parse Error: [line: 3, col: 5] Triples not terminated by \'.\'')
    })

    it('returns the message field from a JSON error response, when present', () => {
      const wrapper = mountFunction()
      const file = { error: 'denied', response: { message: 'Unsupported Media Type' } }
      expect(wrapper.vm.getUploadErrorMessage(file)).toBe('Unsupported Media Type')
    })

    it('falls back to a readable message for a generic "server" error code', () => {
      const wrapper = mountFunction()
      const file = { error: 'server', response: {} }
      expect(wrapper.vm.getUploadErrorMessage(file)).toBe('Upload failed: the server encountered an error while processing the file.')
    })

    it('falls back to a readable message for a generic "network" error code', () => {
      const wrapper = mountFunction()
      const file = { error: 'network', response: {} }
      expect(wrapper.vm.getUploadErrorMessage(file)).toBe('Upload failed: could not reach the server. Please check your connection and try again.')
    })

    it('returns a generic message when there is no file', () => {
      const wrapper = mountFunction()
      expect(wrapper.vm.getUploadErrorMessage(null)).toBe('Upload failed.')
    })
  })

  describe('file list validation', () => {
    it('does not flag the form invalid just from removing the last (failed) file', async () => {
      const wrapper = mountFunction()
      await nextTick()

      wrapper.vm.upload.files = [{ id: '1', name: 'broken.ttl', error: null, success: false, active: false, response: {} }]
      await nextTick()
      expect(wrapper.vm.fileUploadClasses).toEqual(['btn', 'btn-success', 'is-valid'])

      wrapper.vm.upload.files = [{ id: '1', name: 'broken.ttl', error: 'denied', success: false, active: false, response: 'Parse Error' }]
      await nextTick()
      expect(wrapper.vm.fileUploadClasses).toEqual(['btn', 'btn-success', 'is-valid'])

      wrapper.vm.upload.files = []
      await nextTick()
      expect(wrapper.vm.fileUploadClasses).toEqual(['btn', 'btn-success'])
    })

    it('still flags the form invalid on an explicit submit attempt with no files', () => {
      const wrapper = mountFunction()
      expect(wrapper.vm.validateFiles()).toBe(false)
      expect(wrapper.vm.fileUploadClasses).toEqual(['btn', 'btn-success', 'is-invalid'])
    })
  })
})
