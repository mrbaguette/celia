@file:JvmName("Converter")
package io.konduct.celia.util


fun fromTenthsToSeconds(tenths: Int) : String {
    return if (tenths < 60 * 10) {
        String.format("%.1f", tenths / 10.0)
    } else {
        val hours = (tenths / 10) / (60 * 60)
        val minutes = (tenths / 10) % (60 * 60) / 60
        val seconds = (tenths / 10) % (60 * 60) % 60
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    }
}
