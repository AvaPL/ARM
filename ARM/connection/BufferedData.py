import re


class BufferedData:
    def __init__(self):
        self.buffer = ""

    def append(self, string):
        self.buffer += string

    def flush(self):
        return self.flushAllData() if self.endsWithNewLine(self.buffer) else self.flushOnlyFullLines()

    def flushAllData(self):
        dataStrings = self.buffer.splitlines()
        self.buffer = ""
        return dataStrings

    def endsWithNewLine(self, string):
        return re.fullmatch(".*[\r\n]+", string, re.S)

    def flushOnlyFullLines(self):
        dataStrings = self.buffer.splitlines()
        self.buffer = dataStrings[-1]  # last string
        return dataStrings[:-1]  # everything except last string
