package hierarchy

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.convertValue

@JsonDeserialize(using = XCUIElementDeserializer::class)
interface XCUIElement

data class IdbElementNode(val children: List<AccessibilityNode>) : XCUIElement

@JsonDeserialize(`as` = XCUIElementNode::class)
data class XCUIElementNode(
    val label: String,
    val elementType: Int,
    val identifier: String,
    val horizontalSizeClass: Int,
    val windowContextID: Long,
    val verticalSizeClass: Int,
    val selected: Boolean,
    val displayID: Int,
    val hasFocus: Boolean,
    val placeholderValue: String?,
    val value: String?,
    val frame: Frame,
    val enabled: Boolean,
    val title: String?,
    val children: ArrayList<XCUIElement>?,
) : XCUIElement {
    fun isClickable(): Boolean {
        return when (XCUIElementDeserializer.ELEMENT_TYPES[elementType.toString()]) {
            "button",
            "checkBox",
            "toggle",
            "textField",
            "switch",
            "searchField",
            "radioButton",
            "popUpButton",
            "menuButton",
            "link",
            "map",
            "keyboard",
            "datePicker",
            "pickerWheel"
            -> true
            else -> false
        }
    }
}

class XCUIElementDeserializer : JsonDeserializer<XCUIElement>() {

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): XCUIElement {
        val mapper = parser.codec as ObjectMapper
        val root: TreeNode = mapper.readTree(parser)
        if (root.get(ELEMENT_TYPE_PROPERTY) != null) {
            return mapper.convertValue(root, XCUIElementNode::class.java)
        }
        val accessibilityNode: List<AccessibilityNode> = mapper.convertValue(root)
        return IdbElementNode(accessibilityNode)
    }

    companion object {
        private const val ELEMENT_TYPE_PROPERTY = "elementType"
        val ELEMENT_TYPES = mapOf(
            "1" to "activityIndicator",
            "7" to "alert",
            "0" to "any",
            "2" to "application",
            "31" to "browser",
            "9" to "button",
            "75" to "cell",
            "12" to "checkBox",
            "32" to "collectionView",
            "67" to "colorWell",
            "15" to "comboBox",
            "51" to "datePicker",
            "60" to "decrementArrow",
            "8" to "dialog",
            "13" to "disclosureTriangle",
            "70" to "dockItem",
            "6" to "drawer",
            "73" to "grid",
            "3" to "group",
            "78" to "handle",
            "68" to "helpTag",
            "44" to "icon",
            "43" to "image",
            "59" to "incrementArrow",
            "20" to "key",
            "19" to "keyboard",
            "76" to "layoutArea",
            "77" to "layoutItem",
            "74" to "levelIndicator",
            "42" to "link",
            "57" to "map",
            "69" to "matte",
            "53" to "menu",
            "55" to "menuBar",
            "56" to "menuBarItem",
            "16" to "menuButton",
            "54" to "menuItem",
            "21" to "navigationBar",
            "1" to "other",
            "29" to "outline",
            "30" to "outlineRow",
            "34" to "pageIndicator",
            "38" to "picker",
            "39" to "pickerWheel",
            "14" to "popUpButton",
            "18" to "popover",
            "35" to "progressIndicator",
            "10" to "radioButton",
            "11" to "radioGroup",
            "62" to "ratingIndicator",
            "66" to "relevanceIndicator",
            "71" to "ruler",
            "72" to "rulerMarker",
            "47" to "scrollBar",
            "46" to "scrollView",
            "45" to "searchField",
            "50" to "secureTextField",
            "37" to "segmentedControl",
            "5" to "sheet",
            "33" to "slider",
            "64" to "splitGroup",
            "65" to "splitter",
            "48" to "staticText",
            "25" to "statusBar",
            "82" to "statusItem",
            "79" to "stepper",
            "40" to "switch",
            "80" to "tab",
            "22" to "tabBar",
            "23" to "tabGroup",
            "26" to "table",
            "28" to "tableColumn",
            "27" to "tableRow",
            "49" to "textField",
            "52" to "textView",
            "61" to "timeline",
            "41" to "toggle",
            "24" to "toolbar",
            "17" to "toolbarButton",
            "63" to "valueIndicator",
            "58" to "webView",
            "4" to "window",
            "81" to "touchBar"
        )
    }
}
