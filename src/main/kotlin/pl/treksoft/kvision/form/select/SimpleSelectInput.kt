/*
 * Copyright (c) 2017-present Robert Jaros
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package pl.treksoft.kvision.form.select

import com.github.snabbdom.VNode
import pl.treksoft.kvision.core.Container
import pl.treksoft.kvision.core.StringBoolPair
import pl.treksoft.kvision.core.StringPair
import pl.treksoft.kvision.form.FormInput
import pl.treksoft.kvision.form.InputSize
import pl.treksoft.kvision.form.ValidationStatus
import pl.treksoft.kvision.html.TAG
import pl.treksoft.kvision.html.Tag
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.state.ObservableState
import pl.treksoft.kvision.utils.set

internal const val KVNULL = "#kvnull"

/**
 * Simple select component.
 *
 * @constructor
 * @param options an optional list of options (value to label pairs) for the select control
 * @param value select input value
 * @param emptyOption determines if an empty option is automatically generated
 * @param multiple allows multiple value selection (multiple values are comma delimited)
 * @param selectSize the number of visible options
 * @param classes a set of CSS class names
 */
open class SimpleSelectInput(
    options: List<StringPair>? = null, value: String? = null, emptyOption: Boolean = false,
    multiple: Boolean = false,
    selectSize: Int? = null,
    classes: Set<String> = setOf()
) : SimplePanel(classes + "form-control"), FormInput, ObservableState<String?> {

    protected val observers = mutableListOf<(String?) -> Unit>()

    /**
     * A list of options (value to label pairs) for the select control.
     */
    var options by refreshOnUpdate(options) { setChildrenFromOptions() }

    /**
     * Text input value.
     */
    var value by refreshOnUpdate(value) { refreshState(); observers.forEach { ob -> ob(it) } }

    /**
     * The value of the selected child option.
     *
     * This value is placed directly in the generated HTML code, while the [value] property is dynamically
     * bound to the select component.
     */
    var startValue by refreshOnUpdate(value) { this.value = it; selectOption() }

    /**
     * The name attribute of the generated HTML input element.
     */
    override var name: String? by refreshOnUpdate()

    /**
     * Determines if the field is disabled.
     */
    override var disabled by refreshOnUpdate(false)

    /**
     * Determines if the text input is automatically focused.
     */
    var autofocus: Boolean? by refreshOnUpdate()

    /**
     * Determines if an empty option is automatically generated.
     */
    var emptyOption by refreshOnUpdate(emptyOption) { setChildrenFromOptions() }

    /**
     * Determines if multiple value selection is allowed.
     */
    var multiple by refreshOnUpdate(multiple)

    /**
     * The number of visible options.
     */
    var selectSize: Int? by refreshOnUpdate(selectSize)

    /**
     * The size of the input.
     */
    override var size: InputSize? by refreshOnUpdate()

    /**
     * The validation status of the input.
     */
    override var validationStatus: ValidationStatus? by refreshOnUpdate()

    init {
        setChildrenFromOptions()
        this.setInternalEventListener<SimpleSelectInput> {
            change = {
                val v = getElementJQuery()?.`val`()
                self.value = v?.let {
                    calculateValue(it)
                }
            }
        }
    }

    protected open fun calculateValue(v: Any): String? {
        return if (this.multiple) {
            @Suppress("UNCHECKED_CAST")
            val arr = v as? Array<String>
            if (arr != null && arr.isNotEmpty()) {
                arr.filter { it.isNotEmpty() }.joinToString(",")
            } else {
                null
            }
        } else {
            val vs = v as String?
            if (vs != null && vs.isNotEmpty() && vs != KVNULL) {
                vs
            } else {
                null
            }
        }
    }

    override fun render(): VNode {
        return render("select", childrenVNodes())
    }

    private fun setChildrenFromOptions() {
        super.removeAll()
        if (emptyOption) {
            super.add(Tag(TAG.OPTION, "", attributes = mapOf("value" to KVNULL)))
        }
        val valueSet = if (this.multiple) value?.split(",") ?: emptySet() else setOf(value)
        options?.let {
            val c = it.map {
                val attributes = if (valueSet.contains(it.first)) {
                    mapOf("value" to it.first, "selected" to "selected")
                } else {
                    mapOf("value" to it.first)
                }
                Tag(TAG.OPTION, it.second, attributes = attributes)
            }
            super.addAll(c)
        }
    }

    private fun selectOption() {
        val valueSet = if (this.multiple) value?.split(",") ?: emptySet() else setOf(value)
        children.forEach { child ->
            if (child is Tag && child.type == TAG.OPTION) {
                if (valueSet.contains(child.getAttribute("value"))) {
                    child.setAttribute("selected", "selected")
                } else {
                    child.removeAttribute("selected")
                }
            }
        }
    }

    override fun getSnClass(): List<StringBoolPair> {
        val cl = super.getSnClass().toMutableList()
        validationStatus?.let {
            cl.add(it.className to true)
        }
        size?.let {
            cl.add(it.className to true)
        }
        return cl
    }

    override fun getSnAttrs(): List<StringPair> {
        val sn = super.getSnAttrs().toMutableList()
        name?.let {
            sn.add("name" to it)
        }
        if (multiple) {
            sn.add("multiple" to "multiple")
        }
        selectSize?.let {
            sn.add("size" to it.toString())
        }
        autofocus?.let {
            if (it) {
                sn.add("autofocus" to "autofocus")
            }
        }
        if (disabled) {
            sn.add("disabled" to "disabled")
        }
        return sn
    }

    override fun afterInsert(node: VNode) {
        refreshState()
    }

    protected open fun refreshState() {
        value?.let {
            if (this.multiple) {
                getElementJQuery()?.`val`(it.split(",").toTypedArray())
            } else {
                getElementJQuery()?.`val`(it)
            }
        } ?: getElementJQueryD()?.`val`(null)
    }

    /**
     * Makes the input element focused.
     */
    override fun focus() {
        getElementJQuery()?.focus()
    }

    /**
     * Makes the input element blur.
     */
    override fun blur() {
        getElementJQuery()?.blur()
    }

    override fun getState(): String? = value

    override fun subscribe(observer: (String?) -> Unit): () -> Unit {
        observers += observer
        observer(value)
        return {
            observers -= observer
        }
    }
}

/**
 * DSL builder extension function.
 *
 * It takes the same parameters as the constructor of the built component.
 */
fun Container.simpleSelectInput(
    options: List<StringPair>? = null, value: String? = null, emptyOption: Boolean = false,
    multiple: Boolean = false,
    selectSize: Int? = null,
    classes: Set<String>? = null,
    className: String? = null,
    init: (SimpleSelectInput.() -> Unit)? = null
): SimpleSelectInput {
    val simpleSelectInput =
        SimpleSelectInput(
            options,
            value,
            emptyOption,
            multiple,
            selectSize,
            classes ?: className.set
        ).apply { init?.invoke(this) }
    this.add(simpleSelectInput)
    return simpleSelectInput
}
