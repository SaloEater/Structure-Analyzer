package com.saloeater.structure_analyzer.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SlotWidget.class, remap = false)
public interface SlotWidgetAccessor {
    @Accessor("stack")
    void SetStack(EmiIngredient stack);
}
