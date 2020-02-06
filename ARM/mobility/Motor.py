import RPi.GPIO as GPIO
from enum import IntEnum


class Motor:
    PWM_FREQUENCY = 10000

    class Direction(IntEnum):
        FORWARD = 0
        BACKWARD = 1

    def __init__(self, PWMPin, directionPin):
        GPIO.setup(PWMPin, GPIO.OUT)
        self.PWM = GPIO.PWM(PWMPin, self.PWM_FREQUENCY)
        self.PWM.start(0)
        GPIO.setup(directionPin, GPIO.OUT)
        self.directionPin = directionPin

    def setSpeed(self, speed, direction):
        GPIO.output(self.directionPin, direction)
        self.PWM.ChangeDutyCycle(speed)

    def stop(self):
        self.PWM.ChangeDutyCycle(0)
