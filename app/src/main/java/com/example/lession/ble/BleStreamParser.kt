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

            val line = buffer.substring(0, lfIndex).trim()
            buffer.delete(0, lfIndex + 1)

            if (line.isNotEmpty()) {
                parseLine(line)
            }
        }
    }

    private fun parseLine(line: String) {
        val parts = line.split(",")
        if (parts.size == 2) {
            val tMs = parts[0].trim().toLongOrNull()
            val adc = parts[1].trim().toIntOrNull()
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
