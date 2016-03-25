package stellarium.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import sciapi.api.value.IValRef;
import sciapi.api.value.euclidian.CrossUtil;
import sciapi.api.value.euclidian.EVector;
import sciapi.api.value.euclidian.IEVector;
import sciapi.api.value.util.VOp;
import stellarium.client.ClientSettings;
import stellarium.stellars.ExtinctionRefraction;
import stellarium.stellars.Optics;
import stellarium.stellars.StellarManager;
import stellarium.util.math.Spmath;
import stellarium.util.math.VecMath;

public class CelestialLayerMoon implements ICelestialLayer {
	
	private static final ResourceLocation locationMoonPng = new ResourceLocation("stellarium", "stellar/lune.png");
	private static final ResourceLocation locationhalolunePng = new ResourceLocation("stellarium", "stellar/haloLune.png");
	
	private EVector posm = new EVector(3), difm = new EVector(3), difm2 = new EVector(3);
	private int latn, longn;
	private EVector moonvec[][], moonnormal[][];
	private float moonilum[][];

	EVector Buf = new EVector(3);
	EVector Buff = new EVector(3);
	
	@Override
	public void init(ClientSettings settings) {
		this.latn = settings.imgFrac;
		this.longn = 2*settings.imgFrac;
		this.moonvec=new EVector[longn][latn+1];
		this.moonilum=new float[longn][latn+1];
		this.moonnormal=new EVector[longn][latn+1];
	}
	
	@Override
	public void render(StellarManager manager, StellarRenderInfo info) {
		
		posm.set(ExtinctionRefraction.refraction(manager.Moon.getPosition(), true));
		double sizem=manager.Moon.radius.asDouble()/Spmath.getD(VecMath.size(posm));

		double difactor = 0.8 / 180.0 * Math.PI / sizem;
		difactor = difactor * difactor / Math.PI;

		sizem *= (98.0*6.0);
		
		int latc, longc;
		for(longc=0; longc<longn; longc++){
			for(latc=0; latc<=latn; latc++){
				Buf.set(manager.Moon.posLocalM((double)longc/(double)longn*360.0, (double)latc/(double)latn*180.0-90.0,
						manager.transforms.yr));
				moonilum[longc][latc]=(float) (manager.Moon.illumination(Buf) * difactor * 1.5);
				moonnormal[longc][latc] = new EVector(3).set(Buf);
				Buf.set(manager.Moon.posLocalG(Buf));
				Buf.set(VecMath.mult(50000.0, Buf));
				IValRef ref=manager.transforms.projection.transform(Buf);

				moonvec[longc][latc] = new EVector(3);
				moonvec[longc][latc].set(ref);
				moonvec[longc][latc].set(ExtinctionRefraction.refraction(ref, true));

				if(VecMath.getZ(moonvec[longc][latc])<0.0f) moonilum[longc][latc]=0.0f;
			}
		}
		
		info.mc.renderEngine.bindTexture(locationhalolunePng);

		if(VecMath.getZ(posm)>0.0f){

			posm.set(VOp.normalize(posm));
			difm.set(VOp.normalize(CrossUtil.cross((IEVector)posm, (IEVector)new EVector(0.0,0.0,1.0))));
			difm2.set((IValRef)CrossUtil.cross((IEVector)difm, (IEVector)posm));
			posm.set(VecMath.mult(98.0, posm));

			difm.set(VecMath.mult(sizem, difm));
			difm2.set(VecMath.mult(sizem, difm2));

			float alpha=(float) (Optics.getAlphaFromMagnitude(16.0+manager.Moon.mag-2.5*Math.log10(difactor), info.bglight));		
			
			GlStateManager.color(1.0f, 1.0f, 1.0f, info.weathereff*alpha);

			info.worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			info.worldrenderer.pos(VecMath.getX(posm)+VecMath.getX(difm), VecMath.getY(posm)+VecMath.getY(difm), VecMath.getZ(posm)+VecMath.getZ(difm)).tex(0.0,0.0).endVertex();
			info.worldrenderer.pos(VecMath.getX(posm)+VecMath.getX(difm2), VecMath.getY(posm)+VecMath.getY(difm2), VecMath.getZ(posm)+VecMath.getZ(difm2)).tex(0.0,1.0).endVertex();
			info.worldrenderer.pos(VecMath.getX(posm)-VecMath.getX(difm), VecMath.getY(posm)-VecMath.getY(difm), VecMath.getZ(posm)-VecMath.getZ(difm)).tex(1.0,1.0).endVertex();
			info.worldrenderer.pos(VecMath.getX(posm)-VecMath.getX(difm2), VecMath.getY(posm)-VecMath.getY(difm2), VecMath.getZ(posm)-VecMath.getZ(difm2)).tex(1.0,0.0).endVertex();
			info.tessellator.draw();
		}

		info.mc.renderEngine.bindTexture(locationMoonPng);

		for(longc=0; longc<longn; longc++){
			for(latc=0; latc<latn; latc++){

				int longcd=(longc+1)%longn;
				double longd=(double)longc/(double)longn;
				double latd=1.0-(double)latc/(double)latn;
				double longdd=(double)(longc+1)/(double)longn;
				double latdd=1.0-(double)(latc+1)/(double)latn;

				float lightlevel = (0.875f*(info.bglight/2.1333334f));
				
				GlStateManager.color(1.0f, 1.0f, 1.0f, info.weathereff*((float)moonilum[longc][latc]-4.0f*info.bglight)*2.0f);
				
				info.worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
				info.worldrenderer.pos(VecMath.getX(moonvec[longc][latc]), VecMath.getY(moonvec[longc][latc]), VecMath.getZ(moonvec[longc][latc])).tex(Spmath.fmod(longd+0.5, 1.0), latd);
				info.worldrenderer.color(1.0f, 1.0f, 1.0f, Spmath.clip((info.weathereff*(float)moonilum[longc][latc]-4.0f*info.bglight)*2.0f));
				info.worldrenderer.normal((float)VecMath.getX(moonnormal[longc][latc]), (float)VecMath.getY(moonnormal[longc][latc]), (float)VecMath.getZ(moonnormal[longc][latc]));
				info.worldrenderer.endVertex();
				
				info.worldrenderer.pos(VecMath.getX(moonvec[longcd][latc]), VecMath.getY(moonvec[longcd][latc]), VecMath.getZ(moonvec[longcd][latc])).tex(Spmath.fmod(longdd+0.5,1.0), latd);
				info.worldrenderer.color(1.0f, 1.0f, 1.0f, Spmath.clip((info.weathereff*(float)moonilum[longcd][latc]-4.0f*info.bglight)*2.0f));
				info.worldrenderer.normal((float)VecMath.getX(moonnormal[longcd][latc]), (float)VecMath.getY(moonnormal[longcd][latc]), (float)VecMath.getZ(moonnormal[longcd][latc]));
				info.worldrenderer.endVertex();
				
				info.worldrenderer.pos(VecMath.getX(moonvec[longcd][latc+1]), VecMath.getY(moonvec[longcd][latc+1]), VecMath.getZ(moonvec[longcd][latc+1])).tex(Spmath.fmod(longdd+0.5, 1.0), latdd);
				info.worldrenderer.color(1.0f, 1.0f, 1.0f, Spmath.clip((info.weathereff*(float)moonilum[longcd][latc+1]-4.0f*info.bglight)*2.0f));
				info.worldrenderer.normal((float)VecMath.getX(moonnormal[longcd][latc+1]), (float)VecMath.getY(moonnormal[longcd][latc+1]), (float)VecMath.getZ(moonnormal[longcd][latc+1]));
				info.worldrenderer.endVertex();

				info.worldrenderer.pos(VecMath.getX(moonvec[longc][latc+1]), VecMath.getY(moonvec[longc][latc+1]), VecMath.getZ(moonvec[longc][latc+1])).tex(Spmath.fmod(longd+0.5,1.0), latdd);
				info.worldrenderer.color(1.0f, 1.0f, 1.0f, Spmath.clip((info.weathereff*(float)moonilum[longc][latc+1]-4.0f*info.bglight)*2.0f));
				info.worldrenderer.normal((float)VecMath.getX(moonnormal[longc][latc+1]), (float)VecMath.getY(moonnormal[longc][latc+1]), (float)VecMath.getZ(moonnormal[longc][latc+1]));
				info.worldrenderer.endVertex();
				info.tessellator.draw();
			}
		}
		
	}

}
