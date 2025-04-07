package es.uji.jvilar.frameworktest

import androidx.lifecycle.ViewModel

class TicTacToeViewModel : ViewModel() {
    enum class CellColor {
        EMPTY, RED, BLUE
    }

    var view: TicTacToeView? = null

    private val board: Array<Array<CellColor>> = Array(3) { Array(3) {CellColor.EMPTY} }
    var lastRowPlayed = -1
        private set
    var lastColumPlayed = -1
        private set
    var turn: CellColor = CellColor.EMPTY
        private set
    var winner: CellColor = CellColor.EMPTY
        private set
    val isEnded get() = turn == CellColor.EMPTY
    val winnerCells: Array<IntArray> = Array(3) { intArrayOf(0, 0) }
    private var turnCount = 0

    init {
        restart()
    }

    fun restart() {
        lastRowPlayed = -1
        lastColumPlayed = -1
        turnCount = 0
        for (row in 0..2)
            for (column in 0..2)
                board[row][column] = CellColor.EMPTY
        turn = CellColor.RED
        winner = CellColor.EMPTY
    }

    fun getCell(row: Int, column: Int): CellColor {
        return board[row][column]
    }

    fun canPlay(row: Int, column: Int): Boolean {
        return row in 0 .. 2 && column in 0 .. 2 &&
                turn != CellColor.EMPTY &&
                board[row][column] == CellColor.EMPTY
    }

    fun play(row: Int, column: Int) {
        lastRowPlayed = row
        lastColumPlayed = column
        board[row][column] = turn
        view?.playMove()
        val wins = winsRow(row) || winsColumn(column) || winsDiagonal(row, column)
        if (wins) {
            winner = turn
            turn = CellColor.EMPTY
            view?.playVictory()
        } else {
            turnCount++
            if (turnCount == 9) {
                winner = CellColor.EMPTY
                turn = CellColor.EMPTY
            } else turn = if (turn == CellColor.RED) CellColor.BLUE else CellColor.RED
        }
    }

    private fun winsRow(row: Int) = checkCells(row, 0, row, 1, row, 2)

    private fun winsColumn(column: Int) = checkCells(0, column, 1, column, 2, column)

    private fun winsDiagonal(row: Int, column: Int) = if (row == column)
        checkCells(0, 0, 1, 1, 2, 2)
    else if (row == 2 - column)
        checkCells(0, 2, 1, 1, 2, 0)
    else false

    private fun checkCells(c00: Int, c01: Int, c10: Int, c11: Int, c20: Int, c21: Int): Boolean {
        if (board[c00][c01] != board[c10][c11] || board[c10][c11] != board[c20][c21])
            return false
        winnerCells[0][0] = c00
        winnerCells[0][1] = c01
        winnerCells[1][0] = c10
        winnerCells[1][1] = c11
        winnerCells[2][0] = c20
        winnerCells[2][1] = c21
        return true
    }
}