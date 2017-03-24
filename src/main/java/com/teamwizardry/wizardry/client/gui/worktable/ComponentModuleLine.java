package com.teamwizardry.wizardry.client.gui.worktable;

import com.teamwizardry.librarianlib.client.gui.GuiComponent;
import com.teamwizardry.librarianlib.client.gui.Option;
import com.teamwizardry.librarianlib.common.util.math.Vec2d;
import com.teamwizardry.librarianlib.common.util.math.interpolate.position.InterpBezier2D;
import com.teamwizardry.librarianlib.common.util.math.interpolate.position.InterpBezier3D;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class ComponentModuleLine extends GuiComponent<ComponentModuleLine> {

	private Vec2d end = Vec2d.ZERO;

	public ComponentModuleLine(Vec2d start, Vec2d end) {
		super(start.getXi(), start.getYi());
		this.end = end;
	}

	public void set(Vec2d start, Vec2d end) {
		this.end = end;
		setPos(start);
	}

	@Override
	public void drawComponent(Vec2d mousePos, float partialTicks) {
		GlStateManager.disableTexture2D();
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vb = tessellator.getBuffer();
		vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		Vec2d lastPos = null;
		for (Vec2d position : new InterpBezier2D(getPos(), end).list(50)) {
			vb.pos(position.getX(), position.getY(), 0).endVertex();
			if (lastPos != null) vb.pos(lastPos.getX(), lastPos.getY(), 0).endVertex();
			lastPos = position;
		}
		tessellator.draw();

		GlStateManager.enableTexture2D();
	}

}
