'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

var _stringify;

function _load_stringify() {
  return _stringify = _interopRequireDefault(require('babel-runtime/core-js/json/stringify'));
}

exports.stringifyLangArgs = stringifyLangArgs;

var _format;

function _load_format() {
  return _format = require('./format.js');
}

var _index;

function _load_index() {
  return _index = _interopRequireWildcard(require('./lang/index.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const util = require('util');
/* eslint no-unused-vars: 0 */

function stringifyLangArgs(args) {
  return args.map(function (val) {
    if (val != null && val.inspect) {
      return val.inspect();
    } else {
      try {
        return (0, (_stringify || _load_stringify()).default)(val) || val + '';
      } catch (e) {
        return util.inspect(val);
      }
    }
  });
}

class BaseReporter {
  constructor() {
    let opts = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

    const lang = 'en';
    this.language = lang;

    this.stdout = opts.stdout || process.stdout;
    this.stderr = opts.stderr || process.stderr;
    this.stdin = opts.stdin || process.stdin;
    this.emoji = !!opts.emoji;

    // $FlowFixMe: this is valid!
    this.isTTY = this.stdout.isTTY;

    this.peakMemory = 0;
    this.startTime = Date.now();
    this.format = (_format || _load_format()).defaultFormatter;
  }

  lang(key) {
    const msg = (_index || _load_index())[this.language][key] || (_index || _load_index()).en[key];
    if (!msg) {
      throw new ReferenceError(`Unknown language key ${ key }`);
    }

    // stringify args

    for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
      args[_key - 1] = arguments[_key];
    }

    const stringifiedArgs = stringifyLangArgs(args);

    // replace $0 placeholders with args
    return msg.replace(/\$(\d+)/g, (str, i) => {
      return stringifiedArgs[i];
    });
  }

  initPeakMemoryCounter() {
    this.checkPeakMemory();
    this.peakMemoryInterval = setInterval(() => {
      this.checkPeakMemory();
    }, 1000);
  }

  checkPeakMemory() {
    var _process$memoryUsage = process.memoryUsage();

    const heapTotal = _process$memoryUsage.heapTotal;

    if (heapTotal > this.peakMemory) {
      this.peakMemory = heapTotal;
    }
  }

  close() {
    if (this.peakMemoryInterval) {
      clearInterval(this.peakMemoryInterval);
      this.peakMemoryInterval = null;
    }
  }

  getTotalTime() {
    return Date.now() - this.startTime;
  }

  // TODO
  list(key, items) {}

  // TODO
  tree(key, obj) {}

  // called whenever we begin a step in the CLI.
  step(current, total, message, emoji) {}

  // a error message has been triggered. this however does not always meant an abrupt
  // program end.
  error(message) {}

  // an info message has been triggered. this provides things like stats and diagnostics.
  info(message) {}

  // a warning message has been triggered.
  warn(message) {}

  // a success message has been triggered.
  success(message) {}

  // a simple log message
  log(message) {}

  // a shell command has been executed
  command(command) {}

  // inspect and pretty-print any value
  inspect(value) {}

  // the screen shown at the very start of the CLI
  header(command, pkg) {}

  // the screen shown at the very end of the CLI
  footer(showPeakMemory) {}

  //
  table(head, body) {}

  // render an activity spinner and return a function that will trigger an update
  activity() {
    return {
      tick: function tick(name) {},
      end: function end() {}
    };
  }

  //
  activitySet(total, workers) {
    return {
      spinners: Array(workers).fill({
        clear: function clear() {},
        setPrefix: function setPrefix() {},
        tick: function tick() {},
        end: function end() {}
      }),
      end: function end() {}
    };
  }

  //
  question(question) {
    let options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    return (_promise || _load_promise()).default.reject(new Error('Not implemented'));
  }

  //
  questionAffirm(question) {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const condition = true; // trick eslint

      while (condition) {
        let answer = yield _this.question(question);
        answer = answer.toLowerCase();

        if (answer === 'y' || answer === 'yes') {
          return true;
        }
        if (answer === 'n' || answer === 'no') {
          return false;
        }

        _this.error('Invalid answer for question');
      }

      return false;
    })();
  }

  // prompt the user to select an option from an array
  select(header, question, options) {
    return (_promise || _load_promise()).default.reject(new Error('Not implemented'));
  }

  // render a progress bar and return a function which when called will trigger an update
  progress(total) {
    return function () {};
  }
}
exports.default = BaseReporter;