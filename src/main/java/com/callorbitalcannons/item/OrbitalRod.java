package com.callorbitalcannons.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Factory and validator for the two special fishing rods.
 *
 * <p>The weapon mode lives in the {@code CUSTOM_DATA} component under the
 * {@code CallOrbitalRod}/{@code Weapon} keys. Display names and the
 * enchantment glint are cosmetic only: validation checks item data, never
 * the name, so a renamed ordinary rod is always rejected.
 *
 * <p>Rods are struck with a single durability point, so the first survival
 * use breaks them.
 */
public final class OrbitalRod {
    private OrbitalRod() {
    }

    public static final String ROD_KEY = "CallOrbitalRod";
    public static final String WEAPON_KEY = "Weapon";

    /** Builds one single-use weapon rod of {@code mode}. */
    public static ItemStack create(WeaponMode mode) {
        ItemStack rod = new ItemStack(Items.FISHING_ROD);
        rod.setDamageValue(rod.getMaxDamage() - 1);
        rod.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal(mode.displayName()).formatted(Formatting.AQUA).formatted(Formatting.ITALIC));
        rod.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean(ROD_KEY, true);
        nbt.putString(WEAPON_KEY, mode.id());
        rod.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return rod;
    }

    /** True only for fishing rods carrying this mod's weapon data. */
    public static boolean isWeapon(ItemStack stack) {
        return modeOf(stack) != null;
    }

    /** Reads the stored weapon mode, or null for any non-weapon stack. */
    public static WeaponMode modeOf(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isOf(Items.FISHING_ROD)) {
            return null;
        }
        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        NbtCompound nbt = data.getNbt();
        if (nbt == null || !nbt.getBoolean(ROD_KEY)) {
            return null;
        }
        return WeaponMode.fromId(nbt.getString(WEAPON_KEY));
    }
}
