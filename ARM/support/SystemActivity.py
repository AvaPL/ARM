from threading import Thread
from time import sleep

import RPi.GPIO as GPIO
import subprocess

GPIO.setmode(GPIO.BCM)
shutdownPin = 21
GPIO.setup(shutdownPin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
activityLedPin = 19
GPIO.setup(activityLedPin, GPIO.OUT)


def shutdown():
    while True:
        GPIO.wait_for_edge(shutdownPin, GPIO.FALLING)
        sleep(0.2)  # Debounce
        if GPIO.input(shutdownPin) == GPIO.LOW:
            edgeDetected = GPIO.wait_for_edge(shutdownPin, GPIO.RISING, timeout=1800)
            if edgeDetected is None:
                callShutdownSubprocess()


def callShutdownSubprocess():
    for i in range(5):
        blinkActivityLed(onTime=0.1, offTime=0.1)
    # GPIO.cleanup()
    # subprocess.call("sudo shutdown -h now", shell=True) TODO: Uncomment, debug only.


def blinkActivityLed(onTime, offTime):
    GPIO.output(activityLedPin, GPIO.HIGH)
    sleep(onTime)
    GPIO.output(activityLedPin, GPIO.LOW)
    sleep(offTime)


shutdownThread = Thread(target=shutdown, daemon=True)
shutdownThread.start()

try:
    while True:
        blinkActivityLed(onTime=0.2, offTime=2.8)
finally:
    GPIO.cleanup()
