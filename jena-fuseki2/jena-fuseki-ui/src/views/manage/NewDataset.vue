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
  <b-container fluid>
    <b-row class="mt-4">
      <b-col cols="12">
        <h2>New dataset</h2>
        <b-card no-body>
          <b-card-header header-tag="nav">
            <Menu />
          </b-card-header>
          <b-card-body>
            <b-container fluid>
              <b-row>
                <b-col cols="12">
                  <b-form
                    @submit="onSubmit"
                    ref="form"
                  >
                    <b-form-group
                      id="dataset-name-group"
                      label="Dataset name"
                      label-for="dataset-name"
                      label-cols="4"
                      label-cols-lg="2"
                      label-size="sm"
                    >
                      <b-form-input
                        pattern="[^\s]+"
                        oninvalid="this.setCustomValidity('Enter a valid dataset name, without spaces')"
                        oninput="this.setCustomValidity('')"
                        id="dataset-name"
                        v-model="form.datasetName"
                        type="text"
                        placeholder="dataset name"
                        required
                        trim
                      ></b-form-input>
                    </b-form-group>
                    <b-form-group
                      id="dataset-type-group"
                      label="Dataset type"
                      label-for="dataset-type"
                      label-cols="4"
                      label-cols-lg="2"
                      label-size="sm"
                    >
                      <b-form-radio-group
                        :options="datasetTypes"
                        value-field="item"
                        text-field="name"
                        v-model="form.datasetType"
                        name="dataset-type"
                        required
                        stacked
                      >
                      </b-form-radio-group>
                    </b-form-group>
                    <b-button
                      type="submit"
                      variant="primary"
                    >
                      <FontAwesomeIcon icon="check" />
                      <span class="ml-1">create dataset</span>
                    </b-button>
                  </b-form>
                </b-col>
              </b-row>
           </b-container>
          </b-card-body>
        </b-card>
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import Menu from '@/components/manage/Menu'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

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
        {
          item: 'tdb',
          name: 'Persistent – dataset will persist across Fuseki restart'
        },
        {
          item: 'tdb2',
          name: 'Persistent (TDB2) – dataset will persist across Fuseki restarts'
        }
      ]
    }
  },

  methods: {
    validateDatasetName () {
      // no spaces
      if (this.form.datasetName && this.form.datasetName.includes(' ')) {
        return false
      }
      return null
    },
    async onSubmit (evt) {
      evt.preventDefault()
      try {
        await this.$fusekiService.createDataset(this.form.datasetName, this.form.datasetType)
        await this.$router.push('/manage')
        this.$bvToast.toast(`Dataset "${this.form.datasetName}" created`, {
          title: 'Notification',
          autoHideDelay: 5000,
          appendToast: false
        })
      } catch (error) {
        this.$bvToast.toast(`${error}`, {
          title: 'Error',
          noAutoHide: true,
          appendToast: false
        })
      }
    }
  }
}
</script>
