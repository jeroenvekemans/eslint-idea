var net = require('net');

var PORT = process.argv[2];

net.createServer(function (socket) {
  'use strict';

  socket.on('data', function (data) {
      socket.write(data + '\n');
  });

}).listen(PORT);