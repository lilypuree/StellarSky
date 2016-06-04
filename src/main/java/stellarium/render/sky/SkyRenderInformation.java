package stellarium.render.sky;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import stellarium.view.ViewerInfo;

public class SkyRenderInformation {
	public final Minecraft minecraft;
	public final WorldClient world;
	public final Tessellator tessellator;
	public final float partialTicks;
	
	public final boolean isFrameBufferEnabled;
	public final float deepDepth;
	
	public final ViewerInfo info;
	public final double screenSize;
	
	public SkyRenderInformation(Minecraft mc, WorldClient theWorld, float partialTicks, ViewerInfo viewer) {
		this.minecraft = mc;
		this.world = theWorld;
		this.tessellator = Tessellator.instance;
		this.partialTicks = partialTicks;
		
		this.isFrameBufferEnabled = OpenGlHelper.isFramebufferEnabled();
		
		int renderDistance = mc.gameSettings.renderDistanceChunks;
		
		this.deepDepth = 30.0f * renderDistance;

		this.info = viewer;
		this.screenSize = mc.displayWidth;
	}
}