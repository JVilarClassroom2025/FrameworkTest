package es.uji.jvilar.frameworktest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import es.uji.vj1229.framework.AnimatedBitmap
import es.uji.vj1229.framework.Graphics
import es.uji.vj1229.framework.SpriteSheet

object Assets {
    private const val BALL_DURATION = 0.2f
    private const val BALL_FRAMES = 10
    private const val SPRITE_BALL_SIDE = 128
    private const val RED_BALL_INDEX = 0
    private const val BLUE_BALL_INDEX = 3
    private var ballSprites: Bitmap? = null
    private var balls: SpriteSheet? = null
    lateinit var blueBall: Bitmap
    lateinit var redBall: Bitmap
    lateinit var blueBallAnimated: AnimatedBitmap
    lateinit var redBallAnimated: AnimatedBitmap
    lateinit var reset: Drawable

    fun createAssets(context: Context, ballSide: Int) {
        val resources = context.resources

        ballSprites = BitmapFactory.decodeResource(resources, R.drawable.balls)
        balls = SpriteSheet(ballSprites, SPRITE_BALL_SIDE, SPRITE_BALL_SIDE)
        reset = AppCompatResources.getDrawable(context, R.drawable.reset)!!

        blueBall = balls!!.getScaledSprite(0, BLUE_BALL_INDEX, ballSide, ballSide)
        redBall = balls!!.getScaledSprite(0, RED_BALL_INDEX, ballSide, ballSide)

        blueBallAnimated = createAnimation(BLUE_BALL_INDEX, ballSide)
        redBallAnimated = createAnimation(RED_BALL_INDEX, ballSide)
    }

    private fun createAnimation(index: Int, ballSide: Int): AnimatedBitmap {
        val frames = Array<Bitmap>(BALL_FRAMES) {
            val side = ballSide * (it + 1) / BALL_FRAMES
            val sprite = balls!!.getScaledSprite(0, index, side, side)
            val x = (ballSide - side) / 2f
            with (Graphics(ballSide, ballSide)) {
                drawBitmap(sprite, x, x)
                frameBuffer
            }
        }
        return AnimatedBitmap(BALL_DURATION, false, *frames)
    }
}