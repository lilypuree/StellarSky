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

public class ModelFixButton implements IRenderModel {
	
	private static final ModelFixButton instance = new ModelFixButton();
	
	public static ModelFixButton getInstance() {
		return instance;
	}
	
	private ModelSimpleRect selectModel = ModelSimpleRect.getInstance();
	private ModelSimpleTextured clickedModel;
	private ModelSimpleTextured fixTextureModel;
	
	public ModelFixButton() {
		this.clickedModel = new ModelSimpleTextured(StellarSkyResources.clicked);
		this.fixTextureModel = new ModelSimpleTextured(StellarSkyResources.fix);
	}

	@Override
	public void renderModel(String info, IRectangleBound totalBound, IRectangleBound clipBound, Tessellator tessellator,
			TextureManager textureManager) {
		GL11.glPushMatrix();
		if(info.equals("select")) {
			GL11.glScalef(0.9f, 0.9f, 0.9f);
			selectModel.setColor(1.0f, 1.0f, 1.0f, 0.2f);
			selectModel.renderModel(info, totalBound, clipBound, tessellator, textureManager);
		} else if(info.equals("fixed")) {
			clickedModel.renderModel(info, totalBound, clipBound, tessellator, textureManager);
			GL11.glScalef(0.9f, 0.9f, 0.9f);
			fixTextureModel.renderModel(info, totalBound, clipBound, tessellator, textureManager);
		} else {
			GL11.glScalef(0.9f, 0.9f, 0.9f);
			GL11.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
			fixTextureModel.renderModel(info, totalBound, clipBound, tessellator, textureManager);
		}
		GL11.glPopMatrix();
	}

}
