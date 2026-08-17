package com.example.voltasirtest

class VoltasEncoder {

    companion object {
        const val CARRIER_HZ = 38_000

        private const val BIT_MARK = 1026
        private const val ONE_SPACE = 2553
        private const val ZERO_SPACE = 554

        private const val MIN_TEMP = 16
        private const val MAX_TEMP = 30

        private const val MODE_FAN = 0b0001
        private const val MODE_DRY = 0b0100
        private const val MODE_COOL = 0b1000

        private const val FAN_HIGH = 0b001
        private const val FAN_MED = 0b010
        private const val FAN_LOW = 0b100
        private const val FAN_AUTO = 0b111
    }

    // Validated base state:
    // OFF · COOL · 24°C · AUTO fan · Lamp ON · V-Swing OFF · Turbo OFF.
    private val state = byteArrayOf(
        0x33, 0xE8.toByte(), 0x00, 0x18, 0x3B,
        0x3B, 0x3B, 0x11, 0x00, 0x0A
    )

    fun powerOn(): IntArray {
        setPower(true)
        updateChecksum()
        return toPattern()
    }

    fun powerOff(): IntArray {
        setPower(false)
        updateChecksum()
        return toPattern()
    }

    fun setTemperatureCommand(tempC: Int): IntArray {
        setTemperature(tempC)
        updateChecksum()
        return toPattern()
    }

    fun modeCool(): IntArray {
        setMode(MODE_COOL)
        updateChecksum()
        return toPattern()
    }

    fun modeDry(): IntArray {
        setMode(MODE_DRY)
        setTemperature(24)
        setFan(FAN_LOW)

        updateChecksum()
        return toPattern()
    }

    fun modeFan(): IntArray {
        setMode(MODE_FAN)
        setFan(FAN_HIGH)

        updateChecksum()
        return toPattern()
    }

    fun fanAuto(): IntArray {
        setFan(FAN_AUTO)
        updateChecksum()
        return toPattern()
    }

    fun fanLow(): IntArray {
        setFan(FAN_LOW)
        updateChecksum()
        return toPattern()
    }

    fun fanMedium(): IntArray {
        setFan(FAN_MED)
        updateChecksum()
        return toPattern()
    }

    fun fanHigh(): IntArray {
        setFan(FAN_HIGH)
        updateChecksum()
        return toPattern()
    }

    fun lampOn(): IntArray {
        val b = state[8].toInt() and 0xFF

        // Validated model-specific mapping:
        // bit 5 = 0 => indoor display/lamp ON.
        state[8] = (b and 0xDF).toByte()

        updateChecksum()
        return toPattern()
    }

    fun lampOff(): IntArray {
        val b = state[8].toInt() and 0xFF

        // Validated model-specific mapping:
        // bit 5 = 1 => indoor display/lamp OFF.
        state[8] = (b or 0x20).toByte()

        updateChecksum()
        return toPattern()
    }

    fun verticalSwingOn(): IntArray {
        val b = state[2].toInt() and 0xFF
        state[2] = ((b and 0xF8) or 0x07).toByte()

        updateChecksum()
        return toPattern()
    }

    fun verticalSwingOff(): IntArray {
        val b = state[2].toInt() and 0xFF
        state[2] = (b and 0xF8).toByte()

        updateChecksum()
        return toPattern()
    }

    fun turboOn(): IntArray {
        setMode(MODE_COOL)

        val b = state[2].toInt() and 0xFF
        state[2] = (b or 0x20).toByte()

        updateChecksum()
        return toPattern()
    }

    fun turboOff(): IntArray {
        val b = state[2].toInt() and 0xFF
        state[2] = (b and 0xDF).toByte()

        updateChecksum()
        return toPattern()
    }

    fun timerOnHours(hours: Int): IntArray {
        val safeHours = hours.coerceIn(1, 15)
        setOnTimer(safeHours)

        updateChecksum()
        return toPattern()
    }

    fun timerOffHours(hours: Int): IntArray {
        val safeHours = hours.coerceIn(1, 15)
        setOffTimer(safeHours)

        updateChecksum()
        return toPattern()
    }

    fun cancelOnTimer(): IntArray {
        setOnTimer(0)
        updateChecksum()
        return toPattern()
    }

    fun cancelOffTimer(): IntArray {
        setOffTimer(0)
        updateChecksum()
        return toPattern()
    }

    fun rawStateHex(): String =
        state.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }

    private fun setPower(on: Boolean) {
        val b = state[2].toInt() and 0xFF

        state[2] =
            if (on) (b or 0x80).toByte()
            else (b and 0x7F).toByte()
    }

    private fun setMode(mode: Int) {
        val b = state[1].toInt() and 0xFF
        state[1] = ((b and 0xF0) or (mode and 0x0F)).toByte()
    }

    private fun setFan(fan: Int) {
        val b = state[1].toInt() and 0xFF
        state[1] = ((b and 0x1F) or ((fan and 0x07) shl 5)).toByte()
    }

    private fun setTemperature(tempC: Int) {
        val t = tempC.coerceIn(MIN_TEMP, MAX_TEMP) - MIN_TEMP
        val b = state[3].toInt() and 0xFF
        state[3] = ((b and 0xF0) or (t and 0x0F)).toByte()
    }

    private fun setOnTimer(hours: Int) {
        if (hours == 0) {
            // Disable ON timer: byte 8 bit 7.
            state[8] = ((state[8].toInt() and 0xFF) and 0x7F).toByte()
            state[4] = 0x3B
            return
        }

        state[4] = 0x3B

        // Byte 7 low nibble = ON timer hours.
        val byte7 = state[7].toInt() and 0xFF
        state[7] = ((byte7 and 0xF0) or (hours and 0x0F)).toByte()

        // Byte 8 bit 7 = ON timer enabled.
        state[8] = ((state[8].toInt() and 0xFF) or 0x80).toByte()
    }

    private fun setOffTimer(hours: Int) {
        if (hours == 0) {
            // Disable OFF timer: byte 8 bit 6.
            state[8] = ((state[8].toInt() and 0xFF) and 0xBF).toByte()
            state[5] = 0x3B
            return
        }

        state[5] = 0x3B

        // Byte 7 high nibble = OFF timer hours.
        val byte7 = state[7].toInt() and 0xFF
        state[7] = ((byte7 and 0x0F) or ((hours and 0x0F) shl 4)).toByte()

        // Byte 8 bit 6 = OFF timer enabled.
        state[8] = ((state[8].toInt() and 0xFF) or 0x40).toByte()
    }

    private fun updateChecksum() {
        var sum = 0

        for (i in 0 until state.size - 1) {
            sum = (sum + (state[i].toInt() and 0xFF)) and 0xFF
        }

        state[state.lastIndex] = (sum.inv() and 0xFF).toByte()
    }

    private fun toPattern(): IntArray {
        val pattern = ArrayList<Int>(state.size * 16 + 1)

        for (byte in state) {
            val value = byte.toInt() and 0xFF

            for (bit in 7 downTo 0) {
                pattern.add(BIT_MARK)
                pattern.add(
                    if (((value shr bit) and 1) == 1) ONE_SPACE else ZERO_SPACE
                )
            }
        }

        pattern.add(BIT_MARK)
        return pattern.toIntArray()
    }
}
