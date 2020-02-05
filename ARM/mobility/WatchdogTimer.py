from threading import Thread, Event


class WatchdogTimer(Thread):
    def __init__(self, handler, timeoutSeconds):
        super().__init__(daemon=True)
        self.handler = handler
        self.timeoutSeconds = timeoutSeconds
        self.startEvent = Event()
        self.resetEvent = Event()
        self.start()

    def run(self):
        while self.startEvent.wait():
            while self.resetEvent.wait(self.timeoutSeconds):
                self.resetEvent.clear()
            self.handler()
            self.startEvent.clear()

    def restart(self):
        self.startEvent.set()

    def reset(self):
        self.resetEvent.set()
