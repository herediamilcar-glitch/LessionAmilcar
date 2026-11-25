package com.example.lession.ble

class BleStreamParser(
    private val onReadingParsed: (EmgReading) -> Unit,
) {
    private val buffer = StringBuilder()

    fun parseChunk(data: ByteArray) {
        val text = String(data, Charsets.UTF_8)
        buffer.append(text)

        while (true) {
            val lfIndex = buffer.indexOf('\n')
            if (lfIndex == -1) break

            val line = buffer.substring(0, lfIndex)
            buffer.delete(0, lfIndex + 1)

            parseLine(line)
        }
    }

    private fun parseLine(line: String) {
        val cleanLine = line.replace("\r", "").trim()
        if (cleanLine.isEmpty()) return

        val parts = cleanLine.split(",")
        if (parts.size >= 2) {
            val rawTime = parts[0].replace(Regex("[^0-9]"), "")
            val rawAdc = parts[1].replace(Regex("[^0-9]"), "")

            val tMs = rawTime.toLongOrNull()
            val adc = rawAdc.toIntOrNull()

            if (tMs != null && adc != null && adc in 0..4095) {
                onReadingParsed(
                    EmgReading(
                        timestampMs = tMs,
                        adcValue = adc,
                    )
                )
            }
        }
    }

    fun clear() {
        buffer.clear()
    }
}
