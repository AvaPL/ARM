from bluetooth import BluetoothError

from connection.BufferedData import BufferedData
from exceptions.NotReachableException import NotReachableException


class CommandInterpreter:
    def __init__(self, bluetoothConnection, arm, mobilePlatform):
        self.bluetoothConnection = bluetoothConnection
        self.bufferedData = BufferedData()
        self.arm = arm
        self.mobilePlatform = mobilePlatform

    def readCommands(self):
        # Returns True if a shutdown command was received and False if the client disconnected.
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
        if command == "joints":
            self.sendJoints()
        elif command == "grab":
            self.sendGrab()
        elif command == "speed":
            self.sendSpeed()
        elif command == "kinematics":
            self.sendKinematics()
        elif command == "forward":
            self.mobilePlatform.forward()
        elif command == "back":
            self.mobilePlatform.back()
        elif command == "left":
            self.mobilePlatform.left()
        elif command == "right":
            self.mobilePlatform.right()
        elif command == "stop":
            self.mobilePlatform.stop()
        elif splitCommand[0] == "angle":
            self.changeAngle(splitCommand)
        elif splitCommand[0] == "coordinate":
            self.changeCoordinates(splitCommand)
        elif splitCommand[0] == "speed":
            self.changeSpeed(splitCommand)
        else:
            self.sendUnknownCommand()

    def sendJoints(self):
        for index in range(self.arm.servoNumber - 1):
            self.bluetoothConnection.writeData(self.getJointAngleCommand(index))
        self.sendGrab()

    def getJointAngleCommand(self, index):
        angle = round(self.arm.getAngle(index))
        return "angle " + str(index) + " " + str(angle)

    def sendGrab(self):
        self.bluetoothConnection.writeData(self.getGrabAngleCommand())

    def getGrabAngleCommand(self):
        servoIndex = self.arm.servoNumber - 1
        angle = round(self.arm.getAngle(servoIndex))
        return "angle grab " + str(angle)

    def sendSpeed(self):
        self.bluetoothConnection.writeData(self.getSpeedCommand())

    def getSpeedCommand(self):
        speed = self.mobilePlatform.speed
        return "speed " + str(speed)

    def sendKinematics(self):
        coordinates = self.arm.kinematics.getCorrectedKinematics()
        x = coordinates[0][0]
        y = coordinates[1][0]
        phi = coordinates[2][0]
        self.bluetoothConnection.writeData(self.getCoordinateCommand("x", x))
        self.bluetoothConnection.writeData(self.getCoordinateCommand("y", y))
        self.bluetoothConnection.writeData(self.getCoordinateCommand("phi", phi))

    def getCoordinateCommand(self, coordinate, value):
        roundedValue = round(value.item())
        return "coordinate " + coordinate + " " + str(roundedValue)

    def isNavigationCommand(self, command):
        navigationCommands = ["forward", "back", "left", "right"]
        return command in navigationCommands

    def changeAngle(self, splitCommand):
        servoIndex = int(splitCommand[1])
        angle = int(splitCommand[2])
        self.arm.changeAngle(servoIndex, angle)

    def changeCoordinates(self, splitCommand):
        coordinate = splitCommand[1]
        value = int(splitCommand[2])
        try:
            self.arm.changeCoordinates(coordinate, value)
        except NotReachableException:
            self.sendKinematics()

    def changeSpeed(self, splitCommand):
        speed = int(splitCommand[1])
        self.mobilePlatform.setSpeed(speed)

    def sendUnknownCommand(self):
        self.bluetoothConnection.writeData("Unknown command")
