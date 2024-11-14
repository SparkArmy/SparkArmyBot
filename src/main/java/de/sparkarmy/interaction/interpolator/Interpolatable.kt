package de.sparkarmy.interaction.interpolator

interface Interpolatable<T> {
    fun interpolate(interpolator: Interpolator, context: InterpolationContext): T
}