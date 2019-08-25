from BluetoothConnection import BluetoothConnection
from Arm import Arm

bluetoothConnection = BluetoothConnection(port=1)
servoNumber = 4
pulseWidths = [[800, 2200], [800, 2200], [800, 2200], [1600, 2200]]
angles = [135, 50, 110, 90]
arm = Arm(bluetoothConnection, servoNumber, pulseWidths, angles)

try:
    exitRead = False
    while not exitRead:  # establish a new connection if the client disconnected
        bluetoothConnection.establish()
        exitRead = arm.readCommands()
except KeyboardInterrupt:
    pass
finally:
    bluetoothConnection.close()
