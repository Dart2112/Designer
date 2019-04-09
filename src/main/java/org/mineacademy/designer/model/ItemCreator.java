package org.mineacademy.designer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.mineacademy.designer.button.Button;
import org.mineacademy.designer.button.Button.DummyButton;
import org.mineacademy.remain.Remain;
import org.mineacademy.remain.model.CompDye;
import org.mineacademy.remain.model.CompItemFlag;
import org.mineacademy.remain.model.CompMaterial;
import org.mineacademy.remain.model.CompMonsterEgg;
import org.mineacademy.remain.model.CompProperty;
import org.mineacademy.remain.nbt.NBTItem;
import org.mineacademy.remain.util.RemainUtils;
import org.mineacademy.remain.util.MinecraftVersion;
import org.mineacademy.remain.util.MinecraftVersion.V;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

/**
 * Our core class for easy and comfortable item creation.
 *
 * You can use this to make named items with incredible speed and quality.
 */
@Builder
public class ItemCreator {

	/**
	 * The initial item stack
	 */
	private final ItemStack item;

	/**
	 * The initial material
	 */
	private final CompMaterial material;

	/**
	 * The amount of the item
	 */
	@Builder.Default private final int amount = 1;

	/**
	 * The item damage
	 */
	@Builder.Default private final short damage = 0;

	/**
	 * The item name, colors are replaced
	 */
	private final String name;

	/**
	 * The lore for this item, colors are replaced
	 */
	@Singular private final List<String> lores;

	/**
	 * The enchants applied for the item
	 */
	@Singular private final List<UIEnchant> enchants;

	/**
	 * The item flags
	 */
	@Singular private List<CompItemFlag> flags;

	/**
	 * Is the item unbreakable?
	 */
	private Boolean unbreakable;

	/**
	 * The dye color in case your item is compatible
	 */
	private final CompDye color;

	/**
	 * Should we hide all tags from the item (enchants, etc.)?
	 */
	private final boolean hideTags;

	/**
	 * Should we add glow to the item? (adds a fake enchant and uses {@link ItemFlag} to hide it)
	 *
	 * The enchant is visible on older MC versions.
	 */
	private final boolean glow;

	/**
	 * The skull owner, in case it applies
	 */
	private final String skullOwner;

	/**
	 * The internal NBT tag, permanent
	 *
	 * This will be stored at the key "Your plugin name + _Item"
	 * @deprecated use with caution
	 */
	@Deprecated
	private final String nbt;

	/**
	 * The item meta, overriden by other fields
	 */
	private final ItemMeta meta;

	// ----------------------------------------------------------------------------------------
	// Constructing items
	// ----------------------------------------------------------------------------------------

	/**
	 * Constructs a new {@link DummyButton} from this item
	 *
	 * @return a new dummy button
	 */
	public DummyButton makeButton() {
		return Button.makeDummy(this);
	}

	/**
	 * Make an unbreakable item with all attributes hidden, suitable for menu use.
	 *
	 * @return the new menu tool, unbreakable with all attributes hidden
	 */
	public ItemStack makeMenuTool() {
		unbreakable = true;

		return make();
	}

	/**
	 * Construct a valid {@link ItemStack} from all parameters above.
	 *
	 * @return the finished item
	 */
	public ItemStack make() {
		if (item == null)
			Objects.requireNonNull(material, "Material == null!");

		ItemStack is = item != null ? item.clone() : null;

		// Skip if air
		if ((item != null && item.getType() == Material.AIR) || (material != null && material == CompMaterial.AIR))
			return new ItemStack(Material.AIR);

		if (MinecraftVersion.atLeast(V.v1_13)) {

			if (is == null)
				is = new ItemStack(material.getMaterial(), amount, damage);

			color: if (color != null) {
				final String dye = color.getDye().toString();

				// Apply specific material color if possible
				final List<String> colorableMaterials = Arrays.asList("BANNER", "BED", "CARPET", "CONCRETE", "GLAZED_TERRACOTTA", "SHULKER_BOX",
						"STAINED_GLASS", "STAINED_GLASS_PANE", "TERRACOTTA", "WALL_BANNER", "WOOL");

				for (final String colorable : colorableMaterials) {
					final String suffix = "_" + colorable;

					if (is.getType().toString().endsWith(suffix)) {
						is.setType(Material.valueOf(dye + suffix));

						break color;
					}
				}

				// If not revert to wool
				is.setType(Material.valueOf(dye + "_WOOL"));
			}

		}

		// If using legacy MC version
		else {
			if (is == null) {
				int dataValue = material.getData();

				if (!material.toString().contains("LEATHER") && color != null)
					dataValue = color.getDye().getWoolData();

				is = new ItemStack(material.getMaterial(), amount, damage, (byte) dataValue);
			}
		}

		CompMaterial material = this.material;

		{ // Assign both material and item
			if (material == null)
				material = CompMaterial.fromMaterial(is.getType());

			Objects.requireNonNull(is, "ItemStack is null for " + material);
		}

		// Fix monster eggs
		if (material.toString().endsWith("SPAWN_EGG")) {

			EntityType entity = null;

			if (MinecraftVersion.olderThan(V.v1_13)) { // Try to find it if already exists
				CompMonsterEgg.acceptUnsafeEggs = true;
				final EntityType pre = CompMonsterEgg.getEntity(is);
				CompMonsterEgg.acceptUnsafeEggs = false;

				if (pre != null && pre != EntityType.UNKNOWN)
					entity = pre;
			}

			if (entity == null) {
				final String itemName = material.toString();
				String entityRaw = itemName.replace("_SPAWN_EGG", "");

				if ("MOOSHROOM".equals(entityRaw))
					entityRaw = "MUSHROOM_COW";

				else if ("ZOMBIE_PIGMAN".equals(entityRaw))
					entityRaw = "PIG_ZOMBIE";


				try {
					entity = EntityType.valueOf(entityRaw);

				} catch (final Throwable t) {
					t.printStackTrace();

					// Probably version incompatible
					RemainUtils.debug("The following item could not be transformed into " + entityRaw + " egg, item: " + is);
				}
			}

			if (entity != null)
				is = CompMonsterEgg.setEntity(is, entity);
		}


		final ItemMeta myMeta = meta != null ? meta.clone() : is.getItemMeta();

		flags = new ArrayList<>(RemainUtils.getOrDefault(flags, new ArrayList<>()));

		if (color != null && is.getType().toString().contains("LEATHER"))
			((LeatherArmorMeta)myMeta).setColor(color.getDye().getColor());

		if (skullOwner != null && myMeta instanceof SkullMeta)
			((SkullMeta) myMeta).setOwner(skullOwner);

		if (glow) {
			myMeta.addEnchant(Enchantment.DURABILITY, 1, true);

			flags.add(CompItemFlag.HIDE_ENCHANTS);
		}

		if (enchants != null)
			for (final UIEnchant ench : enchants)
				myMeta.addEnchant(ench.getEnchant(), ench.getLevel(), true);

		if (name != null)
			myMeta.setDisplayName(RemainUtils.colorize("&r" + name));

		if (lores != null) {
			final List<String> coloredLore = new ArrayList<>();

			lores.forEach( (line) -> coloredLore.add(RemainUtils.colorize("&7" + line)) );
			myMeta.setLore(coloredLore);
		}

		if (unbreakable != null) {
			flags.add(CompItemFlag.HIDE_ATTRIBUTES);
			flags.add(CompItemFlag.HIDE_UNBREAKABLE);

			if (MinecraftVersion.olderThan(V.v1_12))
				myMeta.spigot().setUnbreakable(true);

			else
				CompProperty.UNBREAKABLE.apply(myMeta, true);
		}

		if (hideTags)
			for (final CompItemFlag f : CompItemFlag.values())
				if (!flags.contains(f))
					flags.add(f);

		try {
			final List<org.bukkit.inventory.ItemFlag> f = RemainUtils.convert(flags, obj -> org.bukkit.inventory.ItemFlag.valueOf(obj.toString()));

			myMeta.addItemFlags( f.toArray( new org.bukkit.inventory.ItemFlag[ f.size() ] ) );
		} catch (final Throwable t) {}

		is.setItemMeta(myMeta);

		if (nbt != null) {
			final NBTItem nbtItem = new NBTItem(is);
			nbtItem.setString(Remain.getPlugin().getName() + "_Item", nbt);

			return nbtItem.getItem();
		}

		return is;
	}

	// ----------------------------------------------------------------------------------------
	// Static access
	// ----------------------------------------------------------------------------------------

	/**
	 * Convenience method to get a new item creator with material, name and lore set
	 *
	 * @param material
	 * @param name
	 * @param lore
	 * @return new item creator
	 */
	public static ItemCreatorBuilder of(Material material, String name, @NonNull String... lore) {
		return of(CompMaterial.fromMaterial(material), name, lore);
	}

	/**
	 * Convenience method to get a new item creator with material, name and lore set
	 *
	 * @param material
	 * @param name
	 * @param lore
	 * @return new item creator
	 */
	public static ItemCreatorBuilder of(CompMaterial material, String name, @NonNull String... lore) {
		for (int i = 0; i < lore.length; i++)
			lore[i] = "&7" + lore[i];

		return ItemCreator.builder().material(material).name("&r" + name).lores(Arrays.asList(lore)).hideTags(true);
	}

	/**
	 * Convenience method to get a wool
	 *
	 * @param color the wool color
	 * @return the new item creator
	 */
	public static ItemCreatorBuilder ofWool(CompDye color) {
		return of(CompMaterial.makeWool(color, 1)).color(color);
	}

	/**
	 * Convenience method to get the creator of an existing itemstack
	 *
	 * @param item existing itemstack
	 * @return the new item creator
	 */
	public static ItemCreatorBuilder of(ItemStack item) {
		return ItemCreator.builder().item(item);
	}

	/**
	 * Get a new item creator from material
	 *
	 * @param material existing material
	 * @deprecated use {@link #of(CompMaterial)} to ensure maximum cross-version compatibility
	 * @return the new item creator
	 */
	@Deprecated
	public static ItemCreatorBuilder of(Material material) {
		return of(CompMaterial.fromMaterial(material));
	}

	/**
	 * Get a new item creator from material
	 *
	 * @param material existing material
	 * @return the new item creator
	 */
	public static ItemCreatorBuilder of(CompMaterial mat) {
		Objects.requireNonNull(mat, "Material cannot be null!");

		return ItemCreator.builder().material(mat);
	}
}