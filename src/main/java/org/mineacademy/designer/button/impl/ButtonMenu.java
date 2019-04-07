package org.mineacademy.designer.button.impl;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.designer.button.Button;
import org.mineacademy.designer.menu.Menu;
import org.mineacademy.designer.model.ItemCreator;
import org.mineacademy.remain.model.CompMaterial;
import org.mineacademy.remain.util.ReflectionUtil;

import lombok.Getter;

/**
 * A button that opens another menu
 */
public final class ButtonMenu extends Button {

	/**
	 * Sometimes you need to allocate data when you create the button,
	 * but these data are not yet available when you make new instance of this button
	 *
	 * Use this helper to set them right before showing the button
	 */
	private final MenuLateBind menuLateBind;

	/**
	 * The menu this button opens
	 */
	private final Menu menuToOpen;

	/**
	 * The icon of this button
	 */
	@Getter
	private final ItemStack item;

	/**
	 * Create a new button that triggers another menu
	 *
	 * @param menuClass
	 * @param material
	 * @param name
	 * @param lore
	 */
	public ButtonMenu(Class<? extends Menu> menuClass, CompMaterial material, String name, String... lore) {
		this(menuClass, ItemCreator.of(material, name, lore));
	}

	/**
	 * Create a new button that triggers another menu
	 *
	 * @param menuClass
	 * @param material
	 * @param name
	 * @param lore
	 */
	public ButtonMenu(Class<? extends Menu> menuClass, Material material, String name, String... lore) {
		this(menuClass, ItemCreator.of(material, name, lore));
	}

	/**
	 * Create a new button that triggers another menu
	 *
	 * @param menuClass
	 * @param item
	 */
	public ButtonMenu(Class<? extends Menu> menuClass, ItemCreator.ItemCreatorBuilder item) {
		this(null, () -> ReflectionUtil.instatiate(menuClass), item.hideTags(true).build().make());
	}

	/**
	 * Create a new button that triggers another menu
	 *
	 * @param menuLateBind
	 * @param item
	 */
	public ButtonMenu(MenuLateBind menuLateBind, ItemCreator.ItemCreatorBuilder item) {
		this(null, menuLateBind, item.hideTags(true).build().make());
	}

	/**
	 * Create a new button that triggers another menu
	 *
	 * @param menuLateBind
	 * @param item
	 */
	public ButtonMenu(MenuLateBind menuLateBind, ItemStack item) {
		this(null, menuLateBind, item);
	}

	/**
	 * Create a new button that triggers another menu
	 *
	 * @param menu
	 * @param material
	 * @param name
	 * @param lore
	 */
	public ButtonMenu(Menu menu, CompMaterial material, String name, String... lore) {
		this(menu, ItemCreator.of(material, name, lore));
	}

	/**
	 * Create a new button that triggers another menu
	 *
	 * @param menu
	 * @param material
	 * @param name
	 * @param lore
	 */
	public ButtonMenu(Menu menu, Material material, String name, String... lore) {
		this(menu, ItemCreator.of(material, name, lore));
	}

	/**
	 * Create a new button that triggers another menu
	 *
	 * @param menu
	 * @param item
	 */
	public ButtonMenu(Menu menu, ItemCreator.ItemCreatorBuilder item) {
		this(menu, null, item.hideTags(true).build().make());
	}

	/**
	 * Create a new button that triggers another menu
	 *
	 * @param menu
	 * @param item
	 */
	public ButtonMenu(Menu menu, ItemStack item) {
		this(menu, null, item);
	}

	// Private constructor
	private ButtonMenu(Menu menuToOpen, MenuLateBind menuLateBind, ItemStack item) {
		this.menuToOpen = menuToOpen;
		this.menuLateBind = menuLateBind;
		this.item = item;
	}

	/**
	 * Automatically display another menu when the button is clicked
	 */
	@Override
	public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
		if (menuLateBind != null)
			menuLateBind.getMenu().displayTo(pl);

		else {
			Objects.requireNonNull(menuToOpen, "Report / ButtonTrigger requires either 'late bind menu' or normal menu to be set!");

			menuToOpen.displayTo(pl);
		}
	}
}