'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _util;

function _load_util() {
  return _util = require('./util.js');
}

const repeat = require('repeating');

class ProgressBar {
  constructor(total) {
    let stdout = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : process.stderr;

    this.stdout = stdout;
    this.total = total;
    this.chars = ProgressBar.bars[0].split('');
    this.delay = 60;
    this.curr = 0;
    (0, (_util || _load_util()).clearLine)(stdout);
  }

  tick() {
    this.curr++;

    // schedule render
    if (!this.id) {
      this.id = setTimeout(() => this.render(), this.delay);
    }

    // progress complete
    if (this.curr >= this.total) {
      clearTimeout(this.id);
      (0, (_util || _load_util()).clearLine)(this.stdout);
    }
  }

  render() {
    // clear throttle
    clearTimeout(this.id);
    this.id = null;

    let ratio = this.curr / this.total;
    ratio = Math.min(Math.max(ratio, 0), 1);

    // progress without bar
    let bar = ` ${ this.curr }/${ this.total }`;

    // calculate size of actual bar
    // $FlowFixMe: investigate process.stderr.columns flow error
    const availableSpace = Math.max(0, this.stdout.columns - bar.length - 1);
    const width = Math.min(this.total, availableSpace);
    const completeLength = Math.round(width * ratio);
    const complete = repeat(this.chars[0], completeLength);
    const incomplete = repeat(this.chars[1], width - completeLength);
    bar = `${ complete }${ incomplete }${ bar }`;

    (0, (_util || _load_util()).toStartOfLine)(this.stdout);
    this.stdout.write(bar);
  }
}
exports.default = ProgressBar;
ProgressBar.bars = ['█░'];