from connection.BluetoothConnection import BluetoothConnection
from Arm import Arm
from CommandInterpreter import CommandInterpreter
import RPi.GPIO as GPIO
import subprocess

bluetoothConnection = BluetoothConnection(port=1)

servoNumber = 4
pulseWidths = [[500, 2600], [500, 2600], [500, 2600], [1800, 2600]]
initialAngles = [160, 110, 150, 90]
# arm = Arm(bluetoothConnection, servoNumber, pulseWidths, initialAngles)
arm = Arm(servoNumber, pulseWidths, initialAngles)

mobilePlatform = None

commandInterpreter = CommandInterpreter(bluetoothConnection, arm, mobilePlatform)

def shutdown():
    bluetoothConnection.close()
    GPIO.cleanup()
    # subprocess.call("sudo shutdown -h now", shell=True) TODO: Uncomment, debug only.


try:
    shutdownReceived = False
    while not shutdownReceived:  # establish a new connection if the client disconnected
        bluetoothConnection.establish()
        shutdownReceived = commandInterpreter.readCommands()
except:
    pass
finally:
    shutdown()
