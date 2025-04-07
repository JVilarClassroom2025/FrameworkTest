package es.uji.jvilar.frameworktest

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.viewModels
import es.uji.vj1229.framework.AnimatedBitmap
import es.uji.vj1229.framework.GameActivity
import es.uji.vj1229.framework.Graphics
import es.uji.vj1229.framework.IEventProcessor
import es.uji.vj1229.framework.TouchHandler

class MainActivity : GameActivity(), TicTacToeView, IEventProcessor {
    companion object {
        private const val BACKGROUND_COLOR = -12723068 // 0xff3ddc84
        private const val LINE_COLOR = Color.BLACK
        private const val WIN_COLOR = Color.YELLOW
    }
    private lateinit var transformation: Transformation
    
    private lateinit var graphics: Graphics
    private val viewModel: TicTacToeViewModel by viewModels()

    private lateinit var soundPool: SoundPool
    private var victorySoundId = 0
    private var moveSoundId = 0

    private var lastRow = -1
    private var lastColumn = -1
    private var animation: AnimatedBitmap? = null
    private var hasToDraw = true

    private var xReset = 0f
    private var yReset = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        portraitFullScreenOnCreate()
        prepareSoundPool()
    }

    override fun onResume() {
        super.onResume()
        viewModel.view = this
        hasToDraw = true
    }

    override fun onPause() {
        super.onPause()
        viewModel.view = null
    }

    override fun getEventProcessor() = this
    override fun onUpdate(deltaTime: Float, touchEvents: List<TouchHandler.TouchEvent>) {
        for (event in touchEvents)
            if (event.type == TouchHandler.TouchType.TOUCH_UP) {
                if (pointInReset(event.x, event.y)) //
                    viewModel.restart()
                else with(transformation) {
                    val col = x2Col(event.x)
                    val row = y2Row(event.y)
                    if (viewModel.canPlay(row, col))
                        viewModel.play(row, col)
                }
            }
        updateAnimation(deltaTime)
    }

    private fun pointInReset(x: Int, y: Int) = with(transformation) {
        xReset <= x && x <= xReset + ballSide
                && yReset <= y && y <= yReset + ballSide
    }

    private fun prepareSoundPool() {
        val attributes = AudioAttributes.Builder().run {
            setUsage(AudioAttributes.USAGE_GAME)
            setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            build()
        }
        soundPool = SoundPool.Builder().run {
            setMaxStreams(5)
            setAudioAttributes(attributes)
            build()
        }
        victorySoundId = soundPool.load(this, R.raw.victory, 0)
        moveSoundId = soundPool.load(this, R.raw.move, 0)
    }

    override fun onBitmapMeasuresAvailable(width: Int, height: Int) {
        transformation = Transformation(width, height)
        Assets.createAssets(this, transformation.ballSide.toInt())
        graphics = Graphics(width, height)
        with(transformation) {
            xReset = col2X(1)
            yReset = row2Y(4)
            graphics.setTextSize((0.8f * ballSide).toInt())
            graphics.setTextColor(Color.BLACK)
        }
    }
    
    override fun onDrawingRequested(): Bitmap? {
        if (!hasToDraw)
            return null
        graphics.clear(BACKGROUND_COLOR)
        drawTurn()
        drawReset()
        drawBoard()
        drawPieces()
        drawEnd()
        hasToDraw = animation.let { it != null && !it.isEnded }

        return graphics.frameBuffer
    }

    private fun drawReset() = with(transformation) {
        graphics.drawDrawable(Assets.reset, xReset, yReset, ballSide, ballSide)
    }

    private fun drawTurn() = with(transformation) {
        if (viewModel.turn == TicTacToeViewModel.CellColor.EMPTY)
            return
        val asset = if (viewModel.turn == TicTacToeViewModel.CellColor.RED)
            Assets.redBall
        else
            Assets.blueBall
        graphics.drawText(col2X(0), row2Y(-2) + 0.75f * ballSide, "Turn:")
        graphics.drawBitmap(asset, col2X(2), row2Y(-2))
    }

    private fun drawBoard() = with(transformation) {
        with(graphics) {
            for (col in 1..2) {
                val x = col2X(col)
                drawLine(x, row2Y(0), x, row2Y(3), lineWidth, LINE_COLOR)
            }
            for (row in 1..2) {
                val y = row2Y(row)
                drawLine(col2X(0), y, col2X(3), y, lineWidth, LINE_COLOR)
            }
        }
    }

    private fun drawPieces() = with(transformation) {
        for (row in 0..2)
            for (col in 0..2) {
                val cell = viewModel.getCell(row, col)
                if (cell != TicTacToeViewModel.CellColor.EMPTY) {
                    val bitmap = if (row == lastRow && col == lastColumn)
                        animation?.currentFrame
                    else
                        if (cell == TicTacToeViewModel.CellColor.RED)
                            Assets.redBall
                        else
                            Assets.blueBall
                    graphics.drawBitmap(bitmap, col2X(col) + halfLineWidth, row2Y(row) + halfLineWidth)
                }
            }
    }

    private fun drawEnd() = with(transformation) {
        if (!viewModel.isEnded)
            return
        if (viewModel.winner == TicTacToeViewModel.CellColor.EMPTY) {
            graphics.drawText(col2X(0), row2Y(-2) + 0.75f * ballSide, "Draw!")
            return
        }
        val asset = if (viewModel.winner == TicTacToeViewModel.CellColor.RED)
            Assets.redBall
        else
            Assets.blueBall
        graphics.drawText(col2X(1), row2Y(-2) + 0.75f * ballSide, "Wins!")
        graphics.drawBitmap(asset, col2X(0), row2Y(-2))

        val winnerCells = viewModel.winnerCells
        val row0 = winnerCells[0][0]
        val row1 = winnerCells[2][0]
        val col0 = winnerCells[0][1]
        val col1 = winnerCells[2][1]
        val x0: Float
        val x1: Float
        val y0: Float
        val y1: Float
        if (row0 == row1) { // horizontal line
            y0 = row2Y(row0) + 0.5f * cellSide
            y1 = y0
            x0 = col2X(col0)
            x1 = col2X(col1 + 1)
        } else if (col0 == col1) { // vertical line
            x0 = col2X(col0) + 0.5f * cellSide
            x1 = x0
            y0 = row2Y(row0)
            y1 = row2Y(row1 + 1)
        } else { // diagonal line
            if (col0 == 0) {
                x0 = col2X(0)
                x1 = col2X(3)
            } else {
                x0 = col2X(3)
                x1 = col2X(0)
            }
            y0 = row2Y(0)
            y1 = row2Y(3)
        }
        graphics.drawLine(x0, y0, x1, y1, lineWidth, WIN_COLOR)
    }

    override fun playVictory() {
        soundPool.play(victorySoundId, 0.6f, 0.8f, 0, 0, 1f)
    }

    override fun playMove() {
        soundPool.play(moveSoundId, 0.6f, 0.8f, 0, 0, 1f)
    }

    fun updateAnimation(deltaTime: Float) {
        animation?.onUpdate(deltaTime)
        if (viewModel.lastRowPlayed != lastRow || viewModel.lastColumPlayed != lastColumn) {
            hasToDraw = true
            lastRow = viewModel.lastRowPlayed
            lastColumn = viewModel.lastColumPlayed
            if (lastRow != -1 && lastColumn != -1) {
                animation = when (viewModel.getCell(lastRow, lastColumn)) {
                    TicTacToeViewModel.CellColor.BLUE -> Assets.blueBallAnimated
                    TicTacToeViewModel.CellColor.RED -> Assets.redBallAnimated
                    else -> null
                }
                animation?.restart()
            } else
                animation = null
        }
    }
}