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

import axios from 'axios'
import qs from 'qs'
import ServerStatus from '@/model/server.status'
import { BUS } from '@/events'

const DATASET_SIZE_QUERY_1 = 'select (count(*) as ?count) {?s ?p ?o}'
const DATASET_SIZE_QUERY_2 = 'select ?g (count(*) as ?count) {graph ?g {?s ?p ?o}} group by ?g'

const DATASET_COUNT_GRAPH_QUERY_1 = 'select (count(*) as ?count) {?s ?p ?o}'
const DATASET_COUNT_GRAPH_QUERY_2 = 'select ?g (count(*) as ?count) {graph ?g {?s ?p ?o}} group by ?g'

class FusekiService {
  /**
   * @param {Location} location
   */
  constructor (location) {
    this.isOffline = true
    this.pathname = location.pathname
  }

  /**
   * Gets the Fuseki URL, only the pathname onward. The protocol, server, port, etc,
   * are left to the browser/JS engine & Vue to choose. Previously we were passing
   * strings such as `/#/ping$`. But this did not work when the application was
   * deployed on a Tomcat server, for example, where the base URL could be something
   * like `http://localhost:8080/jena-fuseki/#/`.
   *
   * See RC1 vote thread for 4.4.0 for more: https://lists.apache.org/thread/z3gb5w95oc7c4v0g1jpk9jkxm0l4b7lh
   *
   * @param {string} url - Vue route URL
   * @return {string} a new route URL that includes any pathname in the URL
   */
  getFusekiUrl (url) {
    // modified version from: https://stackoverflow.com/a/24381515
    return `${this.pathname}/${url}`.replace(/(\/)\/+/g, '$1')
  }

  async getServerData () {
    const response = await axios.get(this.getFusekiUrl('/$/server'))
    return response.data
  }

  async getServerStatus () {
    const startTime = new Date().getTime()
    try {
      await axios.get(this.getFusekiUrl('/$/ping'))
      // connection reset?
      if (this.isOffline) {
        BUS.$emit('connection:reset')
      }
      this.isOffline = false
      return new ServerStatus(true, `Last ping returned OK in ${this._duration(startTime)}ms`)
    } catch (error) {
      this.isOffline = true
      return new ServerStatus(false, `Last ping returned "${error}" in ${this._duration(startTime)}ms`)
    }
  }

  _duration (startTime) {
    return new Date().getTime() - startTime
  }

  async getDatasetStats (datasetName) {
    const response = await axios.get(this.getFusekiUrl(`/$/stats/${datasetName}`))
    return response.data
  }

  async getDatasetSize (datasetName) {
    const promisesResult = await Promise.all([
      axios
        .get(this.getFusekiUrl(`/${datasetName}/sparql`), {
          params: {
            query: DATASET_SIZE_QUERY_1
          }
        }),
      axios.get(this.getFusekiUrl(`/${datasetName}/sparql`), {
        params: {
          query: DATASET_SIZE_QUERY_2
        }
      })
    ])
    const results = {}
    const defaultGraphResult = promisesResult[0]
    results['default graph'] = defaultGraphResult.data.results.bindings[0].count.value
    const allGraphResult = promisesResult[1]
    allGraphResult.data.results.bindings.forEach(binding => {
      results[binding.g.value] = binding.count.value
    })
    return results
  }

  async deleteDataset (datasetName) {
    await axios.delete(this.getFusekiUrl(`/$/datasets${datasetName}`))
  }

  /**
   * @param datasetName
   * @returns {Promise<{
   *   data: {
   *     taskId: string,
   *     requestId: number
   *   }
   * }>}
   */
  async backupDataset (datasetName) {
    return await axios.post(this.getFusekiUrl(`/$/backup${datasetName}`))
  }

  async createDataset (datasetName, datasetType) {
    const data = qs.stringify({
      dbName: datasetName,
      dbType: datasetType
    })
    // const data = new FormData()
    // data.set('dbName', datasetName)
    // data.set('dbType', datasetType)
    const headers = {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
    try {
      await axios.post(this.getFusekiUrl('/$/datasets'), data, {
        headers
      })
    } catch (error) {
      if (error.response) {
        if (error.response.status !== 200) {
          if (error.response.status === 409) {
            throw new Error(`failed to create dataset "${datasetName}", reason: there is another dataset with the same name`)
          }
          throw new Error(`failed to create dataset "${datasetName}" with type ${datasetType}, reason: HTTP status: "${error.response.status}", message: ${error.response.statusText}`)
        }
      }
      throw error
    }
  }

  async getTasks () {
    return axios.get(this.getFusekiUrl('/$/tasks'))
  }

  async countGraphsTriples (datasetName) {
    const promisesResult = await Promise.all([
      axios
        .get(this.getFusekiUrl(`/${datasetName}/sparql`), {
          params: {
            query: DATASET_COUNT_GRAPH_QUERY_1
          }
        }),
      axios.get(this.getFusekiUrl(`/${datasetName}/sparql`), {
        params: {
          query: DATASET_COUNT_GRAPH_QUERY_2
        }
      })
    ])
    const results = {}
    const defaultGraphResult = promisesResult[0]
    results.default = defaultGraphResult.data.results.bindings[0].count.value
    const allGraphResult = promisesResult[1]
    allGraphResult.data.results.bindings.forEach(binding => {
      results[binding.g.value] = binding.count.value
    })
    return results
  }

  async fetchGraph (datasetName, graphName) {
    return await axios
      .get(this.getFusekiUrl(`/${datasetName}`), {
        params: {
          graph: graphName
        },
        headers: {
          Accept: 'text/turtle; charset=utf-8'
        }
      })
  }

  async saveGraph (datasetName, graphName, code) {
    return await axios
      .put(this.getFusekiUrl(`/${datasetName}`), code, {
        params: {
          graph: graphName
        },
        headers: {
          Accept: 'application/json, text/javascript, */*; q=0.01',
          'Content-Type': 'text/turtle; charset=UTF-8'
        }
      })
      .catch(error => {
        throw new Error(error.response.data)
      })
  }
}

export default FusekiService
