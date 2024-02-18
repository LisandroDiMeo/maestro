package maestro.cli.runner.gen.actionhash

import maestro.TreeNode
import maestro.orchestra.MaestroCommand

/**
 * Interface that defines the method to hash an action.
 * This is important on model based strategies (or when it's desired to build a graphical model)
 * since it will specify the granularity of when an action is different to another.
 * For example, a ActionHasher that produces a different hash for each call,
 * (e.g: hashAction(a) = 1, hashAction(a) = 2) will treat each action as unique,
 * finally producing a very specific model. Instead, if we analyze the action at the given tree,
 * we would be able to produce a more reasonable model.
 */
interface ActionHasher {
    fun hashAction(
        root: TreeNode,
        action: MaestroCommand,
        view: TreeNode?
    ): String
}
