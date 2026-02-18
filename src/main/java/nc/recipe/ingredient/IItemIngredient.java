package nc.recipe.ingredient;

import nc.util.StackHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public interface IItemIngredient extends IIngredient<ItemStack> {
	
	@Override
	default ItemStack getNextStack(int ingredientNumber) {
		ItemStack nextStack = getStack();
		nextStack.setCount(getNextStackSize(ingredientNumber));
		return nextStack;
	}
	
	@Override
	default List<ItemStack> getInputStackHashingList() {
		List<ItemStack> list = new ArrayList<>();
		for (ItemStack stack : getInputStackList()) {
			int meta = StackHelper.getMetadata(stack);
			if (stack != null && !stack.isEmpty() && meta == OreDictionary.WILDCARD_VALUE) {
				NonNullList<ItemStack> subStacks = new NonNullList<>(new ArrayList<>(), ItemStack.EMPTY);
				Item item = stack.getItem();
				if (item instanceof ItemBlock) {
					for (int i = 0; i < 16; ++i) {
						subStacks.add(new ItemStack(item, stack.getCount(), i));
					}
				}
				else {
					stack.getItem().getSubItems(CreativeTabs.SEARCH, subStacks);
				}
				list.addAll(subStacks);
			}
			else {
				list.add(stack);
			}
		}
		return list;
	}
	
	@Override
	IItemIngredient getFactoredIngredient(int factor);
}
