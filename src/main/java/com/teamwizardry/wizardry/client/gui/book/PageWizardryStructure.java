package com.teamwizardry.wizardry.client.gui.book;

import com.google.gson.JsonObject;
import com.teamwizardry.librarianlib.features.gui.component.GuiComponent;
import com.teamwizardry.librarianlib.features.gui.provided.book.IBookGui;
import com.teamwizardry.librarianlib.features.gui.provided.book.context.Bookmark;
import com.teamwizardry.librarianlib.features.gui.provided.book.hierarchy.entry.Entry;
import com.teamwizardry.librarianlib.features.gui.provided.book.hierarchy.page.Page;
import com.teamwizardry.librarianlib.features.math.Vec2d;
import kotlin.jvm.functions.Function0;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PageWizardryStructure implements Page {

	private Entry entry;
	private String structureName;
	private ResourceLocation structure;

	public PageWizardryStructure(Entry entry, JsonObject element) {
		this.entry = entry;
		if (element != null && element.has("name"))
			structureName = element.getAsJsonPrimitive("name").getAsString();
		if (structureName != null) {
			structure = new ResourceLocation(structureName);
		}
	}

	@NotNull
	@Override
	public Entry getEntry() {
		return entry;
	}

	@NotNull
	@Override
	public List<Bookmark> getExtraBookmarks() {
		ArrayList<Bookmark> list = new ArrayList<>();
		list.add(new BookmarkWizardryStructure(structure));
		return list;
	}

	@Nullable
	@Override
	public Collection<String> getSearchableKeys() {
		ArrayList<String> list = new ArrayList<>();
		list.add(structureName);
		return list;
	}

	@Nullable
	@Override
	public Collection<String> getSearchableStrings() {
		return null;
	}

	@SideOnly(Side.CLIENT)
	@NotNull
	@Override
	public List<Function0<GuiComponent>> createBookComponents(@NotNull IBookGui book, @NotNull Vec2d size) {
		ArrayList<Function0<GuiComponent>> list = new ArrayList<>();
		list.add(() -> new ComponentWizardryStructure(book, 16, 16, size.getXi(), size.getYi(), structure));
		return list;
	}
}
