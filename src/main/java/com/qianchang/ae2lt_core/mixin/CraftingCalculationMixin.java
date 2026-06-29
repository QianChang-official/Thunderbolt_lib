package com.qianchang.ae2lt_core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;
import appeng.crafting.inv.NetworkCraftingSimulationState;

import com.qianchang.ae2lt_core.Ae2ltCore;
import com.qianchang.ae2lt_core.CoreConfig;
import com.qianchang.ae2lt_core.crafting.ae2.FastCraftingPlanner;

/**
 * Installs the linear-time autocrafting fast path inside AE2's per-amount attempt
 * ({@code CraftingCalculation#runCraftAttempt(boolean, long)}).
 *
 * <p>By hooking the per-amount attempt instead of {@code computePlan}, AE2 keeps driving its own
 * strategy and binary-search loop (no need to reimplement CRAFT_LESS); we only replace the expensive
 * tree simulation of each attempt. When {@link FastCraftingPlanner} declines, AE2's original attempt
 * runs unchanged, so behavior is never broken — only accelerated.
 */
@Mixin(value = CraftingCalculation.class, remap = false)
public abstract class CraftingCalculationMixin {

    @Shadow
    private NetworkCraftingSimulationState networkInv;

    @Shadow
    private AEKey output;

    @Shadow
    ICraftingSimulationRequester simRequester;

    @Shadow
    private boolean simulate;

    @Inject(method = "runCraftAttempt", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2ltCore$fastAttempt(boolean simulate, long amount,
                                       CallbackInfoReturnable<CraftingPlan> cir) {
        if (!CoreConfig.FAST_PATH_ENABLED) {
            return;
        }
        try {
            var gridNode = simRequester.getGridNode();
            if (gridNode == null) {
                return;
            }
            var craftingService = gridNode.getGrid().getCraftingService();
            var attempt = FastCraftingPlanner.tryAttempt(craftingService, networkInv, output, amount, simulate);
            if (attempt.handled()) {
                // Reproduce the side effect of the real method body we are skipping, so that
                // CraftingCalculation#isSimulation() reflects the attempt that produced this plan.
                this.simulate = simulate;
                cir.setReturnValue(attempt.plan());
            }
        } catch (Throwable t) {
            // Never let the optimization break a craft: log once and fall back to AE2.
            Ae2ltCore.LOGGER.debug("[Thunderbolt Core] fast path declined due to error, falling back to AE2", t);
        }
    }
}
