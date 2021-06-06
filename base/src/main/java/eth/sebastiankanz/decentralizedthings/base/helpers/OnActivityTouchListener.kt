package eth.sebastiankanz.decentralizedthings.base.helpers

import android.view.MotionEvent

interface OnActivityTouchListener {
    fun getTouchCoordinates(ev: MotionEvent)
}