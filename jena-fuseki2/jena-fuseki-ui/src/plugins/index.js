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

/*
 * A plug-in to replace Bootstrap Vue's Toasts.
 */
import { BUS } from '@/events'
import Toast from '@/components/Toast'

const ToastPlugin = {
  install (vm) {
    // Add the global $toast object.
    vm.prototype.$toast = {
      error (message, options = {}) {
        this.send(message, 'danger', options)
      },
      notification (message, options = {}) {
        this.send(message, 'primary', options)
      },
      send (message, type, options) {
        BUS.$emit('toast', {
          message,
          type,
          options
        })
      }
    }
    // Register the component for Toasts.
    vm.component('Toast', Toast)
  }
}

export {
  ToastPlugin
}
