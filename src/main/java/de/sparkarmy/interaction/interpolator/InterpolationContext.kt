package de.sparkarmy.interaction.interpolator

import de.sparkarmy.interaction.interpolator.namespace.Env
import de.sparkarmy.interaction.interpolator.namespace.ExpressionNamespace
import kotlin.reflect.KClass

data class InterpolationContext(
    val env: Env,
    val namespaces: Set<KClass<out ExpressionNamespace>>,
    val arguments: Map<String, Any?>,
)