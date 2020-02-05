from adafruit_servokit import ServoKit
# from bluetooth import BluetoothError

from ArmKinematics import ArmKinematics
# from connection.BufferedData import BufferedData
# from exceptions.NotReachableException import NotReachableException


class Arm:
    # def __init__(self, bluetoothConnection, servoNumber, pulseWidths, initialAngles):
    def __init__(self, servoNumber, pulseWidths, initialAngles):
        # self.bluetoothConnection = bluetoothConnection
        # self.bufferedData = BufferedData()
        self.servoNumber = servoNumber
        self.servoKit = ServoKit(channels=16)
        for index in range(servoNumber):
            servo = self.servoKit.servo[index]
            servo.set_pulse_width_range(pulseWidths[index][0], pulseWidths[index][1])
            servo.angle = initialAngles[index]
        self.kinematics = ArmKinematics(q1=self.getAngle(0), q2=self.getAngle(1), q3=self.getAngle(2))

    # def readCommands(self):
    #     # returns True if the arm received a shutdown command
    #     # and False if the client disconnected
    #     try:
    #         return self.readUntilShutdown()
    #     except BluetoothError:
    #         print("Disconnected")
    #         return False

    # def readUntilShutdown(self):
    #     while True:
    #         data = self.bluetoothConnection.readData(1024)
    #         self.bufferedData.append(data)
    #         commands = self.bufferedData.flush()
    #         for command in commands:
    #             print(command)
    #             if command == "shutdown":
    #                 return True
    #             self.processCommand(command)

    # def processCommand(self, command):
    #     splitCommand = command.split(' ')
    #     if command == "joints":
    #         self.sendJoints()
    #     elif command == "grab":
    #         self.sendGrab()
    #     elif command == "kinematics":
    #         self.sendKinematics()
    #     elif self.isNavigationCommand(command):
    #         pass  # TODO: Implement movement.
    #     elif splitCommand[0] == "angle":
    #         self.changeAngle(splitCommand)
    #     elif splitCommand[0] == "coordinate":
    #         self.changeCoordinates(splitCommand)
    #     else:
    #         self.sendUnknownCommand()

    # def sendJoints(self):
    #     for index in range(self.servoNumber - 1):
    #         self.bluetoothConnection.writeData(self.getJointAngleCommand(index))
    #     self.sendGrab()

    # def getJointAngleCommand(self, index):
    #     angle = round(self.getAngle(index))
    #     return "angle " + str(index) + " " + str(angle)

    def getAngle(self, servoIndex):
        angle = self.servoKit.servo[servoIndex].angle
        return self.correctAngle(angle, servoIndex)

    def correctAngle(self, angle, index):
        return angle if index % 2 != 0 else 180 - angle

    # def sendGrab(self):
    #     self.bluetoothConnection.writeData(self.getGrabAngleCommand())

    # def getGrabAngleCommand(self):
    #     servoIndex = self.servoNumber - 1
    #     angle = round(self.getAngle(servoIndex))
    #     return "angle grab " + str(angle)

    # def sendKinematics(self):
    #     coordinates = self.kinematics.getRoundedKinematics()
    #     x = coordinates[0][0]
    #     y = coordinates[1][0]
    #     phi = coordinates[2][0]
    #     self.bluetoothConnection.writeData(self.getCoordinateCommand("x", x))
    #     self.bluetoothConnection.writeData(self.getCoordinateCommand("y", y))
    #     self.bluetoothConnection.writeData(self.getCoordinateCommand("phi", phi))

    # def getCoordinateCommand(self, coordinate, value):
    #     return "coordinate " + coordinate + " " + str(value)

    # def isNavigationCommand(self, command):
    #     navigationCommands = ["forward", "back", "left", "right"]
    #     return command in navigationCommands

    def changeAngle(self, servoIndex, angle):
        if 0 <= angle <= 180:
            self.servoKit.servo[servoIndex].angle = self.correctAngle(angle, servoIndex)
            if servoIndex < self.servoNumber - 1:  # Prevents updating kinematics for grab servo.
                self.updateKinematics()

    def updateKinematics(self):
        self.kinematics.setConfiguration(q1=self.getAngle(0), q2=self.getAngle(1), q3=self.getAngle(2))

    def changeCoordinates(self, coordinate, value):
        if coordinate == "x":
            self.kinematics.changeXPosition(value)
        elif coordinate == "y":
            self.kinematics.changeYPosition(value)
        else:
            self.kinematics.changePhiAngle(value)
        self.updateAngles()

    def updateAngles(self):
        # TODO: Check if it needs to be rounded.
        configuration = self.kinematics.getRoundedConfiguration()
        for i in range(3):
            self.servoKit.servo[i].angle = self.correctAngle(configuration[i][0], i)

    # def sendUnknownCommand(self):
    #     self.bluetoothConnection.writeData("Unknown command")
