/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.jei.category;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import net.minecraftforge.fluids.FluidStack;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.dries007.tfc.common.recipes.SimplePotRecipe;
import net.dries007.tfc.common.recipes.PotRecipe;

public class SimplePotRecipeCategory extends PotRecipeCategory<PotRecipe>
{
    private static final int[] INPUT_X = {15, 5, 25, 5, 25};
    private static final int[] INPUT_Y = {5, 25, 25, 45, 45};
    private static final int[] OUTPUT_X = {75, 65, 85, 65, 85};
    private static final int[] OUTPUT_Y = {5, 25, 25, 45, 45};

    public SimplePotRecipeCategory(RecipeType<PotRecipe> type, IGuiHelper helper)
    {
        super(type, helper, helper.createBlankDrawable(110, 100));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PotRecipe potRecipe, IFocusGroup focuses)
    {
        SimplePotRecipe recipe = (SimplePotRecipe) potRecipe;
        int i = 0;
        for (Ingredient ingredient : recipe.getItemIngredients())
        {
            if (!ingredient.isEmpty())
            {
                IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X[i] + 1, INPUT_Y[i] + 1);
                input.addIngredients(ingredient);
                i++;
            }
        }

        final List<FluidStack> inputFluids = collapse(recipe.getFluidIngredient());
        if (!inputFluids.isEmpty())
        {
            IRecipeSlotBuilder fluidOutput = builder.addSlot(RecipeIngredientRole.OUTPUT, 16, 66);
            fluidOutput.addIngredients(VanillaTypes.FLUID, inputFluids);
            fluidOutput.setFluidRenderer(1, false, 16, 16);
        }

        int j = 0;
        for (ItemStack stack : recipe.getOutputStacks())
        {
            if (!stack.isEmpty())
            {
                IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X[j] + 1, OUTPUT_Y[j] + 1);
                output.addItemStack(stack);
                j++;
            }
        }

        final FluidStack outputFluid = recipe.getDisplayFluid();
        if (!outputFluid.isEmpty())
        {
            IRecipeSlotBuilder fluidOutput = builder.addSlot(RecipeIngredientRole.OUTPUT, 16, 66);
            fluidOutput.addIngredient(VanillaTypes.FLUID, outputFluid);
            fluidOutput.setFluidRenderer(1, false, 16, 16);
        }
    }

    /**             y
     *  X      X    5
     * X X -> X X   25
     * X X    X X   45
     *  X      X    65
     */
    @Override
    public void draw(PotRecipe recipe, IRecipeSlotsView recipeSlots, PoseStack stack, double mouseX, double mouseY)
    {
        // Item Input
        drawFive(stack, INPUT_X, INPUT_Y);
        // Water Input
        slot.draw(stack, 15, 65);
        // fire
        fire.draw(stack, 47, 45);
        fireAnimated.draw(stack, 47, 45);
        // Item Output
        drawFive(stack, OUTPUT_X, OUTPUT_Y);
        // Water Output
        slot.draw(stack, 75, 65);
    }

    private void drawFive(PoseStack stack, int[] x, int[] y)
    {
        slot.draw(stack, x[0], y[0]);
        slot.draw(stack, x[1], y[1]);
        slot.draw(stack, x[2], y[2]);
        slot.draw(stack, x[3], y[3]);
        slot.draw(stack, x[4], y[4]);
    }
}
