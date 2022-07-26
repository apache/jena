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

import { expect } from 'chai'
import sinon from 'sinon'
import FusekiService from '@/services/fuseki.service'
import axios from 'axios'
import { BUS } from '@/events/index'
import ServerStatus from '@/model/server.status'

describe('FusekiService', () => {
  let fusekiService
  let clock
  beforeEach(async () => {
    fusekiService = new FusekiService({
      pathname: ''
    })
    // Let's freeze time! So we always get responses with `0ms` in the message.
    clock = sinon.useFakeTimers(new Date().getTime())
  })
  afterEach(() => {
    clock.restore()
  })
  it('starts offline', () => {
    expect(fusekiService.isOffline).to.equal(true)
  })
  it('returns server data', async () => {
    const stub = sinon.stub(axios, 'get')
    stub.resolves(Promise.resolve({
      data: 42
    }))
    const data = await fusekiService.getServerData()
    expect(data).to.equal(42)
    expect(stub.calledWith('/$/server')).to.equal(true)
    stub.restore()
  })
  it('gets server status when initially offline', async () => {
    const expected = new ServerStatus(true, 'Last ping returned OK in 0ms')
    const stub = sinon.stub(axios, 'get')
    const bus = sinon.stub(BUS, 'emit')
    stub.resolves(Promise.resolve({}))
    const serverStatus = await fusekiService.getServerStatus()
    expect(stub.calledWith('/$/ping')).to.equal(true, 'Ping URL not called')
    expect(bus.called).to.equal(true, 'Event BUS was not called')
    expect(serverStatus).to.deep.equal(expected, 'Invalid server status returned')
    expect(fusekiService.isOffline).to.equal(false, 'Wrong offline status in Fuseki Service')
    stub.restore()
    bus.restore()
  })
  it('gets server status when initially online', async () => {
    const expected = new ServerStatus(true, 'Last ping returned OK in 0ms')
    const stub = sinon.stub(axios, 'get')
    const bus = sinon.stub(BUS, 'emit')
    stub.resolves(Promise.resolve({}))
    // pretend it's online!
    fusekiService.isOffline = false
    const serverStatus = await fusekiService.getServerStatus()
    expect(stub.calledWith('/$/ping')).to.equal(true, 'Ping URL not called')
    expect(bus.called).to.equal(false, 'Event BUS was called when it should not')
    expect(serverStatus).to.deep.equal(expected, 'Invalid server status returned')
    expect(fusekiService.isOffline).to.equal(false, 'Wrong offline status in Fuseki Service')
    stub.restore()
    bus.restore()
  })
  it('gets server status when initially online, but backend is offline', async () => {
    const expected = new ServerStatus(false, 'Last ping returned "Error: jena" in 0ms')
    const stub = sinon.stub(axios, 'get')
    const bus = sinon.stub(BUS, 'emit')
    stub.resolves(Promise.reject(new Error('jena')))
    // pretend it's online!
    fusekiService.isOffline = false
    const serverStatus = await fusekiService.getServerStatus()
    expect(stub.calledWith('/$/ping')).to.equal(true, 'Ping URL not called')
    expect(bus.called).to.equal(false, 'Event BUS was called when it should not')
    expect(serverStatus).to.deep.equal(expected, 'Invalid server status returned')
    expect(fusekiService.isOffline).to.equal(true, 'Wrong offline status in Fuseki Service')
    stub.restore()
    bus.restore()
  })
  it('gets stats for dataset', async () => {
    const stub = sinon.stub(axios, 'get')
    stub.resolves(Promise.resolve({
      data: 42
    }))
    const data = await fusekiService.getDatasetStats('jena')
    expect(data).to.equal(42)
    expect(stub.calledWith('/$/stats/jena')).to.equal(true)
    stub.restore()
  })
  it('gets the dataset size', async () => {
    const stub = sinon.stub(axios, 'get')
    stub.onCall(0).resolves(Promise.resolve({
      data: {
        results: {
          bindings: [
            {
              count: {
                value: 42
              }
            }
          ]
        }
      }
    }))
    stub.onCall(1).resolves(Promise.resolve({
      data: {
        results: {
          bindings: [
            {
              g: {
                value: 'test'
              },
              count: {
                value: 13
              }
            }
          ]
        }
      }
    }))
    const data = await fusekiService.getDatasetSize('jena')
    expect(data).to.deep.equal({
      'default graph': 42,
      test: 13
    })
    expect(stub.calledTwice).to.equal(true)
    stub.restore()
  })
  it('calls delete endpoint', async () => {
    const stub = sinon.stub(axios, 'delete')
    stub.resolves(Promise.resolve({
      data: 42
    }))
    await fusekiService.deleteDataset('/jena')
    expect(stub.calledWith('/$/datasets/jena')).to.equal(true)
    stub.restore()
  })
  it('calls backup endpoint', async () => {
    const stub = sinon.stub(axios, 'post')
    stub.resolves(Promise.resolve({
      data: 42
    }))
    await fusekiService.backupDataset('/jena')
    expect(stub.calledWith('/$/backup/jena')).to.equal(true)
    stub.restore()
  })
  it('creates dataset', async () => {
    const stub = sinon.stub(axios, 'post')
    stub.resolves(Promise.resolve({}))
    await fusekiService.createDataset('jena', 'tbd2')
    expect(stub.called).to.equal(true)
    stub.restore()
  })
  it('creates dataset but it raises a strange error', async () => {
    const stub = sinon.stub(axios, 'post')
    stub.resolves(Promise.reject(new Error('jena')))
    try {
      await fusekiService.createDataset('jena', 'tbd2')
      expect.fail('Not supposed to get here')
    } catch (error) {
      expect(error.message).to.equal('jena')
    }
    expect(stub.called).to.equal(true)
    stub.restore()
  })
  it('creates dataset but it raises a strange error with status code 200?', async () => {
    const stub = sinon.stub(axios, 'post')
    const error = new Error('jena')
    error.response = {
      // not supposed to happen... but...
      status: 200
    }
    stub.resolves(Promise.reject(error))
    try {
      await fusekiService.createDataset('jena', 'tbd2')
      expect.fail('Not supposed to get here')
    } catch (error) {
      expect(error.message).to.equal('jena')
    }
    expect(stub.called).to.equal(true)
    stub.restore()
  })
  it('creates dataset but it raises a known error with status code 409', async () => {
    const stub = sinon.stub(axios, 'post')
    const error = new Error('jena')
    error.response = {
      status: 409
    }
    stub.resolves(Promise.reject(error))
    try {
      await fusekiService.createDataset('jena', 'tbd2')
      expect.fail('Not supposed to get here')
    } catch (error) {
      expect(error.message).to.equal('failed to create dataset "jena", reason: there is another dataset with the same name')
    }
    expect(stub.called).to.equal(true)
    stub.restore()
  })
  it('creates dataset but it raises a known error with status code different than 409', async () => {
    const stub = sinon.stub(axios, 'post')
    const error = new Error('jena')
    error.response = {
      status: 501,
      statusText: 'test'
    }
    stub.resolves(Promise.reject(error))
    try {
      await fusekiService.createDataset('jena', 'tdb2')
      expect.fail('Not supposed to get here')
    } catch (error) {
      const expected = 'failed to create dataset "jena" with type tdb2, reason: HTTP status: "501", message: test'
      expect(error.message).to.equal(expected)
    }
    expect(stub.called).to.equal(true)
    stub.restore()
  })
  it('gets tasks for dataset', async () => {
    const stub = sinon.stub(axios, 'get')
    stub.resolves(Promise.resolve({
      id: 42
    }))
    const tasks = await fusekiService.getTasks()
    expect(tasks).to.deep.equal({ id: 42 })
    expect(stub.calledWith('/$/tasks')).to.equal(true)
    stub.restore()
  })
  it('counts the graph triples', async () => {
    // TODO: Aren't countGraphsTriples and getDatasetSize too similar? Perhaps they could be combined?
    const stub = sinon.stub(axios, 'get')
    stub.onCall(0).resolves(Promise.resolve({
      data: {
        results: {
          bindings: [
            {
              count: {
                value: 42
              }
            }
          ]
        }
      }
    }))
    stub.onCall(1).resolves(Promise.resolve({
      data: {
        results: {
          bindings: [
            {
              g: {
                value: 'test'
              },
              count: {
                value: 13
              }
            }
          ]
        }
      }
    }))
    const data = await fusekiService.countGraphsTriples('jena')
    expect(data).to.deep.equal({
      default: 42,
      test: 13
    })
    expect(stub.calledTwice).to.equal(true)
    stub.restore()
  })
  it('fetches graphs', async () => {
    const stub = sinon.stub(axios, 'get')
    stub.resolves(Promise.resolve({
      data: 42
    }))
    const graph = await fusekiService.fetchGraph('jena', 'default')
    expect(stub.called).to.equal(true)
    expect(graph).to.deep.equal({ data: 42 })
    stub.restore()
  })
  it('saves graphs', async () => {
    const stub = sinon.stub(axios, 'put')
    stub.resolves(Promise.resolve({
      data: 42
    }))
    const graph = await fusekiService.saveGraph('jena', 'default', 'abc')
    expect(stub.called).to.equal(true)
    expect(graph).to.deep.equal({ data: 42 })
    stub.restore()
  })
  it('saves graphs but results in an error', async () => {
    const stub = sinon.stub(axios, 'put')
    const error = new Error()
    error.response = {
      data: '42'
    }
    stub.resolves(Promise.reject(error))
    try {
      await fusekiService.saveGraph('jena', 'default', 'abc')
      expect.fail('Not supposed to get here')
    } catch (error) {
      expect(error.message).to.be.equal('42')
    }
    expect(stub.called).to.equal(true)
    stub.restore()
  })
  it('creates a valid URL when using a URL graph name', () => {
    // pathname is managed by the browser, we don't need to test if it has
    // multiple `/`'s... in case it does, the only way to call this code
    // is if the server accepted the URL like that, so it should be OK to
    // keep using it as it is.
    const tests = [
      {
        pathname: '/',
        url: '/ds/data?graph=http://example.com',
        expected: '/ds/data?graph=http://example.com'
      },
      {
        pathname: '/',
        url: '//ds/data?graph=http://example.com',
        expected: '/ds/data?graph=http://example.com'
      },
      {
        pathname: '',
        url: '//ds/data?graph=http://example.com',
        expected: '/ds/data?graph=http://example.com'
      },
      {
        pathname: '',
        url: '',
        expected: '/'
      },
      {
        pathname: '/',
        url: '/ds/data?graph=',
        expected: '/ds/data?graph='
      },
      {
        pathname: '/',
        url: '/ds/data?graph=default',
        expected: '/ds/data?graph=default'
      }
    ]
    const originalPathname = fusekiService.pathname
    for (const test of tests) {
      fusekiService.pathname = test.pathname
      expect(fusekiService.getFusekiUrl(test.url)).to.equal(test.expected)
    }
    fusekiService.pathname = originalPathname
  })
})
