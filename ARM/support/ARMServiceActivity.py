from threading import Thread
from time import sleep

import RPi.GPIO as GPIO
import subprocess

GPIO.setmode(GPIO.BCM)
restartServicePin = 26
GPIO.setup(restartServicePin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
activityLedPin = 16
GPIO.setup(activityLedPin, GPIO.OUT)


def restartService():
    while True:
        GPIO.wait_for_edge(restartServicePin, GPIO.FALLING)
        sleep(0.2)  # Debounce
        if GPIO.input(restartServicePin) == GPIO.LOW:
            edgeDetected = GPIO.wait_for_edge(restartServicePin, GPIO.RISING, timeout=1800)
            if edgeDetected is None:
                callRestartSubprocess()


def callRestartSubprocess():
    for i in range(5):
        blinkActivityLed(onTime=0.1, offTime=0.1)
    subprocess.call("sudo systemctl restart arm.service", shell=True)


def blinkActivityLed(onTime, offTime):
    GPIO.output(activityLedPin, GPIO.HIGH)
    sleep(onTime)
    GPIO.output(activityLedPin, GPIO.LOW)
    sleep(offTime)


restartServiceThread = Thread(target=restartService, daemon=True)
restartServiceThread.start()

try:
    while True:
        serviceStatus = subprocess.call("systemctl is-active --quiet arm.service", shell=True)
        if serviceStatus == 0:
            blinkActivityLed(onTime=0.2, offTime=2.8)
finally:
    GPIO.cleanup()
