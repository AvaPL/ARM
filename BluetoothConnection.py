from bluetooth import *


class BluetoothConnection:
    def __init__(self, port):
        self.serverSocket = BluetoothSocket(RFCOMM)
        self.port = port
        self.serverSocket.bind(("", self.port))
        self.serverSocket.listen(self.port)
        self.clientSocket = None

    def establish(self):
        print("Waiting for a new connection...")
        self.clientSocket, address = self.serverSocket.accept()
        self.clientSocket.send(b"ARM ready\n")
        print("Accepted a connection from ", address)

    def readData(self, bytes):
        data = self.clientSocket.recv(bytes)
        return self.decodeData(data)

    def decodeData(self, data):
        decodedData = data.decode()
        return decodedData.rstrip(" \r\n")  # return without whitespaces

    def writeData(self, data):
        encodedData = data.encode()
        self.clientSocket.send(encodedData)

    def close(self):
        if 'clientSocket' in globals():
            self.clientSocket.close()
        self.serverSocket.close()
        print("Sockets closed")
