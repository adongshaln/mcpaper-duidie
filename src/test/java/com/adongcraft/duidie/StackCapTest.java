package com.adongcraft.duidie;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StackCapTest {
    @Test
    void configuredValuesClampToTheVanillaComponentRange() {
        assertEquals(99, StackCaps.effectiveMaxStack(99));
        assertEquals(99, StackCaps.effectiveMaxStack(999));
        assertEquals(2, StackCaps.effectiveMaxStack(1));
        assertEquals(2, StackCaps.effectiveMaxStack(-5));
        assertEquals(64, StackCaps.effectiveMaxStack(64));
    }
}
