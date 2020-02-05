from bluetooth import *


class BluetoothConnection:
    def __init__(self, port):
        self.serverSocket = BluetoothSocket(RFCOMM)
        self.port = port
        self.serverSocket.bind(("", port))
        self.serverSocket.listen(port)
        self.clientSocket = None
        self.newline = '\n'

    def establish(self):
        print("Waiting for a new connection...")
        self.clientSocket, address = self.serverSocket.accept()
        self.clientSocket.send(b"ARM ready\n")
        print("Accepted a connection from ", address)

    def readData(self, bytes):
        data = self.clientSocket.recv(bytes)
        return data.decode()

    def writeData(self, data):
        encodedData = (data + self.newline).encode()
        self.clientSocket.send(encodedData)

    def close(self):
        if 'clientSocket' in globals(): #TODO: Check if globals check is needed (maybe None check is enough).
            self.clientSocket.close()
        self.serverSocket.close()
        print("Sockets closed")
