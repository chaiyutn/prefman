package com.chongos.prefman

import java.util.*
import javax.lang.model.element.Element

/**
 * @author ChongOS
 * @since 07-Jul-2017
 */
internal class Field(val element: Element, val isModel: Boolean) {

    var parent: Field? = null
    val children: MutableList<Field> by lazy { ArrayList<Field>() }
    val name: String = element.toString()

    val prefKey: String
        get() = if (parent == null) name else "${parent!!.prefKey}_$name"

    val access: String
        get() = if (parent == null) name else "${parent!!.access}.$name"

    fun addChild(child: Field) = children.add(child)

    fun hasChild(): Boolean = children.size > 0

    override fun toString(): String {
        val str = StringBuilder("$name : ${element.asType()}")
        if (hasChild()) {
            str.append(" -> [")
            for (child in children) {
                str.append(child).append(", ")
            }
            str.delete(str.length - 2, str.length)
            str.append("]")
        }
        return str.toString()
    }
}
