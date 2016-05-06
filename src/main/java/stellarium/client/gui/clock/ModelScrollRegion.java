package stellarium.client.gui.clock;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import stellarium.StellarSkyReferences;
import stellarium.StellarSkyResources;
import stellarium.client.gui.content.IRectangleBound;
import stellarium.client.gui.content.IRenderModel;
import stellarium.client.gui.content.basicmodel.ModelSimpleRect;
import stellarium.client.gui.content.basicmodel.ModelSimpleTextured;

public class ModelScrollRegion implements IRenderModel {
	
	private static final ModelScrollRegion instance = new ModelScrollRegion();
	
	public static ModelScrollRegion getInstance() {
		return instance;
	}
	
	private ModelSimpleRect selectModel = ModelSimpleRect.getInstance();
	private ModelSimpleTextured scrollregion;
	
	public ModelScrollRegion() {
		this.scrollregion = new ModelSimpleTextured(StellarSkyResources.scrollregion);
	}

	@Override
	public void renderModel(String info, IRectangleBound totalBound, IRectangleBound clipBound, Tessellator tessellator,
			TextureManager textureManager) {
		GL11.glPushMatrix();
		if(info.equals("select")) {
			selectModel.setColor(1.0f, 1.0f, 1.0f, 0.2f);
			selectModel.renderModel(info, totalBound, clipBound, tessellator, textureManager);
		} else if(info.equals("region")) {
			scrollregion.renderModel(info, totalBound, clipBound, tessellator, textureManager);
		}
		GL11.glPopMatrix();
	}

}
