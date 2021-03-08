'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2;

function _load_extends() {
  return _extends2 = _interopRequireDefault(require('babel-runtime/helpers/extends'));
}

var _stringify;

function _load_stringify() {
  return _stringify = _interopRequireDefault(require('babel-runtime/core-js/json/stringify'));
}

var _baseReporter;

function _load_baseReporter() {
  return _baseReporter = _interopRequireDefault(require('./base-reporter.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

class JSONReporter extends (_baseReporter || _load_baseReporter()).default {
  constructor(opts) {
    super(opts);

    this._activityId = 0;
    this._progressId = 0;
  }

  _dump(type, data, error) {
    let stdout = this.stdout;
    if (error) {
      stdout = this.stderr;
    }
    stdout.write(`${ (0, (_stringify || _load_stringify()).default)({ type: type, data: data }) }\n`);
  }

  list(type, items) {
    this._dump('list', { type: type, items: items });
  }

  tree(type, trees) {
    this._dump('tree', { type: type, trees: trees });
  }

  step(current, total, message) {
    this._dump('step', { message: message, current: current, total: total });
  }

  inspect(value) {
    this._dump('inspect', value);
  }

  footer() {
    this._dump('finished', this.getTotalTime());
  }

  log(msg) {
    this._dump('log', msg);
  }

  command(msg) {
    this._dump('command', msg);
  }

  table(head, body) {
    this._dump('table', { head: head, body: body });
  }

  success(msg) {
    this._dump('success', msg);
  }

  error(msg) {
    this._dump('error', msg, true);
  }

  warn(msg) {
    this._dump('warning', msg, true);
  }

  info(msg) {
    this._dump('info', msg);
  }

  activitySet(total, workers) {
    const id = this._activityId++;
    this._dump('activitySetStart', { id: id, total: total, workers: workers });

    const spinners = [];
    for (let i = 0; i < workers; i++) {
      let current = 0;
      let header = '';

      spinners.push({
        clear: function clear() {},
        setPrefix: function setPrefix(_current, _header) {
          current = _current;
          header = _header;
        },

        tick: msg => {
          this._dump('activitySetTick', { id: id, header: header, current: current, worker: i, message: msg });
        },
        end: function end() {}
      });
    }

    return {
      spinners: spinners,
      end: () => {
        this._dump('activitySetEnd', { id: id });
      }
    };
  }

  activity() {
    return this._activity({});
  }

  _activity(data) {
    const id = this._activityId++;
    this._dump('activityStart', (0, (_extends2 || _load_extends()).default)({ id: id }, data));

    return {
      tick: name => {
        this._dump('activityTick', { id: id, name: name });
      },

      end: () => {
        this._dump('activityEnd', { id: id });
      }
    };
  }

  progress(total) {
    const id = this._progressId++;
    let current = 0;
    this._dump('progressStart', { id: id, total: total });

    return () => {
      current++;
      this._dump('progressTick', { id: id, current: current });

      if (current === total) {
        this._dump('progressFinish', { id: id });
      }
    };
  }
}
exports.default = JSONReporter;