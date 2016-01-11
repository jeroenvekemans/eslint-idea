var net = require('net');
var CLIEngine = require('eslint').CLIEngine;
var cli = new CLIEngine();

var PORT = process.argv[2];

net.createServer(function (socket) {
  'use strict';

  socket.on('data', function (data) {
      try {
          var request = JSON.parse(data);
          var report = cli.executeOnText(request.code, request.path);
          socket.write(JSON.stringify(report) + '\n');
      } catch (e) {
          socket.write(JSON.stringify([]) + '\n');
      }
  });

}).listen(PORT);
