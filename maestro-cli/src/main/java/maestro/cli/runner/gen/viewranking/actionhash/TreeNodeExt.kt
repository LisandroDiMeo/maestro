package maestro.cli.runner.gen.viewranking.actionhash

import maestro.TreeNode

fun TreeNode.clone(): TreeNode {
    return TreeNode(
        selected = selected,
        checked = checked,
        enabled = enabled,
        clickable = clickable,
        focused = focused,
        attributes = attributes.toMap().toMutableMap(),
        children = children.map { it.clone() }
    )
}
