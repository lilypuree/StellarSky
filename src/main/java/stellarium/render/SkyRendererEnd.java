package stellarium.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.IRenderHandler;
import stellarium.StellarSkyResources;
import stellarium.api.ICelestialRenderer;

public class SkyRendererEnd extends IRenderHandler {

	private ICelestialRenderer celestials;
	
	public SkyRendererEnd(ICelestialRenderer subRenderer) {
		this.celestials = subRenderer;
	}

	@Override
	public void render(float partialTicks, WorldClient theWorld, Minecraft mc) {
		/*
		 * Render the end sky. This is rendered as cube which is on far away.
		 */
		GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        GL11.glDepthMask(false);
        mc.renderEngine.bindTexture(StellarSkyResources.resourceEndSky.getLocation());
        Tessellator tessellator = Tessellator.instance;

		/*
		 * The cube. (6 faces)
		 */
        for (int i = 0; i < 6; ++i)
        {
            GL11.glPushMatrix();

            if (i == 1)
            {
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 2)
            {
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 3)
            {
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 4)
            {
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            }

            if (i == 5)
            {
                GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
            }

            tessellator.startDrawingQuads();
            tessellator.setColorOpaque_I(2631720);
            tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, 0.0D, 0.0D);
            tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, 0.0D, 16.0D);
            tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, 16.0D, 16.0D);
            tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, 16.0D, 0.0D);
            tessellator.draw();
            GL11.glPopMatrix();
        }
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        
        GL11.glPushMatrix();
        GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
        celestials.renderCelestial(mc, theWorld, new float[] {0.0f, 0.0f, 0.0f}, partialTicks);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        celestials.renderSkyLandscape(mc, theWorld, partialTicks);
        GL11.glPopMatrix();
        
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	}


}
