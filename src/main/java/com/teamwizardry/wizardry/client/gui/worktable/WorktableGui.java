package com.teamwizardry.wizardry.client.gui.worktable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.teamwizardry.librarianlib.api.gui.EnumMouseButton;
import com.teamwizardry.librarianlib.api.gui.GuiBase;
import com.teamwizardry.librarianlib.api.gui.components.*;
import com.teamwizardry.librarianlib.api.gui.components.input.ComponentSlider;
import com.teamwizardry.librarianlib.api.util.misc.Utils;
import com.teamwizardry.librarianlib.client.Sprite;
import com.teamwizardry.librarianlib.client.Texture;
import com.teamwizardry.librarianlib.math.Vec2;
import com.teamwizardry.librarianlib.math.shapes.BezierCurve2D;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.module.Module;
import com.teamwizardry.wizardry.api.module.ModuleRegistry;
import com.teamwizardry.wizardry.api.spell.ModuleType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.teamwizardry.wizardry.lib.LibSprites.Worktable.*;

/**
 * Created by Saad on 6/17/2016.
 */
public class WorktableGui extends GuiBase {
	public static final Texture BACKGROUND_TEXTURE = new Texture(new ResourceLocation(Wizardry.MODID, "textures/gui/worktable/table_background.png"));
	public static final Sprite BACKGROUND_SPRITE = BACKGROUND_TEXTURE.getSprite("bg", 512, 256);

	public static final Texture SPRITE_SHEET = new Texture(new ResourceLocation(Wizardry.MODID, "textures/gui/worktable/sprite_sheet.png"));

	static final int iconSize = 12;
	public boolean useModules = false; // setting to true disables conventional rendering
	public Multimap<ModuleType, Module> modulesByType = HashMultimap.create();
	public Multimap<ModuleType, ComponentVoid> COMPONENTmodulesByType = HashMultimap.create();
	private ComponentVoid paper;
	private int left, top;
	private int rotateShimmer = 0;
	private HashMap<ModuleType, ArrayList<WorktableModule>> moduleCategories;
	private HashMap<ModuleType, WorktableSlider> categorySlidebars;
	private ArrayList<WorktableModule> modulesInSidebar;
	private ArrayList<WorktableModule> modulesOnPaper;
	private ArrayList<WorktableLink> moduleLinks;
	private BezierCurve2D curveModuleBeingLinked;
	private WorktableModule moduleBeingDragged, moduleBeingLinked, masterModule, moduleSelected;

	public WorktableGui() {
		super(512, 256);

		for (HashMap<Integer, Module> hashMap : ModuleRegistry.getInstance().getModules().values())
			for (Module module : hashMap.values())
				modulesByType.get(module.getType()).add(module);

		useModules = true;

		ComponentSprite background = new ComponentSprite(BACKGROUND_SPRITE, 0, 0);
		components.add(background);

		paper = new ComponentVoid(160, 0, 191, 202);
		paper.zIndex = 100;
		paper.add(new ComponentVoid(0, 0, 191, 202).setup((c) -> {
			c.addTag("tray");
			c.mouseUp.add((i, pos, button) -> {
				if (i.mouseOverThisFrame && button == EnumMouseButton.LEFT && moduleBeingDragged != null) {
					// TODO: Config for isMaster on the table
					// Set the module being dragged on the paper
					if (moduleBeingDragged.getModule().getType() == ModuleType.SHAPE)
						moduleBeingDragged.setMaster(true);
					masterModule = moduleBeingDragged;
					modulesOnPaper.add(moduleBeingDragged);
					moduleBeingDragged = null;
					moduleSelected = null;
				} else {
					// Delete module being dragged if it's outside the paper

					List<WorktableLink> concurrentLinks = moduleLinks.stream().filter(link -> link.getEndPointModule() == moduleBeingDragged || link.getStartPointModule() == moduleBeingDragged).collect(Collectors.toList());
					moduleLinks.removeAll(concurrentLinks);

					if (modulesOnPaper.contains(moduleBeingDragged)) modulesOnPaper.remove(moduleBeingDragged);
					moduleBeingDragged = null;
					moduleSelected = null;
				}
				return false;
			});
		}));
		paper.add(new ComponentVoid(213, 134, 98, 66).setup((c) -> c.addTag("tray"))); // TODO: ???
		components.add(paper);

		ComponentVoid effects = new ComponentVoid(92, 32, 52, 158);
		addModules(effects, ModuleType.EFFECT, 7, 7, 3, 12);
		components.add(effects);

		ComponentVoid shapes = new ComponentVoid(32, 32, 52, 74);
		addModules(shapes, ModuleType.SHAPE, 7, 7, 3, 5);
		components.add(shapes);

		ComponentVoid booleans = new ComponentVoid(32, 116, 52, 74);
		addModules(booleans, ModuleType.BOOLEAN, 7, 7, 3, 5);
		components.add(booleans);

		ComponentVoid events = new ComponentVoid(368, 31, 52, 87);
		addModules(events, ModuleType.EVENT, 7, 7, 3, 6);
		components.add(events);

		ComponentVoid modifiers = new ComponentVoid(428, 31, 52, 87);
		addModules(modifiers, ModuleType.MODIFIER, 7, 7, 3, 6);
		components.add(modifiers);
	}

	private void addModules(ComponentVoid parent, ModuleType type, int x, int y, int columns, int rows) {
		ComponentScrolledView view = new ComponentScrolledView(x, y, columns * 12, rows * 12);
		parent.add(view);

		ComponentGrid grid = new ComponentGrid(0, 0, 12, 12, columns);
		view.add(grid);

		int count = 0;
		for (Module constructor : modulesByType.get(type)) {
			SidebarItem item = new SidebarItem(0, 0, constructor, paper);
			grid.add(item.result);
			count++;
		}
		int usedRows = (int) Math.ceil(count / (float) columns);
		if (usedRows > rows) {
			ComponentSpriteCapped scrollSlot = new ComponentSpriteCapped(SCROLL_GROOVE_VERTICAL_TOP, SCROLL_GROOVE_VERTICAL_MIDDLE, SCROLL_GROOVE_VERTICAL_BOTTOM, false, x + columns * 12, y, 12, rows * 12);
			parent.add(scrollSlot);

			ComponentSlider scrollSlider = new ComponentSlider(6, SCROLL_SLIDER_VERTICAL.height / 2 + 2, 0, rows * 12 - SCROLL_SLIDER_VERTICAL.height - 4, 0, usedRows - 3);
			scrollSlider.handle.add(new ComponentSprite(SCROLL_SLIDER_VERTICAL, -SCROLL_SLIDER_VERTICAL.width / 2, -SCROLL_SLIDER_VERTICAL.height / 2));
			scrollSlider.percentageChange.add((p) -> view.scrollToPercent(new Vec2(0, p)));
			scrollSlot.add(scrollSlider);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		left = width / 2 - BACKGROUND_SPRITE.width / 2;
		top = height / 2 - BACKGROUND_SPRITE.height / 2;

		moduleCategories = new HashMap<>();
		categorySlidebars = new HashMap<>();
		modulesInSidebar = new ArrayList<>();
		modulesOnPaper = new ArrayList<>();
		moduleLinks = new ArrayList<>();

		initModules();
	}

	private void initModules() {
		// Construct the new module
		for (HashMap<Integer, Module> hashMap : ModuleRegistry.getInstance().getModules().values())
			for (Module module : hashMap.values()) {
				// Add it into moduleCategories
				moduleCategories.putIfAbsent(module.getType(), new ArrayList<>());
				ArrayList<WorktableModule> modules = moduleCategories.get(module.getType());
				modules.add(new WorktableModule(module));
				moduleCategories.put(module.getType(), modules);

				// Add it into modulesInSiderbar
				modulesInSidebar.add(new WorktableModule(module));
			}

		// Recalculate module positions to their respective sidebars
		HashMap<ModuleType, ArrayList<WorktableModule>> copyModuleCategories = new HashMap<>();
		for (ModuleType type : moduleCategories.keySet()) {

			// Calculate where the sidebar is
			int row = 0, maxColumns = 2, maxRows = 2, column = 0, sidebarLeft = 0, sidebarTop = 0;
			switch (type) {
				case BOOLEAN:
					sidebarLeft = left + 39;
					sidebarTop = top + 123;
					maxRows = 5;
					if (moduleCategories.get(type).size() >= 15) {
						maxColumns = 2;
						categorySlidebars.put(type, new WorktableSlider(maxRows, maxColumns, sidebarLeft + iconSize * 2 + 1, sidebarTop, moduleCategories.get(type)));
					} else maxColumns = 3;
					break;
				case SHAPE:
					sidebarLeft = left + 39;
					sidebarTop = top + 39;
					maxRows = 5;
					if (moduleCategories.get(type).size() >= 15) {
						maxColumns = 2;
						categorySlidebars.put(type, new WorktableSlider(maxRows, maxColumns, sidebarLeft + iconSize * 2 + 1, sidebarTop, moduleCategories.get(type)));
					} else maxColumns = 3;
					break;
				case EVENT:
					sidebarLeft = left + 375;
					sidebarTop = top + 38;
					maxRows = 6;
					if (moduleCategories.get(type).size() >= 18) {
						maxColumns = 2;
						categorySlidebars.put(type, new WorktableSlider(maxRows, maxColumns, sidebarLeft + iconSize * 2 + 1, sidebarTop, moduleCategories.get(type)));
					} else maxColumns = 3;
					break;
				case EFFECT:
					sidebarLeft = left + 99;
					sidebarTop = top + 39;
					maxRows = 12;
					// TODO: TESTING HERE //
					maxColumns = 2;
					categorySlidebars.put(type, new WorktableSlider(maxRows, maxColumns, sidebarLeft + iconSize * 2 + 1, sidebarTop, moduleCategories.get(type)));
	                /*if (moduleCategories.get(type).size() >= 36) {
                        maxColumns = 2;
                        categorySlidebars.put(type, new WorktableSlider(maxRows, maxColumns, sidebarLeft + iconSize * 2 + 1, sidebarTop, moduleCategories.get(type)));
                    } else maxColumns = 3;*/
					// TODO: TESTING HERE //
					break;
				case MODIFIER:
					sidebarLeft = left + 435;
					sidebarTop = top + 38;
					maxRows = 6;
					if (moduleCategories.get(type).size() >= 18) {
						maxColumns = 2;
						categorySlidebars.put(type, new WorktableSlider(maxRows, maxColumns, sidebarLeft + iconSize * 2 + 1, sidebarTop, moduleCategories.get(type)));
					} else maxColumns = 3;
					break;
			}

			// Add the actual module into the calculated sidebar positions
			for (WorktableModule module : moduleCategories.get(type)) {

				int iconSeparation = 0;
				int x = sidebarLeft + (row * iconSize) + (row * iconSeparation);
				int y = sidebarTop + (column * iconSize) + (column * iconSeparation);

				module.setX(x);
				module.setY(y);

				if (row >= maxColumns - 1) {
					row = 0;
					if (column < maxRows) column++;
				} else row++;

				copyModuleCategories.putIfAbsent(type, new ArrayList<>());
				ArrayList<WorktableModule> modules = copyModuleCategories.get(type);
				modules.add(module);
				copyModuleCategories.put(type, modules);
			}
		}

		moduleCategories.clear();
		moduleCategories.putAll(copyModuleCategories);
		modulesInSidebar.clear();
		for (ArrayList<WorktableModule> modules : moduleCategories.values()) modulesInSidebar.addAll(modules);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, clickedMouseButton);

		if (clickedMouseButton == 0) {

			// Get a module from the sidebar.
			modulesInSidebar.stream().filter(module -> Utils.isInside(mouseX, mouseY, module.getX(), module.getY(), iconSize)).forEach(module -> {
				moduleBeingDragged = module.copy();
				moduleBeingDragged.setX(mouseX - iconSize / 2);
				moduleBeingDragged.setY(mouseY - iconSize / 2);
			});

			// Select a module
			boolean insideAnything = false;
			for (WorktableModule module : modulesOnPaper) {
				if (Utils.isInside(mouseX, mouseY, module.getX(), module.getY(), iconSize)) {
					if (moduleSelected != module) {
						moduleSelected = module;
						insideAnything = true;
						break;
					}
				}
			}
			if (!insideAnything && moduleSelected != null) moduleSelected = null;
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		if (moduleBeingLinked == null && clickedMouseButton == 1) {
			// Link module on paper
			for (WorktableModule module : modulesOnPaper) {
				if (Utils.isInside(mouseX, mouseY, module.getX(), module.getY(), iconSize)) {
					moduleBeingLinked = module;
					curveModuleBeingLinked = new BezierCurve2D(new Vec2(module.getX() + iconSize / 2, module.getY() + iconSize / 2), new Vec2(mouseX, mouseY));
					break;
				}
			}
		}

		if (clickedMouseButton == 0) {
			// Drag/Readjust module on paper.
			WorktableModule remove = null;
			if (moduleBeingDragged == null)
				for (WorktableModule module : modulesOnPaper) {
					if (Utils.isInside(mouseX, mouseY, module.getX(), module.getY(), iconSize)) {
						if (masterModule == module) masterModule = null;
						moduleBeingDragged = module;
						moduleBeingDragged.setX(mouseX);
						moduleBeingDragged.setY(mouseY);
						remove = module;
						break;
					}
				}

			// Delete module that was on paper but is now a module being dragged
			if (remove != null) {
				if (modulesOnPaper.contains(remove))
					modulesOnPaper.remove(remove);
				moduleSelected = null;
			}
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int clickedMouseButton) {
		super.mouseReleased(mouseX, mouseY, clickedMouseButton);

		if (clickedMouseButton == 1) {
			if (moduleBeingLinked != null) {
				boolean insideAnything = false;
				for (WorktableModule to : modulesOnPaper) {
					if (Utils.isInside(mouseX, mouseY, to.getX(), to.getY(), iconSize)) {
						WorktableModule from = moduleBeingLinked;

						boolean wasLinked = false;

						// Remove a link if it's already established on either side
						for (WorktableLink link : moduleLinks)
							if (link.getStartPointModule() == from && link.getEndPointModule() == to) {
								moduleLinks.remove(link);
								wasLinked = true;
								break;
							} else if (link.getStartPointModule() == to && link.getEndPointModule() == from) {
								moduleLinks.remove(link);
								wasLinked = true;
								break;
							}

						if (to.getModule().accept(from.getModule())) {

							// There was no link, make one
							if (!wasLinked) moduleLinks.add(new WorktableLink(from, to));

							curveModuleBeingLinked = null;
							moduleBeingLinked = null;
							insideAnything = true;
						}
						break;
					}
				}

				// The mouse linking was never in a module to begin with, remove the mouse link
				if (!insideAnything) {
					moduleBeingLinked = null;
					curveModuleBeingLinked = null;
				}
			} else {
				moduleBeingLinked = null;
				curveModuleBeingLinked = null;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!useModules) { // no indent for git diff


			WorktableModule moduleBeingHovered = null;

			// SHIMMER CURSOR IF LINKING MODE //
			// TODO
			GlStateManager.color(1F, 1F, 1F, 1F);
			if (moduleBeingLinked != null) {
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.color(1F, 1F, 1F, 1F);
				if (rotateShimmer < 360) rotateShimmer++;
				else rotateShimmer = 0;
				GlStateManager.translate(mouseX, mouseY, 0);
				GlStateManager.rotate(rotateShimmer * 5, 0, 0, 1);
				GlStateManager.translate(-mouseX, -mouseY, 0);
				mc.renderEngine.bindTexture(new ResourceLocation(Wizardry.MODID, "textures/gui/worktable/shimmer.png"));
				drawScaledCustomSizeModalRect(mouseX - 16 / 2, mouseY - 16 / 2, 0, 0, 16, 16, 16, 16, 16, 16);
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			// SHIMMER CURSOR IF LINKING MODE //

			// RENDER MODULES IN THE SIDEBARS //
			GlStateManager.color(1F, 1F, 1F, 1F);
			SPRITE_SHEET.bind();
			for (ModuleType type : moduleCategories.keySet()) {
				if (categorySlidebars.containsKey(type)) {
					for (WorktableModule module : categorySlidebars.get(type).getModules()) {
						// Highlight if hovering over
						if (Utils.isInside(mouseX, mouseY, module.getX(), module.getY(), iconSize)) {
							moduleBeingHovered = module;
						} else {
							Sprite base = MODULE_DEFAULT_GLOW;
							base.getTex().bind();
							base.draw(0, module.getX(), module.getY(), iconSize, iconSize);
						}
					}
				} else {
					for (WorktableModule module : moduleCategories.get(type)) {
						// Highlight if hovering over
						if (Utils.isInside(mouseX, mouseY, module.getX(), module.getY(), iconSize)) {
							moduleBeingHovered = module;
						} else {
							Sprite base = MODULE_DEFAULT_GLOW;
							base.getTex().bind();
							base.draw(0, module.getX(), module.getY(), iconSize, iconSize);

							Sprite icon = module.getModule().getStaticIcon();
							icon.getTex().bind();
							icon.draw(0, module.getX(), module.getY(), iconSize, iconSize);
						}
					}
				}
			}
			GlStateManager.color(1F, 1F, 1F, 1F);
			for (ModuleType type : categorySlidebars.keySet()) categorySlidebars.get(type).draw();
			// RENDER MODULES IN THE SIDEBARS //

			// RENDER LINE BETWEEN LINKED MODULES //
			GlStateManager.color(1F, 1F, 1F, 1F);
			if (moduleBeingDragged != null) {
				for (WorktableLink link : moduleLinks) {
					if (link.getStartPointModule() == moduleBeingDragged) link.setStartPointModule(moduleBeingDragged);
					else if (link.getEndPointModule() == moduleBeingDragged) link.setEndPointModule(moduleBeingDragged);
					link.draw();
				}
			}

			if (moduleBeingLinked != null && curveModuleBeingLinked != null) {
				curveModuleBeingLinked.setStartPoint(new Vec2(mouseX, mouseY));
				curveModuleBeingLinked.draw();
			}

			moduleLinks.stream().filter(link -> link.getStartPointModule() != moduleBeingDragged && link.getEndPointModule() != moduleBeingDragged).forEach(BezierCurve2D::draw);
			// RENDER LINE BETWEEN LINKED MODULES //

			// RENDER MODULE ON THE PAPER //
			GlStateManager.color(1F, 1F, 1F, 1F);
			for (WorktableModule module : modulesOnPaper) {
				if (moduleSelected != module) {
					if (Utils.isInside(mouseX, mouseY, module.getX(), module.getY(), iconSize)) {
						moduleBeingHovered = module;
					} else {
						Sprite moduleSprite = MODULE_DEFAULT;
						moduleSprite.getTex().bind();
						moduleSprite.draw(0, module.getX(), module.getY(), iconSize, iconSize);
					}
				}
			}
			// RENDER MODULE ON THE PAPER //

			// RENDER MODULE BEING DRAGGED //
			GlStateManager.color(1F, 1F, 1F, 1F);
			if (moduleBeingDragged != null) {
				moduleBeingDragged.setX(mouseX - iconSize / 2);
				moduleBeingDragged.setY(mouseY - iconSize / 2);
				Sprite draggingSprite = MODULE_DEFAULT;
				draggingSprite.getTex().bind();
				draggingSprite.draw(0, mouseX - iconSize / 2 - 2, mouseY - iconSize / 2 - 2, iconSize + 4, iconSize + 4);
			}
			// RENDER MODULE BEING DRAGGED //

			// RENDER TOOLTIP & HIGHLIGHT //
			// Highlight module selected
			if (moduleSelected != null) {
				// Render highlight
				GlStateManager.disableLighting();
				Sprite highlight = MODULE_DEFAULT;
				highlight.getTex().bind();
				highlight.draw(0, moduleSelected.getX() - 2, moduleSelected.getY() - 2, iconSize + 4, iconSize + 4);
				GlStateManager.enableLighting();
			}

			// Highlight module being hovered
			if (moduleBeingHovered != null && moduleBeingDragged == null) {
				// Render highlight
				GlStateManager.disableLighting();
				Sprite highlight = MODULE_DEFAULT;
				highlight.getTex().bind();
				highlight.draw(0, moduleBeingHovered.getX(), moduleBeingHovered.getY(), iconSize, iconSize);
				GlStateManager.enableLighting();

				// Render tooltip
				if (modulesOnPaper.contains(moduleBeingHovered) && !isShiftKeyDown()) return;
				List<String> txt = new ArrayList<>();
				txt.add(TextFormatting.GOLD + moduleBeingHovered.getModule().getDisplayName());
				txt.addAll(Utils.padString(moduleBeingHovered.getModule().getDescription(), 30));
				drawHoveringText(txt, mouseX, mouseY, fontRendererObj);
			}
			// RENDER TOOLTIP & HIGHLIGHT //

		} // end useModules - no indent for git diff
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}
}