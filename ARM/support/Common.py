from time import sleep

import RPi.GPIO as GPIO


def isButtonHeld(pin, bouncetimeMillis, holdtimeMillis):
    GPIO.wait_for_edge(pin, GPIO.FALLING)
    sleep(bouncetimeMillis / 1000)  # Debounce
    if GPIO.input(pin) == GPIO.LOW:
        edgeDetected = GPIO.wait_for_edge(pin, GPIO.RISING, timeout=holdtimeMillis - bouncetimeMillis)
        if edgeDetected is None:
            return True
    return False


def blinkLed(pin, onTimeMillis, offTimeMillis):
    GPIO.output(pin, GPIO.HIGH)
    sleep(onTimeMillis / 1000)
    GPIO.output(pin, GPIO.LOW)
    sleep(offTimeMillis / 1000)
