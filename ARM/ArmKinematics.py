import numpy as np

from math import *


class ArmKinematics:
    def __init__(self, q1, q2, q3):
        self.q1 = radians(q1)
        self.q2 = radians(q2)
        self.q3 = radians(q3)
        len1 = 66
        len2 = 22
        self.theta = atan(len1 / len2)
        self.a = sqrt(len1 ** 2 + len2 ** 2)
        self.lenE = 147

    def getConfiguration(self):
        return np.array([[self.q1], [self.q2], [self.q3]])

    def setConfiguration(self, q1, q2, q3):
        self.q1 = radians(q1)
        self.q2 = radians(q2)
        self.q3 = radians(q3)

    def getKinematics(self):
        x = self.getX()
        y = self.getY()
        return np.array([[x], [y], [self.q3]])

    def getX(self):
        return self.a * sin(self.q1 + self.q2 - self.theta) + self.a * sin(self.q1 - self.theta) - self.lenE * cos(self.q1 + self.q2 + self.q3)

    def getY(self):
        return self.a * cos(self.q1 + self.q2 - self.theta) + self.a * cos(self.q1 - self.theta) + self.lenE * sin(self.q1 + self.q2 + self.q3)