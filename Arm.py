from adafruit_servokit import ServoKit
from bluetooth import BluetoothError

class Arm:
    def __init__(self, bluetoothConnection, servoNumber, pulseWidths, angles):
        self.bluetoothConnection = bluetoothConnection
        self.servoKit = ServoKit(channels=16)
        for index in range(servoNumber):
            servo = self.servoKit.servo[index]
            servo.set_pulse_width_range(pulseWidths[index][0], pulseWidths[index][1])
            servo.angle = angles[index]

    def readCommands(self):
        # returns True if arm received "exit" command
        # and False if client disconnected
        try:
            return self.readUntilExit()
        except BluetoothError:
            print("Disconnected")
            return False

    def readUntilExit(self):
        while True:
            command = self.bluetoothConnection.readData(16)
            if command == "exit":
                return True
            self.processCommand(command)

    def processCommand(self, command):
        print(command)
        splitCommand = command.split(' ')
        if splitCommand[0] == "angle":
            servoIndex = int(splitCommand[1])
            angle = self.getNewAngle(servoIndex, splitCommand[2])
            self.changeAngle(servoIndex, angle)
        else:
            self.bluetoothConnection.writeData("Unknown command\n")

    def getNewAngle(self, servoIndex, sign):
        angle = self.servoKit.servo[servoIndex].angle
        if sign == "+":
            angle = angle + 2
        elif sign == "-":
            angle = angle - 2
        return angle

    def changeAngle(self, servoIndex, angle):
        if 0 <= angle <= 180:
            self.servoKit.servo[servoIndex].angle = angle
