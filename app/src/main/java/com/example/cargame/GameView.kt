import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.cargame.GameTask
import com.example.cargame.R
import java.util.*
import kotlin.collections.ArrayList

class GameView(c: Context, var gameTask: GameTask) : View(c) {
    private var myPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var speed = 1
    private var time = 0
    private var score = 0
    private var myCarPosition = 0
    private val otherCars = ArrayList<HashMap<String, Any>>()

    private var viewWidth = 0
    private var viewHeight = 0
    private var carWidth = 0
    private var carHeight = 0

    init {
        myPaint.textSize = 40f
        myPaint.color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (viewWidth == 0 || viewHeight == 0) {
            viewWidth = width
            viewHeight = height
            carWidth = viewWidth / 5
            carHeight = carWidth * 1.5.toInt()  // Assuming cars are 1.5 times as tall as they are wide
        }

        // Handle new cars appearing
        if (time % 700 < 10 + speed) {
            val map = HashMap<String, Any>()
            map["lane"] = (0..2).random()
            map["startTime"] = time
            otherCars.add(map)
        }

        // Increase time for movement and speed adjustments
        time += 10 + speed

        // Draw player's car
        drawCar(canvas, myCarPosition, resources.getDrawable(R.drawable.red, null))

        // Handle other cars
        val iterator = otherCars.iterator()
        while (iterator.hasNext()) {
            val car = iterator.next()
            val lane = car["lane"] as Int
            var carY = time - car["startTime"] as Int

            if (carY > viewHeight) {
                iterator.remove()
                score++
                speed = 1 + score / 8
            } else {
                drawCar(canvas, lane, resources.getDrawable(R.drawable.yellow, null), carY)

                // Collision detection
                if (lane == myCarPosition && carY in viewHeight - 2 * carHeight until viewHeight) {
                    gameTask.closeGame(score)
                }
            }
        }

        // Display score and speed
        canvas.drawText("Score: $score", 80f, 80f, myPaint)
        canvas.drawText("Speed: $speed", 80f, 120f, myPaint)

        // Force a redraw
        invalidate()
    }

    private fun drawCar(canvas: Canvas, lane: Int, drawable: Drawable, yPos: Int = viewHeight - carHeight) {
        drawable.setBounds(
            lane * viewWidth / 3 + viewWidth / 15,
            yPos - carHeight,
            lane * viewWidth / 3 + viewWidth / 15 + carWidth,
            yPos
        )
        drawable.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x1 = event.x
                if (x1 < viewWidth / 2 && myCarPosition > 0) {
                    myCarPosition--
                } else if (x1 > viewWidth / 2 && myCarPosition < 2) {
                    myCarPosition++
                }
                invalidate()
            }
        }
        return true
    }
}
