from adafruit_servokit import ServoKit
from bluetooth import *
from BluetoothConnection import BluetoothConnection

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


def readCommands():
    try:
        global exit
        while not exit:
            command = bluetoothConnection.readData(16)
            processCommand(command)
    except BluetoothError:
        print("Disconnected")


def processCommand(command):
    print(command)
    splitCommand = command.split(' ')
    if splitCommand[0] == "angle":
        servoIndex = int(splitCommand[1])
        angle = getNewAngle(servoIndex, splitCommand[2])
        changeAngle(servoIndex, angle)
    elif splitCommand[0] == "exit":
        global exit
        exit = True


def getNewAngle(servoIndex, sign):
    angle = servoKit.servo[servoIndex].angle
    if sign == "+":
        angle = angle + 2
    elif sign == "-":
        angle = angle - 2
    return angle


def changeAngle(servoIndex, angle):
    if 0 <= angle <= 180:
        servoKit.servo[servoIndex].angle = angle


bluetoothConnection = BluetoothConnection(port=1)

try:
    while not exit:  # establish a new connection if the client disconnected
        bluetoothConnection.establish()
        readCommands()
except KeyboardInterrupt:
    pass
finally:
    bluetoothConnection.close()
