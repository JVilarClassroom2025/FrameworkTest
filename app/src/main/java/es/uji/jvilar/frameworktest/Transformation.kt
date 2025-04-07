package es.uji.jvilar.frameworktest

class Transformation(width: Int, height: Int) {
    companion object {
        private const val LINE_WIDTH_FRACTION = 0.02f
        private const val MARGIN_FRACTION = 0.1f
        private const val CELL_FRACTION = (1 - 2 * MARGIN_FRACTION) / 3
    }

    val cellSide: Int = (width * CELL_FRACTION).toInt()
    val lineWidth = width * LINE_WIDTH_FRACTION
    val halfLineWidth = 0.5f * lineWidth
    val ballSide = cellSide - lineWidth

    private val xOffset= (width - 3 * cellSide) / 2f
    private val yOffset = (height - 3 * cellSide) / 2f

    fun col2X(col: Int) = xOffset + cellSide * col
    fun row2Y(row: Int) = yOffset + cellSide * row

    fun x2Col(x: Int) = ((x - xOffset) / cellSide).toInt()

    fun y2Row(y: Int) = ((y - yOffset) / cellSide).toInt()
}