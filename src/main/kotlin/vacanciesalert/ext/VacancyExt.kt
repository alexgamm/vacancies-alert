package vacanciesalert.ext

import vacanciesalert.ext.hh.search.Contact
import vacanciesalert.ext.hh.search.Salary
import vacanciesalert.ext.hh.search.Vacancy

fun Vacancy.format() =
    listOfNotNull(
        "$name, ${area?.name}",
        department?.name ?: employer?.name,
        contacts?.format()?.let { "\n$it" },
        "\n<b>Заработная плата: </b>" + (salary?.format() ?: "не указана"),
        "<b>Требуемый опыт:</b> ${experience?.name?.lowercase()}",
        "\n" + alternateUrl
    ).joinToString(separator = "\n")

fun Salary.format() =
    listOfNotNull(
        from?.let { "от $it" },
        to?.let { "до $it" }
    ).joinToString(" ") + " $currency"

fun Contact.format(): String {
    val reachOutBy = phones?.takeIf { it.isNotEmpty() }?.joinToString(separator = "\n\n") {
        "${it.comment.orEmpty()}\n${it.formatted.orEmpty()}"
    }
    return listOfNotNull(
        name?.let { "<b>Рекрутер:</b> $it" },
        email?.let { "Контакты: $it" },
        reachOutBy
    ).joinToString(separator = "\n")
}

