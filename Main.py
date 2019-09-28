from BluetoothConnection import BluetoothConnection
from Arm import Arm
import RPi.GPIO as GPIO
import subprocess

bluetoothConnection = BluetoothConnection(port=1)

servoNumber = 4
pulseWidths = [[800, 2200], [800, 2200], [800, 2200], [1600, 2400]]
initialAngles = [160, 110, 150, 90]
arm = Arm(bluetoothConnection, servoNumber, pulseWidths, initialAngles)

GPIO.setmode(GPIO.BCM)
shutdownPin = 21
GPIO.setup(shutdownPin, GPIO.IN, pull_up_down=GPIO.PUD_UP)


def shutdown():
    bluetoothConnection.close()
    GPIO.cleanup()
    subprocess.call("sudo shutdown -h now", shell=True)


def buttonShutdown(channel):
    if channel != shutdownPin:
        return
    shutdown()


GPIO.add_event_detect(shutdownPin, GPIO.FALLING, callback=buttonShutdown)

try:
    shutdownReceived = False
    while not shutdownReceived:  # establish a new connection if the client disconnected
        bluetoothConnection.establish()
        shutdownReceived = arm.readCommands()
except:
    pass
finally:
    shutdown()
