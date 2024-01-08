package maestro.cli.runner.gen.viewranking.actionhash

import maestro.TreeNode
import maestro.orchestra.MaestroCommand

interface ActionHasher {
    fun hashAction(
        root: TreeNode,
        action: MaestroCommand,
        view: TreeNode?
    ): String
}
