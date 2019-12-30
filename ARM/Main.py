from connection.BluetoothConnection import BluetoothConnection
from Arm import Arm
import RPi.GPIO as GPIO
import subprocess

bluetoothConnection = BluetoothConnection(port=1)

servoNumber = 4
pulseWidths = [[800, 2200], [800, 2200], [800, 2600], [1600, 2400]]
initialAngles = [160, 110, 150, 90]
arm = Arm(bluetoothConnection, servoNumber, pulseWidths, initialAngles)


def shutdown():
    bluetoothConnection.close()
    GPIO.cleanup()
    subprocess.call("sudo shutdown -h now", shell=True)


try:
    shutdownReceived = False
    while not shutdownReceived:  # establish a new connection if the client disconnected
        bluetoothConnection.establish()
        shutdownReceived = arm.readCommands()
except:
    pass
finally:
    shutdown()
