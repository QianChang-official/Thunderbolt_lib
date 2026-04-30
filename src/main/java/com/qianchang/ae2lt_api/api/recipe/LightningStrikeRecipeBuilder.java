package com.qianchang.ae2lt_api.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for <strong>Lightning Strike</strong> recipes
 * ({@code ae2lt:lightning_strike}).
 *
 * <p>These rituals are triggered by lightning rods. The recipe center is the
 * block directly below the rod, and the requirement list describes the
 * surrounding structure around that center block.</p>
 */
public final class LightningStrikeRecipeBuilder {

    private boolean requiresNaturalLightning;
    private String centerInput;
    private String centerOutput;
    private final List<Requirement> requirements = new ArrayList<>();

    private LightningStrikeRecipeBuilder() {
    }

    public static LightningStrikeRecipeBuilder create() {
        return new LightningStrikeRecipeBuilder();
    }

    public LightningStrikeRecipeBuilder requiresNaturalLightning(boolean requiresNaturalLightning) {
        this.requiresNaturalLightning = requiresNaturalLightning;
        return this;
    }

    public LightningStrikeRecipeBuilder center(String centerInput, String centerOutput) {
        this.centerInput = Objects.requireNonNull(centerInput, "centerInput");
        this.centerOutput = Objects.requireNonNull(centerOutput, "centerOutput");
        return this;
    }

    public LightningStrikeRecipeBuilder requirement(int x, int y, int z, String blockId, boolean consume) {
        requirements.add(new Requirement(x, y, z, Objects.requireNonNull(blockId, "blockId"), consume));
        return this;
    }

    public JsonObject toJson() {
        if (centerInput == null) {
            throw new IllegalStateException("A center input block must be set");
        }
        if (centerOutput == null) {
            throw new IllegalStateException("A center output block must be set");
        }
        if (requirements.isEmpty()) {
            throw new IllegalStateException("At least one surrounding requirement is required");
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", AE2LTRecipeTypes.LIGHTNING_STRIKE);
        json.addProperty("requires_natural_lightning", requiresNaturalLightning);
        json.addProperty("center_input", centerInput);
        json.addProperty("center_output", centerOutput);

        JsonArray requirementArray = new JsonArray();
        for (Requirement requirement : requirements) {
            requirementArray.add(requirement.toJson());
        }
        json.add("requirements", requirementArray);

        return json;
    }

    private record Requirement(int x, int y, int z, String blockId, boolean consume) {
        JsonObject toJson() {
            JsonObject json = new JsonObject();

            JsonArray offset = new JsonArray();
            offset.add(x);
            offset.add(y);
            offset.add(z);

            json.add("offset", offset);
            json.addProperty("block", blockId);
            json.addProperty("consume", consume);
            return json;
        }
    }
}
