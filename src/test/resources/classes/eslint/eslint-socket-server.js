var net = require('net');

var PORT = process.argv[2];

var server = net.createServer(function (socket) {
  'use strict';

  socket.on('data', function (data) {
      if (data.toString('ascii').indexOf('stop-server-request') >= 0) {
        socket.destroy();
        server.close();
      }
      socket.write(data + '\n');
  });

}).listen(PORT);