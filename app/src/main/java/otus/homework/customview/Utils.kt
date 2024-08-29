package otus.homework.customview

import android.content.res.Resources
import android.os.Build
import android.util.TypedValue

val Float.pxToDp: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

val Int.pxToDp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.pxToSp: Float
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        this * TypedValue.deriveDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            Resources.getSystem().displayMetrics
        )
    } else {
        this * Resources.getSystem().displayMetrics.scaledDensity
    }
