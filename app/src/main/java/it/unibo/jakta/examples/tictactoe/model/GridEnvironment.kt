package it.unibo.jakta.examples.tictactoe.model

import android.widget.TextView
import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.environment.impl.EnvironmentImpl
import it.unibo.jakta.agents.bdi.messages.MessageQueue
import it.unibo.jakta.agents.bdi.perception.Perception
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import kotlin.random.Random

class GridEnvironment(
    private val n: Int,
    private val textView: TextView,
    private val logView: TextView,
    agentIDs: Map<String, AgentID> = emptyMap(),
    externalActions: Map<String, ExternalAction> = emptyMap(),
    messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
    perception: Perception = Perception.of(
        Belief.fromPerceptSource(Struct.of("turn", Atom.of("x"))),
    ),
    data: Map<String, Any> = mapOf("grid" to createGrid(n), "turn" to "x"),
) : EnvironmentImpl(externalActions, agentIDs, messageBoxes, perception, data) {

    companion object {
        internal fun createGrid(n: Int): Array<CharArray> {
            val grid = Array(n) { CharArray(n) { 'e' } }
            val end = n/2
            (1 until end).map {
                println("$it and $end")
                Random.nextInt(n) to Random.nextInt(n)
            }.forEach { (x, y) ->
                grid[x][y] = 'x'
            }
            return grid
        }

        internal fun Array<CharArray>.copy() =
            Array(size) { i -> CharArray(this[i].size) { j -> this[i][j] } }
    }

    @Suppress("UNCHECKED_CAST")
    internal val grid: Array<CharArray>
        get() = data["grid"] as Array<CharArray>

    @Suppress("UNCHECKED_CAST")
    override fun updateData(newData: Map<String, Any>): Environment {
        var newEnv = this
        if ("cell" in newData) {
            val cell = newData["cell"] as Triple<Int, Int, Char>
            val result = computeNextTurnEnvironment()
            result.grid[cell.first][cell.second] = cell.third
            //result.grid.map { println(it) }
            var grid = ""
            result.grid.forEach { grid += "${it.let { String(it) + " " }} \n" }
            textView.post { textView.text = grid }
            //logView.post { logView.text = "Putting ${cell.third} at (${cell.first}, ${cell.second})" }
            //Thread.sleep(500)
            newEnv = result
        }
        if ("changeTurn" in newData) {
            newEnv = computeNextTurnEnvironment()
        }
        return newEnv
    }

    private fun computeNextTurnEnvironment(): GridEnvironment {
        val actualTurn = data["turn"] as String
        val nextTurn = if (actualTurn == "x") "o" else "x"
        return copy(
            data = mapOf("grid" to grid.copy(), "turn" to nextTurn),
            perception = Perception.of(
                Belief.fromPerceptSource(Struct.of("turn", Atom.of(nextTurn))),
            ),
        )
    }

    override fun percept(): BeliefBase =
        BeliefBase.of(
            buildList {
                for (i in grid.indices) {
                    for (j in grid[i].indices) {
                        add(
                            Belief.fromPerceptSource(
                                Struct.of("cell", Integer.of(i), Integer.of(j), Atom.of("${grid[i][j]}")),
                            ),
                        )
                    }
                }
            },
        ).addAll(perception.percept()).updatedBeliefBase

    override fun copy(
        agentIDs: Map<String, AgentID>,
        externalActions: Map<String, ExternalAction>,
        messageBoxes: Map<AgentID, MessageQueue>,
        perception: Perception,
        data: Map<String, Any>,
    ) = GridEnvironment(n, textView, logView, agentIDs, externalActions, messageBoxes, perception, data)
}
