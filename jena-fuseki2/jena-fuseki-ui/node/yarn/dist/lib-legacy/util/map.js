'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _setPrototypeOf;

function _load_setPrototypeOf() {
  return _setPrototypeOf = _interopRequireDefault(require('babel-runtime/core-js/object/set-prototype-of'));
}

exports.default = nullify;

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function nullify() {
  let obj = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

  if (Array.isArray(obj)) {
    for (const item of obj) {
      nullify(item);
    }
  } else if (obj !== null && typeof obj === 'object' || typeof obj === 'function') {
    (0, (_setPrototypeOf || _load_setPrototypeOf()).default)(obj, null);
    for (const key in obj) {
      nullify(obj[key]);
    }
  }

  return obj;
}