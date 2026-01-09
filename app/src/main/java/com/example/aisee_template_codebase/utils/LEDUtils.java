package com.example.aisee_template_codebase.utils;

import java.io.IOException;

// We need to import the ShellOperator to run commands with root privileges.
import com.example.aisee_template_codebase.utils.ShellOperator;

public class LEDUtils {

    public static final int BACK_BLUE = 0;
    public static final int BACK_RED = 1;
    public static final int FRONT = 2;

    public static void setled(int color, boolean onoff) {
        String ledBrightnessFile = "/sys/class/leds/red/brightness";
        String ledTriggerFile = "/sys/class/leds/red/trigger";

        if (color == BACK_RED) {
            ledBrightnessFile = "/sys/class/leds/green/brightness";
            ledTriggerFile = "/sys/class/leds/green/trigger";
        } else if (color == FRONT) {
            ledBrightnessFile = "/sys/class/leds/blue/brightness";
            ledTriggerFile = "/sys/class/leds/blue/trigger";
        }

        // Disable any active trigger to allow direct brightness control.
        writeFile(ledTriggerFile, "none");

        // Set the brightness directly.
        writeFile(ledBrightnessFile, onoff ? "255" : "0");
    }

    public static void setled(int color, int ontime, int offtime, boolean onoff) {

        String ledtri = "/sys/class/leds/red/trigger";
        String ledontime = "/sys/class/leds/red/delay_on";
        String ledofftime = "/sys/class/leds/red/delay_off";

        if (color == BACK_RED) {
            ledtri = "/sys/class/leds/green/trigger";
            ledontime = "/sys/class/leds/green/delay_on";
            ledofftime = "/sys/class/leds/green/delay_off";
        } else if (color == FRONT) {
            ledtri = "/sys/class/leds/blue/trigger";
            ledontime = "/sys/class/leds/blue/delay_on";
            ledofftime = "/sys/class/leds/blue/delay_off";
        }

        if (onoff == false) {
            writeFile(ledtri, "none"); // To turn off timer, set trigger back to none
            return;
        }

        writeFile(ledtri, "timer");
        writeFile(ledontime, String.valueOf(ontime));
        writeFile(ledofftime, String.valueOf(offtime));
    }

    /**
     * THE FIX: This function now uses ShellOperator to execute commands as root.
     * Standard Java FileWriter does not have permission to write to /sys/.
     */
    private static void writeFile(String path, String content) {
        String command = "echo " + content + " > " + path;
        // Correct way to call a method on a Kotlin object from Java
        ShellOperator.INSTANCE.runCommand(command);
    }
}
