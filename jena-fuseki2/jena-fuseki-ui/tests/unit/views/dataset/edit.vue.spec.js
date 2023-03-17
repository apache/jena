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

import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import Edit from '@/views/dataset/Edit.vue'

describe('Edit', () => {
  const mountFunction = options => {
    return mount(Edit, {
      ...options,
      global: {
        mocks: {
          $fusekiService: {
            async listGraphs(datasetName, endpoint) {
              return ["default"]
            }
          }
        }
      }
    })
  }
  it('show default graph without numbers by default', () => {
    const component = mountFunction({
      propsData: {
        datasetName: "myDataSet"
      }
    })
    expect(component.find('.card-body h3').text()).to.equal('Available Graphs')

    // TODO: should return 1 instead
    // expect(component.findAll('tr').length).to.equal(1)
  })
})
