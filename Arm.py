from adafruit_servokit import ServoKit
from bluetooth import BluetoothError

from BufferedData import BufferedData


class Arm:
    def __init__(self, bluetoothConnection, servoNumber, pulseWidths, initialAngles):
        self.bluetoothConnection = bluetoothConnection
        self.servoNumber = servoNumber
        self.servoKit = ServoKit(channels=16)
        for index in range(servoNumber):
            servo = self.servoKit.servo[index]
            servo.set_pulse_width_range(pulseWidths[index][0], pulseWidths[index][1])
            servo.angle = initialAngles[index]
        self.bufferedData = BufferedData()

    def readCommands(self):
        # returns True if the arm received a shutdown command
        # and False if the client disconnected
        try:
            return self.readUntilShutdown()
        except BluetoothError:
            print("Disconnected")
            return False

    def readUntilShutdown(self):
        while True:
            data = self.bluetoothConnection.readData(1024)
            self.bufferedData.append(data)
            commands = self.bufferedData.flush()
            for command in commands:
                print(command)
                if command == "shutdown":
                    return True
                self.processCommand(command)

    def processCommand(self, command):
        splitCommand = command.split(' ')
        if command == "angles":
            self.sendAngles()
        elif self.isNavigationCommand(command):
            pass # TODO: Implement movement.
        elif splitCommand[0] == "angle":
            self.changeAngle(splitCommand)
        else:
            self.sendUnknownCommand()

    def sendAngles(self):
        for index in range(self.servoNumber - 1):
            self.bluetoothConnection.writeData(self.getJointAngleCommand(index))
        self.bluetoothConnection.writeData(self.getGrabAngleCommand())

    def isNavigationCommand(self, command):
        navigationCommands = ["forward", "back", "left", "right"]
        return command in navigationCommands

    def getJointAngleCommand(self, index):
        return "angle " + str(index) + " " + self.getAngle(index)

    def getAngle(self, index):
        angle = self.servoKit.servo[index].angle
        return str(round(angle))

    def getGrabAngleCommand(self):
        index = self.servoNumber - 1
        return "angle grab " + self.getAngle(index)

    def changeAngle(self, splitCommand):
        servoIndex = int(splitCommand[1])
        angle = int(splitCommand[2])
        if 0 <= angle <= 180:
            self.servoKit.servo[servoIndex].angle = angle

    def sendUnknownCommand(self):
        self.bluetoothConnection.writeData("Unknown command")
