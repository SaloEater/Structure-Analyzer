package com.saloeater.structure_analyzer.util;

import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;

public class EMIHack {
    public static void reloadEMIScreen() {
        if (!ModList.get().isLoaded("emi") || !(Minecraft.getInstance().screen instanceof RecipeScreen emiScreen)) {
            return;
        }

        emiScreen.focusCategory(emiScreen.getFocusedCategory());
    }
}
