'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = nullify;
function nullify() {
  let obj = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

  if (Array.isArray(obj)) {
    for (const item of obj) {
      nullify(item);
    }
  } else if (obj !== null && typeof obj === 'object' || typeof obj === 'function') {
    Object.setPrototypeOf(obj, null);
    for (const key in obj) {
      nullify(obj[key]);
    }
  }

  return obj;
}