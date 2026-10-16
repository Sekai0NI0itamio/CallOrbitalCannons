package com.callorbitalcannons.recipe;

import com.callorbitalcannons.geometry.RecipeMatcher;
import com.callorbitalcannons.item.OrbitalRod;
import com.callorbitalcannons.item.WeaponMode;
import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/**
 * 3x3 crafting recipe that forges one weapon rod from one ordinary fishing
 * rod plus one full 64-TNT stack. Orientation matters: TNT on top makes the
 * Orbital rod, TNT on the bottom makes the Stab rod; the two shapes never
 * cross-match and mirroring is not allowed.
 *
 * <p>Vanilla crafting consumes the whole input stacks, so all 64 TNT and the
 * rod are eaten and exactly one weapon rod is produced. Weapon rods are
 * rejected as the rod ingredient, and short TNT stacks never match.
 */
public class WeaponRodRecipe extends SpecialCraftingRecipe {
    private final WeaponMode mode;

    public WeaponRodRecipe(WeaponMode mode, CraftingRecipeCategory category) {
        super(category);
        this.mode = mode;
    }

    public WeaponMode mode() {
        return mode;
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getWidth() != 3 || input.getHeight() != 3) {
            return false;
        }
        return toMatch(input) == expected();
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return OrbitalRod.create(mode);
    }

    @Override
    public boolean fits(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return OrbitalRod.create(mode);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return mode == WeaponMode.NUKE ? Serializers.ORBITAL : Serializers.STAB;
    }

    private RecipeMatcher.Match toMatch(CraftingRecipeInput input) {
        String[] ids = new String[9];
        int[] counts = new int[9];
        for (int i = 0; i < 9; i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                ids[i] = "empty";
            } else if (stack.isOf(Items.TNT)) {
                ids[i] = "tnt";
            } else if (stack.isOf(Items.FISHING_ROD) && !OrbitalRod.isWeapon(stack)) {
                ids[i] = "rod";
            } else {
                ids[i] = "other";
            }
            counts[i] = stack.getCount();
        }
        return RecipeMatcher.match(ids, counts);
    }

    private RecipeMatcher.Match expected() {
        return mode == WeaponMode.NUKE ? RecipeMatcher.Match.ORBITAL : RecipeMatcher.Match.STAB;
    }

    /** Serializer holders, registered by the mod initializer. */
    public static final class Serializers {
        private Serializers() {
        }

        public static final RecipeSerializer<WeaponRodRecipe> ORBITAL = serializerFor(WeaponMode.NUKE);
        public static final RecipeSerializer<WeaponRodRecipe> STAB = serializerFor(WeaponMode.STAB);

        private static RecipeSerializer<WeaponRodRecipe> serializerFor(WeaponMode mode) {
            final MapCodec<WeaponRodRecipe> codec =
                    MapCodec.unit(() -> new WeaponRodRecipe(mode, CraftingRecipeCategory.MISC));
            final PacketCodec<RegistryByteBuf, WeaponRodRecipe> packetCodec =
                    PacketCodec.unit(new WeaponRodRecipe(mode, CraftingRecipeCategory.MISC));
            return new RecipeSerializer<WeaponRodRecipe>() {
                @Override
                public MapCodec<WeaponRodRecipe> codec() {
                    return codec;
                }

                @Override
                public PacketCodec<RegistryByteBuf, WeaponRodRecipe> packetCodec() {
                    return packetCodec;
                }
            };
        }
    }
}
