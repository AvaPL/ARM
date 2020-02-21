from threading import Thread

import RPi.GPIO as GPIO
import subprocess

from Common import blinkLed, isButtonHeld

GPIO.setmode(GPIO.BCM)
restartServicePin = 26
GPIO.setup(restartServicePin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
activityLedPin = 16
GPIO.setup(activityLedPin, GPIO.OUT)


def restartService():
    while True:
        if isButtonHeld(restartServicePin, bouncetimeMillis=200, holdtimeMillis=2000):
            callRestartSubprocess()


def callRestartSubprocess():
    for i in range(5):
        blinkLed(activityLedPin, onTimeMillis=100, offTimeMillis=100)
    subprocess.call("sudo systemctl restart arm.service", shell=True)


restartServiceThread = Thread(target=restartService, daemon=True)
restartServiceThread.start()

try:
    while True:
        serviceStatus = subprocess.call("systemctl is-active --quiet arm.service", shell=True)
        if serviceStatus == 0:
            blinkLed(activityLedPin, onTimeMillis=200, offTimeMillis=2800)
finally:
    GPIO.cleanup()
