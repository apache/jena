'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

var _keys;

function _load_keys() {
  return _keys = _interopRequireDefault(require('babel-runtime/core-js/object/keys'));
}

var _stringify;

function _load_stringify() {
  return _stringify = _interopRequireDefault(require('babel-runtime/core-js/json/stringify'));
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('./map.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const debug = require('debug')('yarn');

class BlockingQueue {
  constructor(alias) {
    let maxConcurrency = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : Infinity;

    this.concurrencyQueue = [];
    this.maxConcurrency = maxConcurrency;
    this.runningCount = 0;
    this.warnedStuck = false;
    this.alias = alias;
    this.first = true;

    this.running = (0, (_map || _load_map()).default)();
    this.queue = (0, (_map || _load_map()).default)();

    this.stuckTick = this.stuckTick.bind(this);
  }

  stillActive() {
    if (this.stuckTimer) {
      clearTimeout(this.stuckTimer);
    }

    this.stuckTimer = setTimeout(this.stuckTick, 5000);
  }

  stuckTick() {
    if (this.runningCount === 1) {
      this.warnedStuck = true;
      debug(`The ${ (0, (_stringify || _load_stringify()).default)(this.alias) } blocking queue may be stuck. 5 seconds ` + `without any activity with 1 worker: ${ (0, (_keys || _load_keys()).default)(this.running)[0] }`);
    }
  }

  push(key, factory) {
    if (this.first) {
      this.first = false;
    } else {
      this.stillActive();
    }

    return new (_promise || _load_promise()).default((resolve, reject) => {
      // we're already running so push ourselves to the queue
      const queue = this.queue[key] = this.queue[key] || [];
      queue.push({ factory: factory, resolve: resolve, reject: reject });

      if (!this.running[key]) {
        this.shift(key);
      }
    });
  }

  shift(key) {
    if (this.running[key]) {
      delete this.running[key];
      this.runningCount--;

      if (this.warnedStuck) {
        this.warnedStuck = false;
        debug(`${ (0, (_stringify || _load_stringify()).default)(this.alias) } blocking queue finally resolved. Nothing to worry about.`);
      }
    }

    const queue = this.queue[key];
    if (!queue) {
      return;
    }

    var _queue$shift = queue.shift();

    const resolve = _queue$shift.resolve;
    const reject = _queue$shift.reject;
    const factory = _queue$shift.factory;

    if (!queue.length) {
      delete this.queue[key];
    }

    const next = () => {
      this.shift(key);
      this.shiftConcurrencyQueue();
    };

    const run = () => {
      this.running[key] = true;
      this.runningCount++;

      factory().then(function (val) {
        resolve(val);
        next();
        return null;
      }).catch(function (err) {
        reject(err);
        next();
      });
    };

    this.maybePushConcurrencyQueue(run);
  }

  maybePushConcurrencyQueue(run) {
    if (this.runningCount < this.maxConcurrency) {
      run();
    } else {
      this.concurrencyQueue.push(run);
    }
  }

  shiftConcurrencyQueue() {
    if (this.runningCount < this.maxConcurrency) {
      const fn = this.concurrencyQueue.shift();
      if (fn) {
        fn();
      }
    }
  }
}
exports.default = BlockingQueue;