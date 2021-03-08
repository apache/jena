"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require("babel-runtime/core-js/promise"));
}

exports.wait = wait;
exports.promisify = promisify;
exports.promisifyObject = promisifyObject;
exports.queue = queue;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function wait(delay) {
  return new (_promise || _load_promise()).default(resolve => {
    setTimeout(resolve, delay);
  });
}

function promisify(fn, firstData) {
  return function () {
    for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }

    return new (_promise || _load_promise()).default(function (resolve, reject) {
      args.push(function (err) {
        for (var _len2 = arguments.length, result = Array(_len2 > 1 ? _len2 - 1 : 0), _key2 = 1; _key2 < _len2; _key2++) {
          result[_key2 - 1] = arguments[_key2];
        }

        let res = result;

        if (result.length <= 1) {
          res = result[0];
        }

        if (firstData) {
          res = err;
          err = null;
        }

        if (err) {
          reject(err);
        } else {
          resolve(res);
        }
      });

      fn.apply(null, args);
    });
  };
}

function promisifyObject(obj) {
  const promisedObj = {};
  for (const key in obj) {
    promisedObj[key] = promisify(obj[key]);
  }
  return promisedObj;
}

function queue(arr, promiseProducer) {
  let concurrency = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : Infinity;

  concurrency = Math.min(concurrency, arr.length);

  // clone
  arr = arr.slice();

  const results = [];
  let total = arr.length;
  if (!total) {
    return (_promise || _load_promise()).default.resolve(results);
  }

  return new (_promise || _load_promise()).default((resolve, reject) => {
    for (let i = 0; i < concurrency; i++) {
      next();
    }

    function next() {
      const item = arr.shift();
      const promise = promiseProducer(item);

      promise.then(function (result) {
        results.push(result);

        total--;
        if (total === 0) {
          resolve(results);
        } else {
          if (arr.length) {
            next();
          }
        }
      }, reject);
    }
  });
}