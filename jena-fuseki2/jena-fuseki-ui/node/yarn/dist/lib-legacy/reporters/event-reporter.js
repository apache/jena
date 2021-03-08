'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _assign;

function _load_assign() {
  return _assign = _interopRequireDefault(require('babel-runtime/core-js/object/assign'));
}

var _jsonReporter;

function _load_jsonReporter() {
  return _jsonReporter = _interopRequireDefault(require('./json-reporter.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _require = require('events');

const EventEmitter = _require.EventEmitter;
class EventReporter extends (_jsonReporter || _load_jsonReporter()).default {

  constructor(opts) {
    super(opts);

    // $FlowFixMe: looks like a flow bug
    EventEmitter.call(this);
  }

  _dump(type, data) {
    this.emit(type, data);
  }
}

exports.default = EventReporter; // $FlowFixMe: need to "inherit" from it

(0, (_assign || _load_assign()).default)(EventReporter.prototype, EventEmitter.prototype);