'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.exec = exports.queue = undefined;

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

exports.spawn = spawn;

var _constants;

function _load_constants() {
  return _constants = _interopRequireWildcard(require('../constants.js'));
}

var _blockingQueue;

function _load_blockingQueue() {
  return _blockingQueue = _interopRequireDefault(require('./blocking-queue.js'));
}

var _errors;

function _load_errors() {
  return _errors = require('../errors.js');
}

var _promise2;

function _load_promise2() {
  return _promise2 = require('./promise.js');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

/* global child_process$spawnOpts */

const child = require('child_process');

const queue = exports.queue = new (_blockingQueue || _load_blockingQueue()).default('child', (_constants || _load_constants()).CHILD_CONCURRENCY);

// TODO: this uid check is kinda whack
let uid = 0;

const exec = exports.exec = (0, (_promise2 || _load_promise2()).promisify)(child.exec);

function spawn(program, args) {
  let opts = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};
  let onData = arguments[3];

  return queue.push(opts.cwd || String(++uid), () => new (_promise || _load_promise()).default((resolve, reject) => {
    const proc = child.spawn(program, args, opts);

    let processingDone = false;
    let processClosed = false;
    let err = null;

    let stdout = '';

    proc.on('error', err => {
      if (err.code === 'ENOENT') {
        reject(new (_errors || _load_errors()).MessageError(`Couldn't find the binary ${ program }`));
      } else {
        reject(err);
      }
    });

    function updateStdout(chunk) {
      stdout += chunk;
      if (onData) {
        onData(chunk);
      }
    }

    function finish() {
      if (err) {
        reject(err);
      } else {
        resolve(stdout.trim());
      }
    }

    if (typeof opts.process === 'function') {
      opts.process(proc, updateStdout, reject, function () {
        if (processClosed) {
          finish();
        } else {
          processingDone = true;
        }
      });
    } else {
      if (proc.stderr) {
        proc.stderr.on('data', updateStdout);
      }

      if (proc.stdout) {
        proc.stdout.on('data', updateStdout);
      }

      processingDone = true;
    }

    proc.on('close', code => {
      if (code >= 1) {
        // TODO make this output nicer
        err = new (_errors || _load_errors()).SpawnError(['Command failed.', `Exit code: ${ code }`, `Command: ${ program }`, `Arguments: ${ args.join(' ') }`, `Directory: ${ opts.cwd || process.cwd() }`, `Output:\n${ stdout.trim() }`].join('\n'));
        err.EXIT_CODE = code;
      }

      if (processingDone || err) {
        finish();
      } else {
        processClosed = true;
      }
    });
  }));
}