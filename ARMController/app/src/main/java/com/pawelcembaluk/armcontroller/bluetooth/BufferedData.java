package com.pawelcembaluk.armcontroller.bluetooth;

import java.util.Arrays;

public class BufferedData {

    private final StringBuilder buffer = new StringBuilder();

    public void append(String string) {
        buffer.append(string);
    }

    public String[] flush() {
        String bufferedString = buffer.toString();
        if (endsWithNewLine(bufferedString))
            return flushAllData();
        else
            return flushOnlyFullLines();
    }

    private boolean endsWithNewLine(String dataString) {
        return dataString.matches("(?s).*\\R$");
    }

    private String[] flushAllData() {
        String[] dataStrings = splitLines(buffer.toString());
        cleanBuffer();
        return dataStrings;
    }

    private String[] splitLines(String string) {
        return string.split("\\R");
    }

    private void cleanBuffer() {
        buffer.setLength(0);
    }

    private String[] flushOnlyFullLines() {
        String[] dataStrings = splitLines(buffer.toString());
        if (dataStrings.length < 2)
            return new String[0]; //No lines or only one line that doesn't end with \R.
        cleanBuffer();
        String lastElement = dataStrings[dataStrings.length - 1];
        buffer.append(lastElement);
        return Arrays.copyOfRange(dataStrings, 0, dataStrings.length - 1);
    }
}
