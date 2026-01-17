package org.aisee.template_codebase.internal_utils;

// We need to import the ShellOperator to run commands with root privileges.


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
     * Control the vibration motor with specified duration and intensity.
     * 
     * @param durationMs Duration of vibration in milliseconds (0 to stop vibration)
     * @param intensity Vibration intensity (0-255, where 255 is maximum)
     */
    public static void setVibration(int durationMs, int intensity) {
        String vibratorDurationFile = "/sys/class/leds/vibrator/duration";
        String vibratorBrightnessFile = "/sys/class/leds/vibrator/brightness";
        String vibratorActivateFile = "/sys/class/leds/vibrator/activate";

        if (durationMs <= 0 || intensity <= 0) {
            // Stop vibration
            writeFile(vibratorDurationFile, "0");
            writeFile(vibratorActivateFile, "0");
            return;
        }

        // Clamp intensity to valid range (0-255)
        intensity = Math.max(0, Math.min(255, intensity));

        // Set duration, brightness, then activate
        writeFile(vibratorDurationFile, String.valueOf(durationMs));
        writeFile(vibratorBrightnessFile, String.valueOf(intensity));
        writeFile(vibratorActivateFile, "1");
    }

    /**
     * Trigger vibration with maximum intensity for specified duration.
     * 
     * @param durationMs Duration of vibration in milliseconds
     */
    public static void setVibration(int durationMs) {
        setVibration(durationMs, 255); // Use maximum intensity
    }

    /**
     * Stop the vibration motor immediately.
     */
    public static void stopVibration() {
        setVibration(0, 0);
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
