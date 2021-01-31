# Storing items in a block as an Inventory

Make sure you've [made a block entity](../Modding-Tutorials/Blocks-and-Block-Entities/blockentity.md) before
reading this tutorial.

The standard way to store items in a BlockEntity is to make it an
`Inventory`. This allows hoppers (or other mods) to insert and extract
items from your BlockEntity without any extra work.

## Implementing Inventory

`Inventory` is just an interface, which means the actual `ItemStack`
state will need to be stored on your `BlockEntity`. A
`DefaultedList<ItemStack>` can be used as an easy way to store these
`ItemStacks`, as it can be set to default to `ItemStack.Empty`, which is
the proper way of saying that there is no item in a slot. Implementing
`Inventory` is fairly simple, but is tedious and prone to error, so
we'll use a default implementation of it which only requires giving it a
`DefaultList<ItemStack>` (copy this as a new file):

```java
/**
 * A simple {@code Inventory} implementation with only default methods + an item list getter.
 *
 * Originally by Juuz
 */
public interface ImplementedInventory extends Inventory {

    /**
     * Retrieves the item list of this inventory.
     * Must return the same instance every time it's called.
     */
    DefaultedList<ItemStack> getItems();
    
    /**
     * Creates an inventory from the item list.
     */
    static ImplementedInventory of(DefaultedList<ItemStack> items) {
        return () -> items;
    }
    
    /**
     * Creates a new inventory with the specified size.
     */
    static ImplementedInventory ofSize(int size) {
        return of(DefaultedList.ofSize(size, ItemStack.EMPTY));
    }
    
    /**
     * Returns the inventory size.
     */
    @Override
    default int size() {
        return getItems().size();
    }
    
    /**
     * Checks if the inventory is empty.
     * @return true if this inventory has only empty stacks, false otherwise.
     */
    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Retrieves the item in the slot.
     */
    @Override
    default ItemStack getStack(int slot) {
        return getItems().get(slot);
    }
    
    /**
     * Removes items from an inventory slot.
     * @param slot  The slot to remove from.
     * @param count How many items to remove. If there are less items in the slot than what are requested,
     *              takes all items in that slot.
     */
    @Override
    default ItemStack removeStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }
    
    /**
     * Removes all items from an inventory slot.
     * @param slot The slot to remove from.
     */
    @Override
    default ItemStack removeStack(int slot) {
        return Inventories.removeStack(getItems(), slot);
    }
    
    /**
     * Replaces the current stack in an inventory slot with the provided stack.
     * @param slot  The inventory slot of which to replace the itemstack.
     * @param stack The replacing itemstack. If the stack is too big for
     *              this inventory ({@link Inventory#getMaxCountPerStack()}),
     *              it gets resized to this inventory's maximum amount.
     */
    @Override
    default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
    }
    
    /**
     * Clears the inventory.
     */
    @Override
    default void clear() {
        getItems().clear();
    }
    
    /**
     * Marks the state as dirty.
     * Must be called after changes in the inventory, so that the game can properly save
     * the inventory contents and notify neighboring blocks of inventory changes.
     */ 
    @Override
    default void markDirty() {
        // Override if you want behavior.
    }
    
    /**
     * @return true if the player can use the inventory, false otherwise.
     */ 
    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }
}
```

Now in your `BlockEntity` Implement `ImplementedInventory`, and provide
it with an instance of `DefaultedList<ItemStack> items` that stores the
items. For this example we'll store a maximum of 2 items in the
inventory:

```java
public class DemoBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }
    [...]

}

```

We're also gonna need to save the inventories to tag and load it from
there. `Inventories` has helper methods that makes this very easy:

```java
public class DemoBlockEntity extends BlockEntity implements ImplementedInventory {
    [...]
    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        Inventories.fromTag(tag,items);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        Inventories.toTag(tag,items);
        return super.toTag(tag);
    }
}
```

## Extracting and inserting from your inventory (or any inventory)

In our block class, we'll override the \`onUse\` behavior to insert and
extract items from our inventory. Note that this can be done to any
`Inventory` instance, not just our own (so you could do the same thing
to a chest block, for example). First we'll handle inserting into the
inventory. The player will insert the item he is holding if he is
holding one. It'll go into the first slot if it is empty, or to the
second slot if the first one is empty, or if the second is empty too
we'll print some information about the inventory. Note that we call
`copy()` when inserting the `ItemStack` into the inventory so it doesn't
get destroyed alongside the player's `ItemStack`.

```java
public class ExampleBlock extends Block implements BlockEntityProvider {
    [...]
    @Override
    public boolean onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
        if (world.isClient) return true;
        Inventory blockEntity = (Inventory) world.getBlockEntity(blockPos);


        if (!player.getStackInHand(hand).isEmpty()) {
            // Check what is the first open slot and put an item from the player's hand there
            if (blockEntity.getStack(0).isEmpty()) {
                // Put the stack the player is holding into the inventory
                blockEntity.setStack(0, player.getStackInHand(hand).copy());
                // Remove the stack from the player's hand
                player.getStackInHand(hand).setCount(0);
            } else if (blockEntity.getStack(1).isEmpty()) {
                blockEntity.setStack(1, player.getStackInHand(hand).copy());
                player.getStackInHand(hand).setCount(0);
            } else {
                // If the inventory is full we'll print it's contents
                System.out.println("The first slot holds "
                        + blockEntity.getStack(0) + " and the second slot holds " + blockEntity.getStack(1));
            }
        } 
        return true;
    }
}
```

We'll have the opposite behavior when the player is not holding an item.
We'll take the item from the second slot, and then the first one of the
second is empty. If the first is empty as well we won't do anything.

```java
public class ExampleBlock extends Block implements BlockEntityProvider {
    [...]
    @Override
    public boolean onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
        ...
        if (!player.getStackInHand(hand).isEmpty()) {
            ...
        } else {
            // If the player is not holding anything we'll get give him the items in the block entity one by one

             // Find the first slot that has an item and give it to the player
            if (!blockEntity.getStack(1).isEmpty()) {
                // Give the player the stack in the inventory
                player.inventory.offerOrDrop(world, blockEntity.getStack(1));
                // Remove the stack from the inventory
                blockEntity.removeStack(1);
            } else if (!blockEntity.getStack(0).isEmpty()) {
                player.inventory.offerOrDrop(world, blockEntity.getStack(0));
                blockEntity.removeStack(0);
            }
        }
        
        return true;
    }
}
```

## Implementing SidedInventory

If you want to have different logic based on what side things (hopper or
other mods) interact with your block you need to implement
`SidedInventory`. If say you wanted to make it so you cannot insert from
the upper side of the block, you would do this:

```java
public class DemoBlockEntity extends BlockEntity implements ImplementedInventory, SidedInventory {
    [...]
    @Override
    public int[] getInvAvailableSlots(Direction var1) {
        // Just return an array of all slots
        int[] result = new int[getItems().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }

        return result;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        return direction != Direction.UP;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        return true;
    }
}

```

