package com.saloeater.structure_analyzer.util;

import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.Minecraft;

public class EMIHack {
    public static void reloadEMIScreen() {
        if (!(Minecraft.getInstance().screen instanceof RecipeScreen emiScreen)) {
            return;
        }

        emiScreen.focusCategory(emiScreen.getFocusedCategory());
    }
}
