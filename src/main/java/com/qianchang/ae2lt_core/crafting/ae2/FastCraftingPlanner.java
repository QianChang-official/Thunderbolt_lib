package com.qianchang.ae2lt_core.crafting.ae2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import net.minecraft.world.item.Item;
import appeng.crafting.CraftingPlan;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;

import com.qianchang.ae2lt_core.crafting.core.BoundedCombinations;
import com.qianchang.ae2lt_core.crafting.core.CraftGraph;
import com.qianchang.ae2lt_core.crafting.core.CraftInput;
import com.qianchang.ae2lt_core.crafting.core.CraftOutput;
import com.qianchang.ae2lt_core.crafting.core.CraftPattern;
import com.qianchang.ae2lt_core.crafting.core.CraftPlan;
import com.qianchang.ae2lt_core.crafting.core.CraftPlannerV2;
import com.qianchang.ae2lt_core.crafting.core.DurabilityChain;
import com.qianchang.ae2lt_core.crafting.core.Sat;

/**
 * Bridges one of AE2's per-amount crafting attempts ({@code CraftingCalculation#runCraftAttempt})
 * to the linear {@link CraftPlanner} fast path, producing AE2-compatible {@link CraftingPlan}s.
 *
 * <p>Hooking the per-amount attempt (rather than the whole {@code computePlan}) lets AE2 keep driving
 * its own strategy/binary-search loop while we replace only the expensive tree simulation of each
 * attempt. The contract of {@code runCraftAttempt(simulate, amount)} is mirrored exactly:
 * {@code simulate=false} returns a feasible plan or {@code null} (this amount can't be made);
 * {@code simulate=true} must return a non-null plan carrying the missing items.
 *
 * <p>Engine: the v2 planner ({@link CraftPlannerV2}) — a linear topological backbone with a bounded
 * backtracking fallback for contention. It natively handles byproducts and multiple competing recipes.
 *
 * <p>Correctness rules:
 * <ul>
 *   <li>Out of envelope (emit / substitution(fuzzy) / container / catalyst) → {@link
 *       FastAttempt#decline()} and AE2's original attempt runs unchanged. Byproducts ARE handled
 *       (routed through a shared pool).</li>
 *   <li><b>Recursion / cycle</b> → handled in-engine: the v2 planner breaks back-edges ("去头尾") so a
 *       compress/decompress pair (1 block ⇄ 9 ingots) is planned directly instead of declining. The
 *       reverse side resolves from stock/missing; cuts only remove options, never overstate feasibility.</li>
 *   <li>A <b>feasible</b> plan is always safe to return (mass-balanced ⇒ executable), even with
 *       byproducts and multiple recipe choices.</li>
 *   <li><b>Infeasible</b>: authoritative iff the bounded search did not hit its per-node cap
 *       ({@code !budgetExhausted}); then {@code simulate=false}→null, {@code simulate=true}→partial
 *       plan with missing items. If the cap was hit ({@code budgetExhausted}) we decline so AE2's
 *       exhaustive simulator can make the final call (chosen exhaustion policy = best-effort, but we
 *       never assert infeasibility we are unsure about).</li>
 * </ul>
 *
 * <p>Byte accounting reproduces AE2's formulas (see {@code CraftingTreeNode#request},
 * {@code CraftingTreeProcess#request} and {@code ICraftingSimulationState#addStackBytes}) over the
 * memoized DAG: byte-identical to AE2 for jobs without shared sub-graphs, smaller otherwise.
 */
public final class FastCraftingPlanner {

    /**
     * Hard-fuzzy budget: an input slot that accepts several substitutes is expanded into the cartesian
     * product of concrete choices, each becoming a competing recipe the v2 planner selects among by
     * availability. If a pattern's product of substitute counts exceeds this, we DON'T drop the recipe
     * (that would be a false negative for something AE2 can craft); instead we greedily keep the best
     * {@code FUZZY_NONCYCLE_STEPS} combinations — the lowest rank-sum over per-slot, most-available-first
     * ordered options — bounding the graph without losing the cheapest routes ("贪心前 N" for non-cyclic
     * fuzzy).
     */
    static final long FUZZY_NONCYCLE_STEPS = 64;

    /**
     * Cyclic-fuzzy budget. A durability tool {@code 1·A(n) + 1·B → 1·C + A(n-1)} forms a degradation
     * chain {@code A(n)→A(n-1)→…→broken}. We walk that chain once via {@code getRemainingKey}, capping
     * the walk here ("超步报缺失" → decline to AE2), then reduce it to the closed form
     * {@code uses = chainLen} so a batch costs {@code ceil(times / uses)} full tools instead of one
     * firing per durability point. Also bounds the secondary-fuzzy (re-fuzzy output) collapse.
     */
    static final long FUZZY_CYCLE_STEPS = 8192;

    private FastCraftingPlanner() {
    }

    /** Outcome of an attempt: either declined (run AE2) or handled (use {@link #plan}, may be null). */
    public record FastAttempt(boolean handled, @Nullable CraftingPlan plan) {
        static FastAttempt decline() {
            return new FastAttempt(false, null);
        }

        static FastAttempt handled(@Nullable CraftingPlan plan) {
            return new FastAttempt(true, plan);
        }
    }

    /**
     * Try to satisfy a single {@code runCraftAttempt(simulate, amount)} call.
     */
    public static FastAttempt tryAttempt(ICraftingService craftingService,
                                         CraftingSimulationState networkInv,
                                         AEKey output,
                                         long amount,
                                         boolean simulate) {
        if (amount <= 0) {
            return FastAttempt.decline();
        }

        // Snapshot inventory the same way AE2 does: a child view that ignores the requested output
        // (existing output stock is never consumed; the full amount is always crafted).
        ChildCraftingSimulationState snapshot = new ChildCraftingSimulationState(networkInv);
        snapshot.ignore(output);

        CraftGraph.Builder<AEKey> builder = CraftGraph.builder();
        boolean[] multiplePaths = {false};
        Map<AEKey, DurabilityChain<AEKey>> durability = new HashMap<>();
        if (!buildGraph(craftingService, snapshot, output, builder, multiplePaths, durability)) {
            return FastAttempt.decline(); // emit / substitution / container / catalyst
        }

        CraftPlan<AEKey> plan = CraftPlannerV2.plan(builder.build(), output, amount);
        if (!plan.supported()) {
            return FastAttempt.decline(); // defensive: v2 breaks cycles in-engine, so this is rare
        }

        boolean multi = multiplePaths[0];
        if (plan.feasible()) {
            return FastAttempt.handled(toAe2Plan(output, amount, plan, multi, false, durability));
        }
        // Infeasible at this amount. Only authoritative if the bounded search ran to completion
        // (never hit a per-node cap); otherwise defer to AE2's exhaustive simulator.
        if (plan.budgetExhausted()) {
            return FastAttempt.decline();
        }
        if (!simulate) {
            return FastAttempt.handled(null); // this amount truly fails within our search
        }
        return FastAttempt.handled(toAe2Plan(output, amount, plan, multi, true, durability)); // partial + missing
    }

    /** BFS the reachable recipe graph; returns false to decline the fast path. */
    private static boolean buildGraph(ICraftingService craftingService,
                                      ChildCraftingSimulationState snapshot,
                                      AEKey root,
                                      CraftGraph.Builder<AEKey> builder,
                                      boolean[] multiplePaths,
                                      Map<AEKey, DurabilityChain<AEKey>> durability) {
        Set<AEKey> seen = new HashSet<>();
        Deque<AEKey> queue = new ArrayDeque<>();
        // Memoized "how much is already in the network" per key (SIMULATE probe), used to rank fuzzy
        // substitutes most-available-first so the bounded keep-best-32 picks the cheapest routes.
        Map<AEKey, Long> availability = new HashMap<>();
        seen.add(root);
        queue.add(root);

        while (!queue.isEmpty()) {
            AEKey key = queue.poll();

            // Durability carrier: a finite-use token resource. Its stock (= aggregate uses over the
            // whole chain, 链长×数量) was set once at capture, so we don't re-stock it here — but we DO
            // craft it: a normal tool is craftable, and one crafted (full) tool yields n uses, so the
            // tool's real pattern is registered with its output scaled by n (优先按链长×数量).
            DurabilityChain<AEKey> carrier = durability.get(key);
            long outputScale = 1;
            if (carrier != null) {
                outputScale = carrier.n();
            } else {
                long available = snapshot.extract(key, Long.MAX_VALUE, Actionable.SIMULATE);
                if (available > 0) {
                    builder.stock(key, available);
                }
            }

            // Emitable items (e.g. via emitter cards) are not modeled by the fast path.
            if (craftingService.canEmitFor(key)) {
                return false;
            }

            Collection<IPatternDetails> patterns = craftingService.getCraftingFor(key);
            if (patterns.size() > 1) {
                multiplePaths[0] = true;
            }

            for (IPatternDetails details : patterns) {
                // This pattern is indexed under `key`, so `key` is one of its outputs: that one is the
                // primary for this node; every other output is a byproduct routed through the pool.
                List<GenericStack> outputs = details.getOutputs();
                GenericStack primary = null;
                List<CraftOutput<AEKey>> byproducts = new ArrayList<>(Math.max(0, outputs.size() - 1));
                for (GenericStack out : outputs) {
                    if (primary == null && key.equals(out.what())) {
                        primary = out;
                    } else {
                        byproducts.add(CraftOutput.of(out.what(), out.amount()));
                    }
                }
                if (primary == null) {
                    return false; // defensive: key not actually produced here
                }

                // Per-slot acceptable concrete options for the hard-fuzzy (OR) expansion.
                IPatternDetails.IInput[] inputs = details.getInputs();
                List<List<CraftInput<AEKey>>> slotOptions = new ArrayList<>(inputs.length);
                long combos = 1;
                for (IPatternDetails.IInput in : inputs) {
                    // Durability tool slot: collapse the degradation chain to one finite-use token.
                    DurabilityChain<AEKey> chain = durabilityChain(in, snapshot, builder, durability);
                    if (chain != null) {
                        slotOptions.add(List.of(CraftInput.of(chain.carrier(), Math.max(1, in.getMultiplier()))));
                        continue; // single deterministic option, never enqueued for crafting
                    }
                    GenericStack[] possible = in.getPossibleInputs();
                    List<CraftInput<AEKey>> opts = new ArrayList<>(possible.length);
                    for (GenericStack template : possible) {
                        AEKey inputKey = template.what();
                        boolean catalyst = key.equals(inputKey) || inputKey.matches(primary);
                        // Container/durability-tool: getRemainingKey gives the leftover (empty bucket,
                        // damaged tool). These are returned/degrading "catalyst" inputs whose finite
                        // reuse only AE2's one-at-a-time limitQty (or a future 成环 collapse) models
                        // correctly. We cannot expand them as plain OR options without risking a false
                        // plan, so any pattern that needs one is declined to AE2 — never silently
                        // skipped (that would be a false negative for a recipe AE2 can actually run).
                        if (catalyst || in.getRemainingKey(inputKey) != null) {
                            continue; // drop this concrete option
                        }
                        opts.add(CraftInput.of(inputKey, Sat.mul(template.amount(), in.getMultiplier())));
                    }
                    if (opts.isEmpty()) {
                        return false; // slot only satisfiable by catalyst/container/tool -> defer to AE2
                    }
                    // Rank this slot's substitutes most-available-first. When the full OR-product
                    // overruns the budget we keep only the best `FUZZY_NONCYCLE_STEPS` combinations
                    // (lowest rank-sum), so the cheapest/in-stock routes survive instead of the recipe
                    // being dropped wholesale.
                    if (opts.size() > 1) {
                        for (CraftInput<AEKey> o : opts) {
                            availability.computeIfAbsent(o.key(),
                                k -> Math.max(0L, snapshot.extract(k, Long.MAX_VALUE, Actionable.SIMULATE)));
                        }
                        opts.sort(Comparator.comparingLong(
                            (CraftInput<AEKey> o) -> availability.get(o.key())).reversed());
                    }
                    slotOptions.add(opts);
                    // Saturating product: a raw `combos *= opts.size()` can overflow Long for patterns
                    // with many fuzzy slots over large tags, wrapping to a small value that slips past
                    // the budget check below. Sat.mul clamps so the budget comparison stays correct.
                    combos = Sat.mul(combos, opts.size());
                }
                if (combos > 1) {
                    multiplePaths[0] = true; // fuzzy expanded into competing recipes
                }
                // For a craftable durability tool, one firing makes one full tool = n uses.
                long outAmount = Sat.mul(primary.amount(), outputScale);
                // Keep the best (lowest rank-sum) up to FUZZY_NONCYCLE_STEPS combinations; when the
                // product is within budget this emits all of them, otherwise it greedily keeps the front.
                emitBestCombinations(builder, seen, queue, key, outAmount, byproducts, slotOptions, details);
            }
        }
        return true;
    }

    /**
     * Detect a durability-tool input and capture its degradation chain once, delegating the chain
     * building / reduction to the engine ({@link DurabilityChain}).
     *
     * <p>The only AE2-specific bits are the two lambdas: {@code remaining} follows
     * {@code getRemainingKey} but returns {@code null} when the step leaves the tool's own {@link Item}
     * (a container like a bucket degrades into a <em>different</em> item → not durability), and
     * {@code stock} probes each exact variant's count (so partial tools are counted). The aggregate uses
     * become the carrier's stock in the graph. Returns {@code null} when this slot is not a reducible
     * durability tool (plain item, container, or a chain longer than {@link #FUZZY_CYCLE_STEPS}).
     */
    private static DurabilityChain<AEKey> durabilityChain(IPatternDetails.IInput in,
                                                          ChildCraftingSimulationState snapshot,
                                                          CraftGraph.Builder<AEKey> builder,
                                                          Map<AEKey, DurabilityChain<AEKey>> registry) {
        GenericStack[] possible = in.getPossibleInputs();
        if (possible.length == 0 || !(possible[0].what() instanceof AEItemKey full)) {
            return null;
        }
        DurabilityChain<AEKey> cached = registry.get(full);
        if (cached != null) {
            return cached;
        }

        Item item = full.getItem();
        DurabilityChain<AEKey> chain = DurabilityChain.build(
                full,
                k -> in.getRemainingKey(k) instanceof AEItemKey next && next.getItem() == item ? next : null,
                k -> snapshot.extract(k, Long.MAX_VALUE, Actionable.SIMULATE),
                FUZZY_CYCLE_STEPS);
        if (chain == null) {
            return null;
        }
        registry.put(full, chain);
        builder.stock(full, chain.totalUses()); // carrier stock = aggregate uses (set once)
        return chain;
    }

    /**
     * Emit up to {@link #FUZZY_NONCYCLE_STEPS} {@link CraftPattern}s for the per-slot substitute options,
     * choosing the lowest rank-sum (most-available-first) combinations when the full cartesian product
     * exceeds the budget. All share the same {@code source} {@link IPatternDetails} (so AE2 fires the one
     * real pattern and resolves the fuzzy slot from whatever the plan charged as used); the v2 planner
     * treats them as competing recipes and picks per availability.
     */
    private static void emitBestCombinations(CraftGraph.Builder<AEKey> builder, Set<AEKey> seen, Deque<AEKey> queue,
                                         AEKey key, long outputAmount, List<CraftOutput<AEKey>> byproducts,
                                         List<List<CraftInput<AEKey>>> slotOptions, IPatternDetails source) {
        for (List<CraftInput<AEKey>> coreInputs
                : BoundedCombinations.bestFirst(slotOptions, (int) FUZZY_NONCYCLE_STEPS)) {
            for (CraftInput<AEKey> opt : coreInputs) {
                if (seen.add(opt.key())) {
                    queue.add(opt.key());
                }
            }
            builder.pattern(new CraftPattern<>(key, outputAmount, coreInputs, byproducts, source));
        }
    }

    private static CraftingPlan toAe2Plan(AEKey output, long amount, CraftPlan<AEKey> plan,
                                          boolean multiplePaths, boolean simulation,
                                          Map<AEKey, DurabilityChain<AEKey>> durability) {
        // Several CraftPatterns may share one IPatternDetails (fuzzy combos / multi-output nodes), so
        // accumulate firing counts per real pattern.
        Map<IPatternDetails, Long> patternTimes = new HashMap<>();
        for (Map.Entry<CraftPattern<AEKey>, Long> e : plan.firings().entrySet()) {
            patternTimes.merge((IPatternDetails) e.getKey().source(), e.getValue(), Long::sum);
        }

        KeyCounter usedItems = new KeyCounter();
        for (Map.Entry<AEKey, Long> e : plan.usedStock().entrySet()) {
            DurabilityChain<AEKey> chain = durability.get(e.getKey());
            if (chain == null) {
                usedItems.add(e.getKey(), e.getValue());
            } else {
                // tokens drawn from stock -> real tools, most-degraded first
                chain.chargeFromStock(e.getValue(), usedItems::add);
            }
        }

        KeyCounter missingItems = new KeyCounter();
        for (Map.Entry<AEKey, Long> e : plan.missing().entrySet()) {
            DurabilityChain<AEKey> chain = durability.get(e.getKey());
            if (chain == null) {
                missingItems.add(e.getKey(), e.getValue());
            } else {
                // Missing uses become full tools to craft/supply: ceil(uses / n).
                missingItems.add(chain.carrier(), Sat.ceilDiv(e.getValue(), chain.n()));
            }
        }

        long bytes = computeBytes(plan, durability);

        return new CraftingPlan(
                new GenericStack(output, amount),
                bytes,
                simulation,
                multiplePaths,
                usedItems,
                new KeyCounter(), // emittedItems: none (emit declines the fast path)
                missingItems,
                patternTimes);
    }

    /**
     * Reproduces AE2's byte total: {@code addStackBytes} per requested node
     * ({@code items / amountPerByte * 8}), plus one byte per pattern firing, plus {@code 8 * nodeCount}.
     */
    private static long computeBytes(CraftPlan<AEKey> plan, Map<AEKey, DurabilityChain<AEKey>> durability) {
        double bytes = 0;
        for (Map.Entry<AEKey, Long> e : plan.grossDemand().entrySet()) {
            int amountPerByte = Math.max(1, e.getKey().getType().getAmountPerByte());
            DurabilityChain<AEKey> chain = durability.get(e.getKey());
            // Carrier demand is in uses; AE2 bytes count tools, so collapse uses -> tools (ceil/n).
            long amt = chain == null ? e.getValue() : Sat.ceilDiv(e.getValue(), chain.n());
            bytes += (double) amt / amountPerByte * 8.0;
        }
        for (long times : plan.firings().values()) {
            bytes += times;
        }
        bytes += 8.0 * plan.grossDemand().size();
        return (long) Math.ceil(bytes);
    }
}
