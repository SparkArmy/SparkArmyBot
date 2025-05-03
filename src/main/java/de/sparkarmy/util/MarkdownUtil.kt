package de.sparkarmy.util

// Headers
fun headerFirst(text: String) = "# $text"
fun headerSecond(text: String) = "## $text"
fun headerThird(text: String) = "### $text"

// Subtext
fun subtext(text: String) = "-# $text"

// Masked Link
fun maskedLink(text: String, url: String) = "[$text]($url)"

// Mentions
fun roleMention(idLong: Long) = "<@&$idLong>"
fun roleMention(id: String) = "<@&$id>"

fun userMention(idLong: Long) = "<@$idLong>"
fun userMention(id: String) = "<@$id>"

