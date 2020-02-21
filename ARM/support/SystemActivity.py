from threading import Thread

import RPi.GPIO as GPIO
import subprocess

from Common import blinkLed, isButtonHeld

GPIO.setmode(GPIO.BCM)
shutdownPin = 21
GPIO.setup(shutdownPin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
activityLedPin = 19
GPIO.setup(activityLedPin, GPIO.OUT)


def shutdown():
    while True:
        if isButtonHeld(shutdownPin, bouncetimeMillis=200, holdtimeMillis=2000):
            callShutdownSubprocess()


def callShutdownSubprocess():
    for i in range(5):
        blinkLed(activityLedPin, onTimeMillis=100, offTimeMillis=100)
    # GPIO.cleanup()
    # subprocess.call("sudo shutdown -h now", shell=True) TODO: Uncomment, debug only.


shutdownThread = Thread(target=shutdown, daemon=True)
shutdownThread.start()

try:
    while True:
        blinkLed(activityLedPin, onTimeMillis=200, offTimeMillis=2800)
finally:
    GPIO.cleanup()
