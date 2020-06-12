package com.example.i3remote;

class CommandGenerator {
    static Command generateCommand(String instruction) {
        return new Command("DISPLAY=:0 " + instruction);
    }

    static Command generatei3Command(String instruction) {
        return generateCommand("i3-msg \"" + instruction + "\"");
    }
}
