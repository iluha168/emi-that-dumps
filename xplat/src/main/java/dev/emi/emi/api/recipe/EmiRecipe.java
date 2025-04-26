package dev.emi.emi.api.recipe;

import java.lang.reflect.Type;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.ButtonWidget;
import dev.emi.emi.api.widget.DrawableWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.TooltipWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.JemiRecipe.JemiWidget;
import dev.emi.emi.widget.RecipeButtonWidget;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

public interface EmiRecipe {

	/**
	 * @return The recipe category this recipe should be displayed under.
	 *  This is used for grouping in the recipe screen, as well as category display in the recipe tree.
	 */
	EmiRecipeCategory getCategory();

	/**
	 * IDs should be standard formatting (minecraft:lime_dye_from_smelting) only if they uniquely represent a data driven json recipe.
	 * If a mod wants to represent a vanilla recipe with different processing, it cannot reuse the vanilla ID.
	 * For example, a custom machine's recipe crafting an iron pickaxe cannot use "minecraft:iron_pickaxe".
	 * If a recipe does not have a normal unique ID, it should use a synthetic ID.
	 * Synthetic IDs are formatted "namespace:/path" with a "/" at the start of the path.
	 * Commonly, synthetic IDs will be formatted "mymod:/my_process/unique_name".
	 * @return The unique ID of the recipe, or null. If null, the recipe cannot be serialized.
	 */
	@Nullable Identifier getId();
	
	/**
	 * @return A list of ingredients required for the recipe.
	 * 	Inputs will consider this recipe a use when exploring recipes.
	 */
	List<EmiIngredient> getInputs();
	
	/**
	 * @return A list of ingredients associated with the creation of the recipe.
	 * 	Catalysts are considered the same as workstations in the recipe, not broken down as a requirement.
	 * 	However, catalysts will consider this recipe a use when exploring recipes.
	 */
	default List<EmiIngredient> getCatalysts() {
		return List.of();
	}

	/**
	 * @return A list of stacks that are created after a craft.
	 * 	Outputs will consider this recipe a source when exploring recipes.
	 */
	List<EmiStack> getOutputs();

	/**
	 * @return The width taken up by the recipe's widgets
	 *  EMI will grow to accomodate requested width.
	 *  To fit within the default width, recipes should request a width of 134.
	 *  If a recipe does not support the recipe tree or recipe filling, EMI
	 * 	will not need to add buttons, and it will have space for a width of 160.
	 */
	int getDisplayWidth();

	/**
	 * @return The maximum height taken up by the recipe's widgets.
	 * 	Vertical screen space is capped, however, and EMI may opt to provide less vertical space.
	 * 
	 * @see {@link WidgetHolder#getHeight()} when adding widgets for the EMI adjusted height.
	 */
	int getDisplayHeight();

	/**
	 * Called to add widgets that display the recipe.
	 * Can be used in several places, including the main recipe screen, and tooltips.
	 * It is worth noting that EMI cannot grow vertically, so recipes with large heights
	 * may be provided less space than requested if they span more than the entire vertical
	 * space available in the recipe scren.
	 * In the case of very large heights, recipes should respect {@link WidgetHolder#getHeight()}.
	 */
	void addWidgets(WidgetHolder widgets);

	/**
	 * @return Whether the recipe supports the recipe tree.
	 * 	Recipes that do not represent a set of inputs producing a set of outputs should exclude themselves.
	 *  Example for unsupportable recipes are pattern based recipes, like arbitrary dying.
	 */
	default boolean supportsRecipeTree() {
		return !getInputs().isEmpty() && !getOutputs().isEmpty();
	}

	/**
	 * @return Whether the recipe should be hidden from the craftable menu.
	 *  This is desirable behavior for recipes that are reimplementations of vanilla recipes in other workstations.
	 */
	default boolean hideCraftable() {
		return false;
	}

	/**
	 * @return The vanilla {@link Recipe} this recipe represents, if any.
	 *  By default, uses the result of {@link EmiRecipe#getId()} to look up in the RecipeManager.
	 */
	default @Nullable Recipe<?> getBackingRecipe() {
		return EmiPort.getRecipe(getId());
	}

	class Serializer implements JsonSerializer<EmiRecipe> {
		@Override
		public JsonElement serialize(EmiRecipe recipe, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			if (recipe.getId() != null)
				jsonObject.addProperty("id", recipe.getId().toString());
			Type ingredientListType = new TypeToken<List<EmiIngredient>>(){}.getType();
			jsonObject.add("inputs", context.serialize(recipe.getInputs(), ingredientListType));
			jsonObject.add("catalysts", context.serialize(recipe.getCatalysts(), ingredientListType));
			Type stackListType = new TypeToken<List<EmiStack>>(){}.getType();
			jsonObject.add("outputs", context.serialize(recipe.getOutputs(), stackListType));
			JsonArray jsonTexts = new JsonArray();
			recipe.addWidgets(new WidgetHolder() {
				@Override public int getWidth() { return 10000000; }
				@Override public int getHeight() { return 10000000; }

				@Override
				public <T extends Widget> T add(T widget) {
					if (widget instanceof TextWidget textWidget) {
						StringBuilder textBuilder = new StringBuilder();
						textWidget.text.accept((i, s, c) -> {
							textBuilder.appendCodePoint(c);
							return true;
						});
						jsonTexts.add(textBuilder.toString());
					} else if (widget instanceof SlotWidget
							|| widget instanceof RecipeButtonWidget
							|| widget instanceof TextureWidget
							|| widget instanceof JemiWidget
							|| widget instanceof TooltipWidget
							|| widget.getClass() == ButtonWidget.class
							|| widget.getClass() == DrawableWidget.class
					) {} else {
						// EmiLog.info("Recipe "+recipe.getId()+" has added an unknown widget: "+widget.toString());
					}
					return widget;
				}
				
			});
			if (!jsonTexts.isEmpty())
				jsonObject.add("texts", jsonTexts);
			return jsonObject;
		}
	}
}
