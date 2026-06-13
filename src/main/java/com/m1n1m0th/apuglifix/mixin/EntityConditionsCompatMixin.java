package com.m1n1m0th.apuglifix.mixin;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Predicate;

/**
 * Apugli's @Redirect targets lambda$register$47, but in Apoli 2.9.2 the
 * enchantment calls it expects are in lambda$register$45 instead.
 * This overwrite preserves the original method and adds no-op call sites
 * so Apugli's redirect has something to grab. Priority 100 < Apugli's 1000.
 */
@Mixin(value = io.github.apace100.apoli.power.factory.condition.EntityConditions.class, priority = 100)
public class EntityConditionsCompatMixin {

    /**
     * @author m1n1m0th
     * @reason Add missing EnchantmentHelper call sites for Apugli redirect compatibility.
     */
    @Overwrite
    private static Boolean lambda$register$47(SerializableData.Instance data, Entity entity) {
        // Original logic: entity_on_block bientity condition
        if (entity.hasVehicle()) {
            if (data.isPresent("bientity_condition")) {
                @SuppressWarnings("unchecked")
                Predicate<Pair<Entity, Entity>> predicate =
                        (Predicate<Pair<Entity, Entity>>) data.get("bientity_condition");
                Entity vehicle = entity.getVehicle();
                return predicate.test(Pair.of(entity, vehicle));
            }
            return true;
        }

        // No-op calls for Apugli's @Redirect to intercept
        if (entity instanceof LivingEntity livingEntity) {
            EnchantmentHelper.getLevel(null, ItemStack.EMPTY);
            EnchantmentHelper.getEquipmentLevel(null, livingEntity);
        }

        return false;
    }
}
