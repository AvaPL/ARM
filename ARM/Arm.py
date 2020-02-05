from adafruit_servokit import ServoKit

from ArmKinematics import ArmKinematics


class Arm:
    def __init__(self, servoNumber, pulseWidths, initialAngles):
        self.servoNumber = servoNumber
        self.servoKit = ServoKit(channels=16)
        for index in range(servoNumber):
            servo = self.servoKit.servo[index]
            servo.set_pulse_width_range(pulseWidths[index][0], pulseWidths[index][1])
            servo.angle = initialAngles[index]
        self.kinematics = ArmKinematics(q1=self.getAngle(0), q2=self.getAngle(1), q3=self.getAngle(2))

    def getAngle(self, servoIndex):
        angle = self.servoKit.servo[servoIndex].angle
        return self.correctAngle(angle, servoIndex)

    def correctAngle(self, angle, index):
        return angle if index % 2 != 0 else 180 - angle

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
        configuration = self.kinematics.getCorrectedConfiguration()
        for i in range(3):
            self.servoKit.servo[i].angle = self.correctAngle(configuration[i][0], i)
