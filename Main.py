from adafruit_servokit import ServoKit
from bluetooth import *

servoKit = ServoKit(channels=16)
servoKit.servo[0].set_pulse_width_range(800, 2200)
servoKit.servo[1].set_pulse_width_range(800, 2200)
servoKit.servo[2].set_pulse_width_range(800, 2200)
servoKit.servo[3].set_pulse_width_range(1600, 2200)

servoKit.servo[0].angle = 135;
servoKit.servo[1].angle = 50;
servoKit.servo[2].angle = 110;
servoKit.servo[3].angle = 90;

exit = False

def establishConnection():
    global clientSocket
    print("Waiting for a new connection...")
    clientSocket, address = serverSocket.accept()
    print("Accepted a connection from ", address)


def readData():
    try:
        global exit
        while not exit:
            data = clientSocket.recv(16)
            command = decodeCommand(data)
            processCommand(command)
    except BluetoothError:
        print("Disconnected")


def decodeCommand(data):
    command = data.decode(sys.stdout.encoding)
    return command.rstrip()


def processCommand(command):
    print(command)
    splitCommand = command.split(' ')
    if splitCommand[0] == "angle":
        servoIndex = int(splitCommand[1])
        angle = getNewAngle(servoIndex, splitCommand[2])
        changeAngle(servoIndex, angle)
    elif splitCommand[0] == "exit\n":
        global exit
        exit = True


def getNewAngle(servoIndex, sign):
    angle = servoKit.servo[servoIndex].angle
    if sign == "+\n":
        angle = angle + 2
    elif sign == "-\n":
        angle = angle - 2
    return angle


def changeAngle(servoIndex, angle):
    if 0 <= angle <= 180:
        servoKit.servo[servoIndex].angle = angle

def closeSockets():
    if 'clientSocket' in globals():
        clientSocket.close()
    serverSocket.close()
    print("Sockets closed")

serverSocket = BluetoothSocket(RFCOMM)
port = 1
serverSocket.bind(("", port))
serverSocket.listen(port)

try:
    while not exit:  # establish a new connection if the client disconnected
        establishConnection()
        readData()
except KeyboardInterrupt:
    pass
finally:
    closeSockets()
