<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<template>
  <div class="container-fluid">
    <div class="row mt-4">
      <div class="col-12">
        <h2>New dataset</h2>
        <div class="card">
          <nav class="card-header">
            <Menu />
          </nav>
          <div class="card-body">
            <div class="container-fluid">
              <div class="row">
                <div class="col-12">
                  <form
                    @submit="onSubmit"
                    ref="form"
                  >
                    <div class="row input-group has-validation align-items-center">
                      <label for="dataset-name" class="col-4 col-lg-2 form-label col-form-label-sm">Dataset name</label>
                      <div class="col g-0">
                        <input
                          v-model="form.datasetName"
                          type="text"
                          id="dataset-name"
                          ref="dataset-name"
                          class="form-control"
                          placeholder="dataset name"
                          required
                        />
                      </div>
                      <div class="invalid-feedback">
                        Please choose a dataset name.
                      </div>
                    </div>
                    <div class="row input-group has-validation align-items-center">
                      <label class="col-4 col-lg-2 form-label col-form-label-sm">Dataset type</label>
                      <div class="col">
                        <div class="row">
                          <div
                            v-for="datasetType of datasetTypes"
                            :key="datasetType.item"
                            class="form-check"
                          >
                            <input
                              :id="`data-set-type-${datasetType.item}`"
                              :value="datasetType.item"
                              v-model="form.datasetType"
                              class="form-check-input"
                              type="radio"
                              name="dataset-type"
                              required
                            >
                            <label
                              :for="`data-set-type-${datasetType.item}`"
                              :key="`data-set-type-${datasetType.item}`"
                              class="form-check-label"
                            >
                              {{ datasetType.name }}
                            </label>
                          </div>
                          <div class="invalid-feedback">
                            Please choose a dataset type.
                          </div>
                        </div>
                      </div>
                    </div>
                    <button
                      type="submit"
                      class="btn btn-primary"
                    >
                      <FontAwesomeIcon icon="check" />
                      <span class="ms-1">create dataset</span>
                    </button>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Menu from '@/components/manage/Menu.vue'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { displayError, displayNotification } from '@/utils'

library.add(faCheck)

export default {
  name: 'NewDataset',

  components: {
    Menu,
    FontAwesomeIcon
  },

  data () {
    return {
      form: {
        datasetName: null,
        datasetType: null
      },
      datasetTypes: [
        {
          item: 'mem',
          name: 'In-memory – dataset will be recreated when Fuseki restarts, but contents will be lost'
        },
        // {
        //   item: 'tdb',
        //   name: 'Persistent – dataset will persist across Fuseki restart'
        // },
        {
          item: 'tdb2',
          name: 'Persistent (TDB2) – dataset will persist across Fuseki restarts'
        }
      ]
    }
  },

  methods: {
    async onSubmit (evt) {
      evt.preventDefault()
      try {
        await this.$fusekiService.createDataset(this.form.datasetName, this.form.datasetType)
        await this.$router.push('/manage')
        displayNotification(this, `Dataset "${this.form.datasetName}" created`)
      } catch (error) {
        displayError(this, error)
      }
    }
  }
}
</script>
