from BluetoothConnection import BluetoothConnection
from Arm import Arm

bluetoothConnection = BluetoothConnection(port=1)
servoNumber = 4
pulseWidths = [[800, 2200], [800, 2200], [800, 2200], [1600, 2200]]
arm = Arm(bluetoothConnection, servoNumber, pulseWidths)

try:
    exitRead = False
    while not exitRead:  # establish a new connection if the client disconnected
        bluetoothConnection.establish()
        exitRead = arm.readCommands()
except KeyboardInterrupt:
    pass
finally:
    bluetoothConnection.close()
