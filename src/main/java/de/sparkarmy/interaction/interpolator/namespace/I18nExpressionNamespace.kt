package de.sparkarmy.interaction.interpolator.namespace

import de.sparkarmy.i18n.LocalizationService
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

const val LOCALE_ENV_KEY = "locale"

@Single
class I18nExpressionNamespace : ExpressionNamespace, KoinComponent {
    override val name = "i18n"
    private val localizationService by inject<LocalizationService>()

    override fun property(property: String, env: Env): String {
        return function(property, env, emptyList())
    }

    override fun function(function: String, env: Env, arguments: Arguments): String {
        val localeValue = env[LOCALE_ENV_KEY]
            ?: throw IllegalStateException("Missing \"$LOCALE_ENV_KEY\" property in env!")

        val locale = getLocale(localeValue)

        val typeSafeArguments = arguments.map { it ?: "null" }.toTypedArray()
        return localizationService.getString(locale, function, *typeSafeArguments)
    }

    private fun getLocale(localeStr: String): Locale {
        return Locale.forLanguageTag(localeStr)
    }
}