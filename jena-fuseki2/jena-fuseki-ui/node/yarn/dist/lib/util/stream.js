'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
const invariant = require('invariant');
const stream = require('stream');
const zlib = require('zlib');

function hasGzipHeader(chunk) {
  return chunk[0] === 0x1F && chunk[1] === 0x8B && chunk[2] === 0x08;
}

class UnpackStream extends stream.Transform {
  constructor(options) {
    super(options);
    this._srcStream = null;
    this._readHeader = false;
    this.once('pipe', src => {
      this._srcStream = src;
    });
  }

  _transform(chunk, encoding, callback) {
    if (!this._readHeader) {
      this._readHeader = true;
      invariant(chunk instanceof Buffer, 'Chunk must be a buffer');
      if (hasGzipHeader(chunk)) {
        // Stop receiving data from the src stream, and pipe it instead to zlib,
        // then pipe it's output through us.
        const unzipStream = zlib.createUnzip();
        unzipStream.on('error', err => this.emit('error', err));

        const srcStream = this._srcStream;
        invariant(srcStream, 'How? To get here a stream must have been piped!');
        srcStream.pipe(unzipStream).pipe(this);

        // Unpipe after another stream has been piped so it's always piping to
        // something, thus avoiding pausing it.
        srcStream.unpipe(this);
        unzipStream.write(chunk);
        this._srcStream = null;
        callback();
        return;
      }
    }
    callback(null, chunk);
  }
}

exports.UnpackStream = UnpackStream;
class ConcatStream extends stream.Transform {
  constructor(done) {
    super();
    this._data = [];
    this._done = done;
  }

  _transform(chunk, encoding, callback) {
    invariant(chunk instanceof Buffer, 'Chunk must be a buffer');
    invariant(this._data != null, 'Missing data array');
    this._data.push(chunk);
    this.push(chunk);
    callback();
  }

  _flush(callback) {
    invariant(this._data != null, 'Missing data array');
    this._done(Buffer.concat(this._data));
    this._data = null;
    callback();
  }
}
exports.ConcatStream = ConcatStream;