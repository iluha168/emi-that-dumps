package dev.emi.emi.stack.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.registry.EmiIngredientSerializers;

public class ListEmiIngredientSerializer implements EmiIngredientSerializer<ListEmiIngredient> {

	@Override
	public String getType() {
		return "list";
	}

	@Override
	public EmiIngredient deserialize(JsonElement element) {
		throw new UnsupportedOperationException("Unimplemented method 'deserialize'");
	}

	@Override
	public JsonElement serialize(ListEmiIngredient stack) {
		JsonObject jsonObject = new JsonObject();
		if (stack.getAmount() != 1)
			jsonObject.addProperty("amount", stack.getAmount());
		if (stack.getChance() != 1)
			jsonObject.addProperty("chance", stack.getChance());
		{
			JsonArray stackArray = new JsonArray();
			for (EmiStack child : stack.getEmiStacks())
				stackArray.add(EmiIngredientSerializers.serialize(child));
			jsonObject.add("stacks", stackArray);
		}
		return jsonObject;
	}
}
