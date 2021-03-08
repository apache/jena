"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.sortAlpha = sortAlpha;
exports.entries = entries;
exports.removePrefix = removePrefix;
exports.removeSuffix = removeSuffix;
exports.addSuffix = addSuffix;
function sortAlpha(a, b) {
  // sort alphabetically
  return a.toLowerCase().localeCompare(b.toLowerCase());
}

function entries(obj) {
  const entries = [];
  if (obj) {
    for (const key in obj) {
      entries.push([key, obj[key]]);
    }
  }
  return entries;
}

function removePrefix(pattern, prefix) {
  if (pattern.startsWith(prefix)) {
    pattern = pattern.slice(prefix.length);
  }

  return pattern;
}

function removeSuffix(pattern, suffix) {
  if (pattern.endsWith(suffix)) {
    return pattern.slice(0, -suffix.length);
  }

  return pattern;
}

function addSuffix(pattern, suffix) {
  if (!pattern.endsWith(suffix)) {
    return pattern + suffix;
  }

  return pattern;
}