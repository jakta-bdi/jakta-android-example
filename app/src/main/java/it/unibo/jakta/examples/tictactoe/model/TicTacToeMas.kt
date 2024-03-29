package it.unibo.jakta.examples.tictactoe.model

import android.widget.TextView
import it.unibo.jakta.agents.bdi.Jakta
import it.unibo.jakta.agents.bdi.dsl.MasScope
import it.unibo.jakta.agents.bdi.dsl.beliefs.BeliefsScope
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.dsl.plans.BodyScope
import it.unibo.jakta.agents.bdi.dsl.plans.PlansScope
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.End
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.Victory
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.aligned
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.allPossibleCombinationsOf
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.antidiagonal
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.cell
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.diagonal
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.e
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.horizontal
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.invoke
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.o
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.stop
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.turn
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.vertical
import it.unibo.jakta.examples.tictactoe.model.TicTacToeLiterals.x
import it.unibo.tuprolog.core.Atom

fun BeliefsScope.alignment(name: String, dx: Int, dy: Int) {
    val first = cell(A, B, C)
    val second = cell(X, Y, Z)
    rule { name(listOf(second)).fromSelf impliedBy second }
    rule {
        name(listFrom(first, second, last = W)).fromSelf.impliedBy(
            // write("Handling  $name("), write(listFrom(first, second, last = W)), write(")"), nl,
            first,
            second,
            (X - A) arithEq dx,
            (Y - B) arithEq dy,
            name(listFrom(second, last = W)).fromSelf,
        )
    }
}

fun ticTacToe(n: Int = 3, textView: TextView, logView: TextView) = mas {
    require(n > 0)
    environment {
        from(GridEnvironment(n, textView, logView))
        actions {
            action(Put)
            action("End", 0) {
                this.updateData(mapOf("changeTurn" to "other"))
            }
            action("Victory", 0) {
                logView.post { logView.text = "[${this.sender}] I Won!" }
            }
        }
    }
    player(mySymbol = o, otherSymbol = x, gridSize = n)
    player(mySymbol = x, otherSymbol = o, gridSize = n)
    executionStrategy { ExecutionStrategy.oneThreadPerAgent() }
}

fun MasScope.player(mySymbol: String, otherSymbol: String, gridSize: Int) = agent("$mySymbol-agent") {
    beliefs {
        alignment(vertical, dx = 0, dy = 1)
        alignment(horizontal, dx = 1, dy = 0)
        alignment(diagonal, dx = 1, dy = 1)
        alignment(antidiagonal, dx = 1, dy = -1)
        for (direction in arrayOf(vertical, horizontal, diagonal, antidiagonal)) {
            rule { aligned(L) impliedBy direction(L).fromSelf }
        }
    }
    plans {
        detectVictory(mySymbol, gridSize) // plan 1
        detectDefeat(mySymbol, otherSymbol, gridSize) // plan 2
        makeWinningMove(mySymbol, gridSize) // plan 3
        preventOtherFromWinning(mySymbol, otherSymbol, gridSize) // plan 4
        randomMove(mySymbol) // plan 5
    }
}

fun PlansScope.detectVictory(mySymbol: String, gridSize: Int) =
    detectLine(mySymbol, mySymbol, gridSize) {
        execute(End)
        execute(Victory)
        execute(stop)
    }
fun PlansScope.detectDefeat(mySymbol: String, otherSymbol: String, gridSize: Int) =
    detectLine(mySymbol, otherSymbol, gridSize) {
        execute(End)
        execute(stop)
    }

fun PlansScope.detectLine(mySymbol: String, symbol: String, size: Int, action: BodyScope.() -> Unit) =
    +turn(mySymbol) onlyIf { aligned((1..size).map { cell(symbol) }) } then(action)

fun PlansScope.makeWinningMove(mySymbol: String, gridSize: Int, symbol: String = mySymbol) {
    for (winningLine in allPossibleCombinationsOf(cell(X, Y, e), cell(symbol), gridSize - 1)) {
        +turn(mySymbol) onlyIf { aligned(winningLine) } then { Put(X, Y, mySymbol) }
    }
}

fun PlansScope.preventOtherFromWinning(mySymbol: String, otherSymbol: String, gridSize: Int) =
    makeWinningMove(mySymbol, gridSize, otherSymbol)

fun PlansScope.randomMove(mySymbol: String) =
    +turn(mySymbol) onlyIf { cell(X, Y, e) } then { Put(X, Y, mySymbol) }

//fun main() {
//    val system = ticTacToe(3)
//    for (agent in system.agents) {
//        Jakta.printAslSyntax(agent)
//    }
//    println("--------------------------------------------------------------------")
//    system.start()
//}
