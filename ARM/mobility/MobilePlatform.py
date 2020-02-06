from mobility.WatchdogTimer import WatchdogTimer
from mobility.Motor import Motor

class MobilePlatform:
    WATCHDOG_TIMEOUT_SECONDS = 1

    def __init__(self, leftMotor, rightMotor):
        self.leftMotor = leftMotor
        self.rightMotor = rightMotor
        self.watchdogTimer = WatchdogTimer(self.stop, self.WATCHDOG_TIMEOUT_SECONDS)
        self.speed = 100

    def setSpeed(self, speed):
        self.speed = speed

    def stop(self):
        self.leftMotor.stop()
        self.rightMotor.stop()

    def forward(self):
        self.move(Motor.Direction.FORWARD, Motor.Direction.FORWARD)

    def move(self, leftMotorDirection, rightMotorDirection):
        if self.watchdogTimer.isActive():
            self.watchdogTimer.reset()
        else:
            self.watchdogTimer.restart()
        self.leftMotor.setSpeed(self.speed, leftMotorDirection)
        self.rightMotor.setSpeed(self.speed, rightMotorDirection)

    def back(self):
        self.move(Motor.Direction.BACKWARD, Motor.Direction.BACKWARD)

    def left(self):
        self.move(Motor.Direction.FORWARD, Motor.Direction.BACKWARD)

    def right(self):
        self.move(Motor.Direction.BACKWARD, Motor.Direction.FORWARD)