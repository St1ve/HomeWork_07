package otus.homework.customview

import android.graphics.Color

fun generateRandomColor(): Int {
    val r = (Math.random() * 256).toInt()
    val g = (Math.random() * 256).toInt()
    val b = (Math.random() * 256).toInt()

    return Color.rgb(r, g, b)
}
