package dev.emi.emi.stack.serializer;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmptyEmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class EmptyEmiStackSerializer implements EmiStackSerializer<EmptyEmiStack> {

	@Override
	public String getType() {
		return "empty";
	}

	@Override
	public EmiStack create(Identifier id, NbtCompound nbt, long amount) {
		return EmiStack.EMPTY;
	}
}
