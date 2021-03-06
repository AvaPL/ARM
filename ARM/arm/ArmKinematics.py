import numpy as np

from math import *

from exceptions.NotReachableException import NotReachableException


class ArmKinematics:
    XI = 0.9
    MAX_INVERSE_KINEMATICS_ITERATIONS = 200
    MAX_INVERSE_KINEMATICS_ERROR_POSITION = 1
    MAX_INVERSE_KINEMATICS_ERROR_ANGLE = radians(1)

    def __init__(self, q1, q2, q3):
        self.q1 = radians(q1)
        self.q2 = radians(q2)
        self.q3 = radians(q3)
        len1 = 66
        len2 = 22
        self.theta = atan(len1 / len2)
        self.a = sqrt(len1 ** 2 + len2 ** 2)
        self.lenE = 147

    def setConfiguration(self, q1, q2, q3):
        self.q1 = radians(q1)
        self.q2 = radians(q2)
        self.q3 = radians(q3)

    def changeXPosition(self, x):
        configuration = self.getConfiguration()
        y = self.getY(configuration)
        phi = self.q3
        self.changeCoordinates(x, y, phi)

    def changeYPosition(self, y):
        configuration = self.getConfiguration()
        x = self.getX(configuration)
        phi = self.q3
        self.changeCoordinates(x, y, phi)

    def changePhiAngle(self, phi):
        configuration = self.getConfiguration()
        x = self.getX(configuration)
        y = self.getY(configuration)
        phi = radians(phi)
        self.changeCoordinates(x, y, phi)

    def changeCoordinates(self, x, y, phi):
        xf = np.array([[x], [y], [phi]])
        iterations = 0
        configuration = self.getConfiguration()
        k = self.getKinematics(configuration)
        while self.shouldCalculateInverseKinematicsAgain(k, xf, iterations):
            invJ = self.getJacobianInverse(configuration)
            configuration = configuration + self.XI * (invJ.dot(xf - k))
            k = self.getKinematics(configuration)
            iterations = iterations + 1
        self.updateConfiguration(configuration)

    def shouldCalculateInverseKinematicsAgain(self, k, xf, iterations):
        if iterations > self.MAX_INVERSE_KINEMATICS_ITERATIONS:
            raise NotReachableException("Coordinates not reachable")
        return not self.isErrorTolerable(k, xf)

    def isErrorTolerable(self, k, xf):
        error = np.abs(xf - k)
        xError = error[0][0]
        yError = error[1][0]
        phiError = error[2][0]
        return xError < self.MAX_INVERSE_KINEMATICS_ERROR_POSITION and yError < self.MAX_INVERSE_KINEMATICS_ERROR_POSITION and phiError < self.MAX_INVERSE_KINEMATICS_ERROR_ANGLE

    def getConfiguration(self):
        return np.array([[self.q1], [self.q2], [self.q3]])

    def getJacobianInverse(self, configuration):
        q1 = configuration[0][0]
        q2 = configuration[1][0]
        q3 = configuration[2][0]
        return np.array([[(self.a * sin(q1 + q2 - self.theta) - self.lenE * cos(q1 + q2 + q3)) / (
                sin(q2) * self.a ** 2 - self.lenE * cos(q2 + q3 + self.theta) * self.a),
                          (self.a * cos(q1 + q2 - self.theta) + self.lenE * sin(q1 + q2 + q3)) / (
                                  sin(q2) * self.a ** 2 - self.lenE * cos(q2 + q3 + self.theta) * self.a),
                          -(self.lenE * cos(q3 + self.theta)) / (
                                  self.a * sin(q2) - self.lenE * cos(q2 + q3 + self.theta))], [-(
                self.a * sin(q1 + q2 - self.theta) + self.a * sin(q1 - self.theta) - self.lenE * cos(
            q1 + q2 + q3)) / (sin(q2) * self.a ** 2 - self.lenE * cos(q2 + q3 + self.theta) * self.a), -(
                self.a * cos(q1 + q2 - self.theta) + self.a * cos(q1 - self.theta) + self.lenE * sin(
            q1 + q2 + q3)) / (sin(q2) * self.a ** 2 - self.lenE * cos(q2 + q3 + self.theta) * self.a), (
                                                                                                       self.lenE * (
                                                                                                       cos(
                                                                                                           q2 + q3 + self.theta) + cos(
                                                                                                   q3 + self.theta))) / (
                                                                                                       self.a * sin(
                                                                                                   q2) - self.lenE * cos(
                                                                                                   q2 + q3 + self.theta))],
                         [0, 0, 1]])

    def getKinematics(self, configuration):
        x = self.getX(configuration)
        y = self.getY(configuration)
        phi = configuration[2][0]
        return np.array([[x], [y], [phi]])

    def getX(self, configuration):
        q1 = configuration[0][0]
        q2 = configuration[1][0]
        q3 = configuration[2][0]
        return self.a * sin(q1 + q2 - self.theta) + self.a * sin(q1 - self.theta) - self.lenE * cos(q1 + q2 + q3)

    def getY(self, configuration):
        q1 = configuration[0][0]
        q2 = configuration[1][0]
        q3 = configuration[2][0]
        return self.a * cos(q1 + q2 - self.theta) + self.a * cos(q1 - self.theta) + self.lenE * sin(q1 + q2 + q3)

    def updateConfiguration(self, configuration):
        q1 = configuration[0][0]
        q2 = configuration[1][0]
        q3 = configuration[2][0]
        if 0 <= q1 <= pi and 0 <= q2 <= pi and 0 <= q3 <= pi:
            self.q1 = q1
            self.q2 = q2
            self.q3 = q3
        else:
            raise NotReachableException("Coordinates not reachable")

    def getCorrectedKinematics(self):
        configuration = self.getConfiguration()
        x = self.getX(configuration)
        y = self.getY(configuration)
        phi = degrees(self.q3) % 360
        return np.array([[x], [y], [phi]])

    def getCorrectedConfiguration(self):
        q1 = degrees(self.q1) % 360
        q2 = degrees(self.q2) % 360
        q3 = degrees(self.q3) % 360
        return np.array([[q1], [q2], [q3]])
