import RPi.GPIO as GPIO
import subprocess

from connection.BluetoothConnection import BluetoothConnection
from arm.Arm import Arm
from mobility.Motor import Motor
from mobility.MobilePlatform import MobilePlatform
from CommandInterpreter import CommandInterpreter

bluetoothConnection = BluetoothConnection(port=1)

servoNumber = 4
pulseWidths = [[500, 2600], [500, 2600], [500, 2600], [1800, 2600]]
initialAngles = [160, 110, 150, 90]
arm = Arm(servoNumber, pulseWidths, initialAngles)

GPIO.setmode(GPIO.BCM)
leftMotorPWMPin = 12
leftMotorDirectionPin = 5
rightMotorPWMPin = 13
rightMotorDirectionPin = 6
leftMotor = Motor(leftMotorPWMPin, leftMotorDirectionPin)
rightMotor = Motor(rightMotorPWMPin, rightMotorDirectionPin)
mobilePlatform = MobilePlatform(leftMotor, rightMotor)

commandInterpreter = CommandInterpreter(bluetoothConnection, arm, mobilePlatform)

def shutdown():
    bluetoothConnection.close()
    mobilePlatform.stop()
    GPIO.cleanup()
    # subprocess.call("sudo shutdown -h now", shell=True) TODO: Uncomment, debug only.


try:
    shutdownReceived = False
    while not shutdownReceived: # Establish a new connection if the client disconnected.
        bluetoothConnection.establish()
        shutdownReceived = commandInterpreter.readCommands()
except:
    pass
finally:
    shutdown() # TODO: System shouldn't be always shut down eg. service restart.
