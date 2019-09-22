from adafruit_servokit import ServoKit
from bluetooth import BluetoothError
import re


class Arm:
    def __init__(self, bluetoothConnection, servoNumber, pulseWidths):
        self.bluetoothConnection = bluetoothConnection
        self.servoNumber = servoNumber
        self.servoKit = ServoKit(channels=16)
        for index in range(servoNumber):
            servo = self.servoKit.servo[index]
            servo.set_pulse_width_range(pulseWidths[index][0], pulseWidths[index][1])
        self.bufferedData = ""

    def readCommands(self):
        # returns True if the arm received an "exit" command
        # and False if the client disconnected
        try:
            return self.readUntilExit()
        except BluetoothError:
            print("Disconnected")
            return False

    def readUntilExit(self):
        while True:
            self.bufferedData += self.bluetoothConnection.readData(1024)
            commands = self.flushBufferedData()
            for command in commands:
                if command == "exit":
                    return True
                self.processCommand(command)

    def flushBufferedData(self):
        return self.flushAllData() if self.endsWithNewLine(self.bufferedData) else self.flushOnlyFullLines()

    def endsWithNewLine(self, string):
        return re.fullmatch(".*[\r\n]+", string, re.S)

    def flushAllData(self):
        dataStrings = self.bufferedData.splitlines()
        self.bufferedData = ""
        return dataStrings

    def flushOnlyFullLines(self):
        dataStrings = self.bufferedData.splitlines()
        self.bufferedData = dataStrings[-1]  # last string
        return dataStrings[:-1]  # everything except last string

    def processCommand(self, command):
        print(command)
        splitCommand = command.split(' ')
        if splitCommand[0] == "angle":
            servoIndex = int(splitCommand[1])
            self.changeAngle(servoIndex, int(splitCommand[2]))
        elif splitCommand[0] == "angles":
            for index in range(self.servoNumber - 1):
                self.bluetoothConnection.writeData(
                    "angle " + str(index) + " " + str(round(self.servoKit.servo[index].angle)) + '\n')
            self.bluetoothConnection.writeData(
                "angle grab " + str(round(self.servoKit.servo[self.servoNumber - 1].angle)) + '\n')
        else:
            self.bluetoothConnection.writeData("Unknown command\n")

    def changeAngle(self, servoIndex, angle):
        if 0 <= angle <= 180:
            self.servoKit.servo[servoIndex].angle = angle
