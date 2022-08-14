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
  <div
    class="toast-container"
    aria-live="polite"
    aria-atomic="true"
  >
    <div class="position-fixed top-0 end-0 px-2" style="z-index: 11">
      <transition-group
        name="toast"
        tag="div"
        appear
      >
        <div
          v-for="message in messages"
          :key="message.id"
          class="toast show mt-2"
          role="alert"
          aria-live="assertive"
          aria-atomic="true"
        >
          <div
            :class="getToastHeaderClass(message.type)"
          >
            <strong class="me-auto text-white">
              {{ getMessageHeaderText(message.type) }}
            </strong>
            <button
              v-if="message.closeable"
              @click="removeMessage(message.id)"
              type="button"
              class="me-2 mb-1 btn-close btn-close-white"
              data-bs-dismiss="toast"
              aria-label="Close"
            >
            </button>
          </div>
          <div class="toast-body">
            {{ message.message }}
          </div>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script>
import { BUS } from '@/events'

export default {
  name: 'Toast',

  props: {
    maxMessages: {
      type: Number,
      default: 4
    },
    timeOut: {
      type: Number,
      default: 5000
    },
    closeable: {
      type: Boolean,
      default: true
    }
  },

  data () {
    return {
      ids: 0,
      messages: []
    }
  },

  created () {
    BUS.$on('toast', this.handleMessageEvent)
  },

  methods: {
    handleMessageEvent (payload) {
      if (!payload.message) {
        throw new Error('A toast payload MUST contain the .message attribute!')
      }
      const id = this.ids++
      const messageData = Object.assign(
        {},
        {
          id,
          message: payload.message,
          type: payload.type || 'notification',
          timeOut: this.timeOut,
          closeable: this.closeable,
          animationFrame: null
        },
        payload.options || {}
      )
      // The new message goes ahead of the previous messages.
      this.messages.unshift(messageData)
      if (this.messages.length > this.maxMessages) {
        this.messages.splice(this.maxMessages)
      }
      // Auto hide it.
      const vm = this
      setTimeout(() => {
        vm.removeMessage(id)
      }, this.timeOut)
    },

    getToastHeaderClass (type) {
      return [
        'toast-header',
        `bg-${type}`
      ]
    },

    removeMessage (id) {
      const messageIndex = this.messages.findIndex(message => message.id === id)
      this.messages.splice(messageIndex, 1)
    },

    getMessageHeaderText (type) {
      const messageHeaderText = type === 'danger' ? 'Error' : 'Notification'
      return messageHeaderText.charAt(0).toUpperCase() + messageHeaderText.slice(1)
    }
  }
}
</script>
