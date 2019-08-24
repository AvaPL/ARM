from adafruit_servokit import ServoKit
from serial import Serial
import os
import sys
import time

servoKit = ServoKit(channels=16)
servoKit.servo[0].set_pulse_width_range(800, 2200)
servoKit.servo[1].set_pulse_width_range(800, 2200)
servoKit.servo[2].set_pulse_width_range(800, 2200)
servoKit.servo[3].set_pulse_width_range(1600, 2200)

servoKit.servo[0].angle = 135;
servoKit.servo[1].angle = 50;
servoKit.servo[2].angle = 110;
servoKit.servo[3].angle = 90;

exit = False

def processCommand(command):
    print(command)
    splitCommand = command.split(' ')
    if splitCommand[0] == "angle":
        servoIndex = int(splitCommand[1])
        angle = getNewAngle(servoIndex, splitCommand[2])
        changeAngle(servoIndex, angle)
    elif splitCommand[0] == "exit\n":
        global exit
        exit = True


def getNewAngle(servoIndex, sign):
    angle = servoKit.servo[servoIndex].angle
    if sign == "+\n":
        angle = angle + 2
    elif sign == "-\n":
        angle = angle - 2
    return angle


def changeAngle(servoIndex, angle):
    servo = servoKit.servo[servoIndex]
    if 0 <= angle <= 180:
        servo.angle = angle


# os.system("sudo rfcomm watch hci0") TODO: Find a better way to connect.
serialLocation = "/dev/rfcomm0"

while not os.path.exists(serialLocation):
    time.sleep(2)

serial = Serial(serialLocation)
while not exit:
    commandBytes = serial.readline()
    command = commandBytes.decode(sys.stdout.encoding)
    processCommand(command)
