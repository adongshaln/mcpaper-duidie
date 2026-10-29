package com.adongcraft.duidie;

/** 配置数值的收口规则（纯逻辑，便于单元测试）。 */
final class StackCaps {
    private StackCaps() { }

    /** 原版 max_stack_size 组件的硬上限是 99，配置再大也只能到 99；下限 2 才有意义。 */
    static int effectiveMaxStack(int configured) {
        return Math.clamp(configured, 2, 99);
    }
}
