package it.unibo.jakta.examples.tictactoe.model

import kotlin.reflect.KProperty

object OwnName {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = property.name
}
