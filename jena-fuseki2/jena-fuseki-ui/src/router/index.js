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

import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home'
import NotFound from '@/views/NotFound'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/dataset/:datasetName/query',
    name: 'DatasetQuery',
    component: () => import(/* webpackChunkName: "datasetQuery" */ '../views/dataset/Query'),
    props: true
  },
  {
    path: '/dataset/:datasetName/upload',
    name: 'DatasetUpload',
    component: () => import(/* webpackChunkName: "datasetUpload" */ '../views/dataset/Upload'),
    props: true
  },
  {
    path: '/dataset/:datasetName/edit',
    name: 'DatasetEdit',
    component: () => import(/* webpackChunkName: "datasetEdit" */ '../views/dataset/Edit'),
    props: true
  },
  {
    path: '/dataset/:datasetName/info',
    name: 'DatasetInfo',
    component: () => import(/* webpackChunkName: "datasetInfo" */ '../views/dataset/Info'),
    props: true
  },
  {
    path: '/manage',
    name: 'ManageDatasets',
    component: () => import(/* webpackChunkName: "manageDatasets" */ '../views/manage/ExistingDatasets')
  },
  {
    path: '/manage/new',
    name: 'NewDataset',
    component: () => import(/* webpackChunkName: "newDataset" */ '../views/manage/NewDataset')
  },
  {
    path: '/manage/tasks',
    name: 'Tasks',
    component: () => import(/* webpackChunkName: "tasks" */ '../views/manage/Tasks')
  },
  {
    path: '/documentation',
    name: 'Help',
    component: () => import(/* webpackChunkName: "documentation" */ '../views/Help')
  },
  {
    path: '*',
    name: 'Not Found',
    component: NotFound
  }
]

const router = new VueRouter({
  routes
})

export default router
