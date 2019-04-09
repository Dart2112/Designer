package org.mineacademy.designer.tool;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.designer.model.ItemCreator;
import org.mineacademy.remain.util.RemainUtils;

/**
 * Represents a tool. A tool is a simple ItemStack that is registered within the
 * plugin and fires automatic events
 */
public abstract class Tool {

	/**
	 * Create a new tool
	 */
	protected Tool() {

		// A hacky way of automatically registering it AFTER the parent constructor, assuming all went okay
		new Thread() {

			@Override
			public void run() {

				try {
					Thread.sleep(3);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				if (!ToolRegistry.isRegistered(Tool.this))
					ToolRegistry.register(Tool.this);
			}
		}.start();
	}

	/**
	 * Get the tool item
	 *
	 * TIP: Use {@link ItemCreator}
	 *
	 * @return the tool item
	 */
	public abstract ItemStack getItem();

	/**
	 * Called automatically when the tool is clicked
	 *
	 * @param event the event
	 */
	public abstract void onBlockClick(PlayerInteractEvent event);

	/**
	 * Called when the player swap items in their hotbar and the new slot matches
	 * this tool.
	 *
	 * @param player the player
	 */
	public void onHotbarFocused(Player player) {
	}

	/**
	 * Called when the player the tool is out of focus at hotbar
	 *
	 * @param player the player
	 */
	public void onHotbarDefocused(Player player) {
	}

	/**
	 * Evaluates the given itemstack whether it is this tool
	 *
	 * @param item the itemstack
	 * @return true if this tool is the given itemstack
	 */
	public boolean isTool(ItemStack item) {
		return RemainUtils.isSimilar(getItem(), item);
	}

	/**
	 * Should we fire {@link #onBlockClick(PlayerInteractEvent)} even on cancelled
	 * events?
	 *
	 * True by default. Set to false if you want to catch clicking air.
	 *
	 * @return true if we should ignore the click event if it was cancelled
	 */
	public boolean ignoreCancelled() {
		return true;
	}

	/**
	 * A convenience method, should we automatically cancel the
	 * {@link PlayerInteractEvent} ?
	 *
	 * @return true if the interact event should be cancelled automatically false by
	 *         default
	 */
	public boolean autoCancel() {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Tool && ((Tool) obj).getItem().equals(getItem());
	}
}
