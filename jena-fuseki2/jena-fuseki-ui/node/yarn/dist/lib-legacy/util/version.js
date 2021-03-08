'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.explodeHashedUrl = explodeHashedUrl;
function explodeHashedUrl(url) {
  const parts = url.split('#');

  return {
    url: parts[0],
    hash: parts[1] || ''
  };
}