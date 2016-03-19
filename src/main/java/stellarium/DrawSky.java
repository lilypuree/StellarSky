package stellarium;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import sciapi.api.value.IValRef;
import sciapi.api.value.euclidian.CrossUtil;
import sciapi.api.value.euclidian.EVector;
import sciapi.api.value.euclidian.IEVector;
import sciapi.api.value.util.VOp;
import stellarium.stellars.Color;
import stellarium.stellars.ExtinctionRefraction;
import stellarium.stellars.Optics;
import stellarium.stellars.Planet;
import stellarium.stellars.background.BrStar;
import stellarium.util.math.SpCoord;
import stellarium.util.math.Spmath;
import stellarium.util.math.Transforms;
import stellarium.util.math.VecMath;

public class DrawSky extends IRenderHandler {

	private TextureManager renderEngine;
	private Minecraft mc;
	private Random random;
	private Tessellator tessellator1=Tessellator.getInstance();
	
    private VertexFormat vertexBufferFormat;
    private net.minecraft.client.renderer.vertex.VertexBuffer skyVBO;
    private net.minecraft.client.renderer.vertex.VertexBuffer sky2VBO;
    private boolean vboEnabled = false;
    
    private int glSkyList = -1;
    private int glSkyList2 = -1;

	private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
	private static final ResourceLocation locationSunPng = new ResourceLocation("stellarium", "stellar/halo.png");
	private static final ResourceLocation locationMoonPng = new ResourceLocation("stellarium", "stellar/lune.png");
	private static final ResourceLocation locationStarPng = new ResourceLocation("stellarium", "stellar/star.png");
	private static final ResourceLocation locationhalolunePng = new ResourceLocation("stellarium", "stellar/haloLune.png");
	private static final ResourceLocation locationMilkywayPng = new ResourceLocation("stellarium", "stellar/milkyway.png");

	/*private boolean IsMid, IsCalcd;*/

	public DrawSky(){
		this.random = new Random(System.currentTimeMillis());
		this.mc = Minecraft.getMinecraft();
        this.renderEngine = mc.getTextureManager();
        this.vboEnabled = OpenGlHelper.useVbo();

        this.vertexBufferFormat = new VertexFormat();
        this.vertexBufferFormat.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 2));
        this.generateSky();
        this.generateSky2();
	}
	
    private void generateSky2()
    {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();

        if (this.sky2VBO != null)
        {
            this.sky2VBO.deleteGlBuffers();
        }

        if (this.glSkyList2 >= 0)
        {
            GLAllocation.deleteDisplayLists(this.glSkyList2);
            this.glSkyList2 = -1;
        }

        if (this.vboEnabled)
        {
            this.sky2VBO = new net.minecraft.client.renderer.vertex.VertexBuffer(this.vertexBufferFormat);
            this.renderSky(worldrenderer, -16.0F, true);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            this.sky2VBO.bufferData(worldrenderer.getByteBuffer());
        }
        else
        {
            this.glSkyList2 = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
            this.renderSky(worldrenderer, -16.0F, true);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void generateSky()
    {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();

        if (this.skyVBO != null)
        {
            this.skyVBO.deleteGlBuffers();
        }

        if (this.glSkyList >= 0)
        {
            GLAllocation.deleteDisplayLists(this.glSkyList);
            this.glSkyList = -1;
        }

        if (this.vboEnabled)
        {
            this.skyVBO = new net.minecraft.client.renderer.vertex.VertexBuffer(this.vertexBufferFormat);
            this.renderSky(worldrenderer, 16.0F, false);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            this.skyVBO.bufferData(worldrenderer.getByteBuffer());
        }
        else
        {
            this.glSkyList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
            this.renderSky(worldrenderer, 16.0F, false);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void renderSky(VertexBuffer worldRendererIn, float p_174968_2_, boolean p_174968_3_)
    {
        int i = 64;
        int j = 6;
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

        for (int k = -384; k <= 384; k += 64)
        {
            for (int l = -384; l <= 384; l += 64)
            {
                float f = (float)k;
                float f1 = (float)(k + 64);

                if (p_174968_3_)
                {
                    f1 = (float)k;
                    f = (float)(k + 64);
                }

                worldRendererIn.pos((double)f, (double)p_174968_2_, (double)l).endVertex();
                worldRendererIn.pos((double)f1, (double)p_174968_2_, (double)l).endVertex();
                worldRendererIn.pos((double)f1, (double)p_174968_2_, (double)(l + 64)).endVertex();
                worldRendererIn.pos((double)f, (double)p_174968_2_, (double)(l + 64)).endVertex();
            }
        }
    }
	
	private void renderSkyEnd()
    {
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.depthMask(false);
        this.renderEngine.bindTexture(locationEndSkyPng);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();

        for (int i = 0; i < 6; ++i)
        {
            GlStateManager.pushMatrix();

            if (i == 1)
            {
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 2)
            {
                GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 3)
            {
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 4)
            {
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            }

            if (i == 5)
            {
                GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            }

            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(40, 40, 40, 255).endVertex();
            worldrenderer.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(40, 40, 40, 255).endVertex();
            worldrenderer.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(40, 40, 40, 255).endVertex();
            worldrenderer.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(40, 40, 40, 255).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
    }
	
	@Override
	public void render(float partialTicks, WorldClient theWorld, Minecraft mc)
    {
        if (this.mc.theWorld.provider.getDimension() == 1)
        {
            this.renderSkyEnd();
        }
        else if (this.mc.theWorld.provider.isSurfaceWorld())
        {
            GlStateManager.disableTexture2D();
            Vec3d vec3 = theWorld.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
            float f = (float)vec3.xCoord;
            float f1 = (float)vec3.yCoord;
            float f2 = (float)vec3.zCoord;

            if (mc.gameSettings.anaglyph)
            {
                float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                f = f3;
                f1 = f4;
                f2 = f5;
            }

            GlStateManager.color(f, f1, f2);
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer worldrenderer = tessellator.getBuffer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();
            GlStateManager.color(f, f1, f2);

            if (this.vboEnabled)
            {
                this.skyVBO.bindBuffer();
                GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                this.skyVBO.drawArrays(7);
                this.skyVBO.unbindBuffer();
                GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            }
            else
            {
                GL11.glCallList(this.glSkyList);
            }

            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            float[] afloat = theWorld.provider.calcSunriseSunsetColors(theWorld.getCelestialAngle(partialTicks), partialTicks);

            if (afloat != null)
            {
                GlStateManager.disableTexture2D();
                GlStateManager.shadeModel(7425);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(MathHelper.sin(theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                float f6 = afloat[0];
                float f7 = afloat[1];
                float f8 = afloat[2];

                if (mc.gameSettings.anaglyph)
                {
                    float f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
                    float f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
                    float f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
                    f6 = f9;
                    f7 = f10;
                    f8 = f11;
                }

                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3]).endVertex();
                int j = 16;

                for (int l = 0; l <= 16; ++l)
                {
                    float f21 = (float)l * (float)Math.PI * 2.0F / 16.0F;
                    float f12 = MathHelper.sin(f21);
                    float f13 = MathHelper.cos(f21);
                    worldrenderer.pos((double)(f12 * 120.0F), (double)(f13 * 120.0F), (double)(-f13 * 40.0F * afloat[3])).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                }

                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.shadeModel(7424);
            }

            GlStateManager.enableTexture2D();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GlStateManager.pushMatrix();
            float weathereff = 1.0F - theWorld.getRainStrength(partialTicks);
			float bglight=f+f1+f2;

			GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f); //e,n,z

			GlStateManager.color(1.0F, 1.0F, 1.0F, weathereff);

			double time=(double)theWorld.getWorldTime()+partialTicks;
			
			this.RenderStar(bglight, weathereff, time);

			GlStateManager.color(1.0F, 1.0F, 1.0F, weathereff);

			//Rendering Sun
			EVector pos = new EVector(3);
			pos.set(StellarSky.getManager().Sun.getPosition());
			double size=StellarSky.getManager().Sun.radius/Spmath.getD(VecMath.size(pos))*99.0*20;
			pos.set(VecMath.normalize(pos));
			dif.set(VOp.normalize(CrossUtil.cross((IEVector)pos, (IEVector)new EVector(0.0,0.0,1.0))));
			dif2.set((IValRef)CrossUtil.cross((IEVector)dif, (IEVector)pos));
			pos.set(VecMath.mult(99.0, pos));

			dif.set(VecMath.mult(size, dif));
			dif2.set(VecMath.mult(size, dif2));

			renderEngine.bindTexture(this.locationSunPng);
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			worldrenderer.pos(VecMath.getX(pos)+VecMath.getX(dif), VecMath.getY(pos)+VecMath.getY(dif), VecMath.getZ(pos)+VecMath.getZ(dif)).tex(0.0,0.0).endVertex();
			worldrenderer.pos(VecMath.getX(pos)+VecMath.getX(dif2), VecMath.getY(pos)+VecMath.getY(dif2), VecMath.getZ(pos)+VecMath.getZ(dif2)).tex(1.0,0.0).endVertex();
			worldrenderer.pos(VecMath.getX(pos)-VecMath.getX(dif), VecMath.getY(pos)-VecMath.getY(dif), VecMath.getZ(pos)-VecMath.getZ(dif)).tex(1.0,1.0).endVertex();
			worldrenderer.pos(VecMath.getX(pos)-VecMath.getX(dif2), VecMath.getY(pos)-VecMath.getY(dif2), VecMath.getZ(pos)-VecMath.getZ(dif2)).tex(0.0,1.0).endVertex();
			tessellator1.draw();
			//Sun


			//Rendering Moon
			int latn=StellarSky.proxy.getClientSettings().imgFrac;
			int longn=2*latn;
			EVector moonvec[][];
			double moonilum[][];
			moonvec=new EVector[longn][latn+1];
			moonilum=new double[longn][latn+1];
			EVector Buf = new EVector(3);
			EVector Buff = new EVector(3);
			int latc, longc;
			for(longc=0; longc<longn; longc++){
				for(latc=0; latc<=latn; latc++){
					Buf.set(StellarSky.getManager().Moon.posLocalM((double)longc/(double)longn*360.0, (double)latc/(double)latn*180.0-90.0, Transforms.yr));
					moonilum[longc][latc]=StellarSky.getManager().Moon.illumination(Buf);
					Buf.set(StellarSky.getManager().Moon.posLocalG(Buf));
					Buf.set(VecMath.mult(50000.0, Buf));
					Buff.set(VecMath.getX(Buf),VecMath.getY(Buf),VecMath.getZ(Buf));
					IValRef ref=Transforms.ZTEctoNEc.transform((IEVector)Buff);
					ref=Transforms.EctoEq.transform(ref);
					ref=Transforms.NEqtoREq.transform(ref);
					ref=Transforms.REqtoHor.transform(ref);

					moonvec[longc][latc] = new EVector(3);
					moonvec[longc][latc].set(ExtinctionRefraction.refraction(ref, true));

					if(VecMath.getZ(moonvec[longc][latc])<0.0f) moonilum[longc][latc]=0.0;

				}
			}
			
			renderEngine.bindTexture(locationhalolunePng);


			EVector posm = new EVector(3);


			posm.set(ExtinctionRefraction.refraction(StellarSky.getManager().Moon.getPosition(), true));

			if(VecMath.getZ(posm)>0.0f){
				double sizem=StellarSky.getManager().Moon.radius.asDouble()/Spmath.getD(VecMath.size(posm))*98.0*5.0;

				posm.set(VOp.normalize(posm));
				difm.set(VOp.normalize(CrossUtil.cross((IEVector)posm, (IEVector)new EVector(0.0,0.0,1.0))));
				difm2.set((IValRef)CrossUtil.cross((IEVector)difm, (IEVector)posm));
				posm.set(VecMath.mult(98.0, posm));

				difm.set(VecMath.mult(sizem, difm));
				difm2.set(VecMath.mult(sizem, difm2));

				float alpha=Optics.getAlphaFromMagnitude(-17.0-StellarSky.getManager().Moon.mag,bglight);

				GlStateManager.color(1.0f, 1.0f, 1.0f, weathereff*alpha);

				worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				worldrenderer.pos(VecMath.getX(posm)+VecMath.getX(difm), VecMath.getY(posm)+VecMath.getY(difm), VecMath.getZ(posm)+VecMath.getZ(difm)).tex(0.0,0.0).endVertex();
				worldrenderer.pos(VecMath.getX(posm)+VecMath.getX(difm2), VecMath.getY(posm)+VecMath.getY(difm2), VecMath.getZ(posm)+VecMath.getZ(difm2)).tex(0.0,1.0).endVertex();
				worldrenderer.pos(VecMath.getX(posm)-VecMath.getX(difm), VecMath.getY(posm)-VecMath.getY(difm), VecMath.getZ(posm)-VecMath.getZ(difm)).tex(1.0,1.0).endVertex();
				worldrenderer.pos(VecMath.getX(posm)-VecMath.getX(difm2), VecMath.getY(posm)-VecMath.getY(difm2), VecMath.getZ(posm)-VecMath.getZ(difm2)).tex(1.0,0.0).endVertex();
				tessellator1.draw();
			}


			renderEngine.bindTexture(locationMoonPng);


			for(longc=0; longc<longn; longc++){
				for(latc=0; latc<latn; latc++){

					int longcd=(longc+1)%longn;
					double longd=(double)longc/(double)longn;
					double latd=1.0-(double)latc/(double)latn;
					double longdd=(double)longcd/(double)longn;
					double latdd=1.0-(double)(latc+1)/(double)latn;

					GlStateManager.color(1.0f, 1.0f, 1.0f, (weathereff*(float)moonilum[longc][latc]-4.0f*bglight)*2.0f);

					worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
					worldrenderer.pos(VecMath.getX(moonvec[longc][latc]), VecMath.getY(moonvec[longc][latc]), VecMath.getZ(moonvec[longc][latc])).tex(Spmath.fmod(longd+0.5, 1.0), latd).endVertex();
					worldrenderer.pos(VecMath.getX(moonvec[longcd][latc]), VecMath.getY(moonvec[longcd][latc]), VecMath.getZ(moonvec[longcd][latc])).tex(Spmath.fmod(longdd+0.5,1.0), latd).endVertex();
					worldrenderer.pos(VecMath.getX(moonvec[longcd][latc+1]), VecMath.getY(moonvec[longcd][latc+1]), VecMath.getZ(moonvec[longcd][latc+1])).tex(Spmath.fmod(longdd+0.5, 1.0), latdd).endVertex();
					worldrenderer.pos(VecMath.getX(moonvec[longc][latc+1]), VecMath.getY(moonvec[longc][latc+1]), VecMath.getZ(moonvec[longc][latc+1])).tex(Spmath.fmod(longd+0.5,1.0), latdd).endVertex();
					tessellator1.draw();
				}
			}
			//Moon
			
			//Galaxy
			for(longc=0; longc<longn; longc++){
				for(latc=0; latc<=latn; latc++){
					Buf.set(new SpCoord(longc*360.0/longn + 90.0, latc*180.0/latn - 90.0).getVec());
					Buf.set(VecMath.mult(50.0, Buf));
					IValRef ref = Transforms.EqtoEc.transform(Buf);
					ref = Transforms.ZTEctoNEc.transform(ref);
					ref = Transforms.EctoEq.transform(ref);
					ref = Transforms.NEqtoREq.transform(ref);
					ref = Transforms.REqtoHor.transform(ref);
					ref = ExtinctionRefraction.refraction(ref, true);

					moonvec[longc][latc] = new EVector(3);
					moonvec[longc][latc].set(ExtinctionRefraction.refraction(ref, true));
				}
			}
						
			mc.renderEngine.bindTexture(locationMilkywayPng);
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			
			float Mag = 3.5f;
			float alpha=Optics.getAlphaForGalaxy(Mag, bglight) - (((1-weathereff)/1)*20f);
			
			GlStateManager.color(1.0f, 1.0f, 1.0f, alpha*2.0f);
			GlStateManager.disableAlpha();
			
			for(longc=0; longc<longn; longc++){
				for(latc=0; latc<latn; latc++){
					int longcd=(longc+1)%longn;
					double longd=1.0-(double)longc/(double)longn;
					double latd=1.0-(double)latc/(double)latn;
					double longdd=1.0-(double)(longc+1)/(double)longn;
					double latdd=1.0-(double)(latc+1)/(double)latn;

					worldrenderer.pos(VecMath.getX(moonvec[longc][latc]), VecMath.getY(moonvec[longc][latc]), VecMath.getZ(moonvec[longc][latc])).tex(longd, latd).endVertex();
					worldrenderer.pos(VecMath.getX(moonvec[longc][latc+1]), VecMath.getY(moonvec[longc][latc+1]), VecMath.getZ(moonvec[longc][latc+1])).tex(longd, latdd).endVertex();
					worldrenderer.pos(VecMath.getX(moonvec[longcd][latc+1]), VecMath.getY(moonvec[longcd][latc+1]), VecMath.getZ(moonvec[longcd][latc+1])).tex(longdd, latdd).endVertex();
					worldrenderer.pos(VecMath.getX(moonvec[longcd][latc]), VecMath.getY(moonvec[longcd][latc]), VecMath.getZ(moonvec[longcd][latc])).tex(longdd, latd).endVertex();
				}
			}
			tessellator1.draw();
			//Galaxy

			renderEngine.bindTexture(locationStarPng);
			for(Planet planet : StellarSky.getManager().planets)
				this.DrawStellarObj(bglight, weathereff, planet.appPos, planet.appMag);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableFog();
            GlStateManager.popMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.color(0.0F, 0.0F, 0.0F);
            double d0 = this.mc.thePlayer.getPositionEyes(partialTicks).yCoord - theWorld.getHorizon();

            if (d0 < 0.0D)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 12.0F, 0.0F);

                if (this.vboEnabled)
                {
                    this.sky2VBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    this.sky2VBO.drawArrays(7);
                    this.sky2VBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                }
                else
                {
                    GlStateManager.callList(this.glSkyList2);
                }

                GlStateManager.popMatrix();
                float f18 = 1.0F;
                float f19 = -((float)(d0 + 65.0D));
                float f20 = -1.0F;
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(-1.0D, (double)f19, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, (double)f19, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, (double)f19, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, (double)f19, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, (double)f19, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, (double)f19, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, (double)f19, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, (double)f19, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
            }

            if (theWorld.provider.isSkyColored())
            {
                GlStateManager.color(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
            }
            else
            {
                GlStateManager.color(f, f1, f2);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -((float)(d0 - 16.0D)), 0.0F);
            GlStateManager.callList(this.glSkyList2);
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
        }
    }

	EVector dif = new EVector(3);
	EVector dif2 = new EVector(3);

	public void RenderStar(float bglight, float weathereff, double time){

		VertexBuffer worldRenderer = tessellator1.getBuffer();
		
		renderEngine.bindTexture(locationStarPng);

		EVector pos = new EVector(3);
		
		GlStateManager.disableAlpha();
		
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		for(int i=0; i<BrStar.NumStar; i++){
			if(BrStar.stars[i].unable) continue;

			BrStar star=BrStar.stars[i];

			pos.set(VecMath.normalize(star.appPos));
			float Mag=star.App_Mag;
			float B_V=star.App_B_V;

			if(Mag > StellarSky.proxy.getClientSettings().mag_Limit)
				continue;

			if(VecMath.getZ(pos)<0) continue;

			float size=0.5f;
			float alpha=Optics.getAlphaFromMagnitudeSparkling(Mag, bglight);

			dif.set(CrossUtil.cross(pos, new EVector(0.0,0.0,1.0)));
			if(Spmath.getD(VecMath.size2(dif)) < 0.01)
				dif.set(CrossUtil.cross(pos, new EVector(0.0,1.0,0.0)));
			dif.set(VecMath.normalize(dif));
			dif2.set((IValRef)CrossUtil.cross(dif, pos));
			pos.set(VecMath.mult(100.0, pos));

			dif.set(VecMath.mult(size, dif));
			dif2.set(VecMath.mult(size, dif2));

			Color c=Color.getColor(B_V);
			
			int ialpha = (int)(weathereff*alpha*255.0);
			if(ialpha < 0)
				ialpha = 0;

			worldRenderer.pos(VecMath.getX(pos)+VecMath.getX(dif), VecMath.getY(pos)+VecMath.getY(dif), VecMath.getZ(pos)+VecMath.getZ(dif)).tex(0.0,0.0).color(c.r, c.g, c.b, ialpha).endVertex();
			worldRenderer.pos(VecMath.getX(pos)+VecMath.getX(dif2), VecMath.getY(pos)+VecMath.getY(dif2), VecMath.getZ(pos)+VecMath.getZ(dif2)).tex(1.0,0.0).color(c.r, c.g, c.b, ialpha).endVertex();
			worldRenderer.pos(VecMath.getX(pos)-VecMath.getX(dif), VecMath.getY(pos)-VecMath.getY(dif), VecMath.getZ(pos)-VecMath.getZ(dif)).tex(1.0,1.0).color(c.r, c.g, c.b, ialpha).endVertex();
			worldRenderer.pos(VecMath.getX(pos)-VecMath.getX(dif2), VecMath.getY(pos)-VecMath.getY(dif2), VecMath.getZ(pos)-VecMath.getZ(dif2)).tex(0.0,1.0).color(c.r, c.g, c.b, ialpha).endVertex();
		}
		
		tessellator1.draw();
		GlStateManager.enableAlpha();
	}


	EVector difm = new EVector(3);
	EVector difm2 = new EVector(3);

	public void DrawStellarObj(float bglight, float weathereff, EVector pos, double Mag){

		if(Mag > StellarSky.proxy.getClientSettings().mag_Limit) return;
		if(VecMath.getZ(pos)<0) return;

		VertexBuffer worldRenderer = tessellator1.getBuffer();

		float size=0.6f;
		float alpha=Optics.getAlphaFromMagnitude(Mag, bglight);

		pos.set(VecMath.normalize(pos));

		difm.set(CrossUtil.cross((IEVector)pos, (IEVector)new EVector(0.0,0.0,1.0)));
		if(Spmath.getD(VecMath.size2(difm)) < 0.01)
			difm.set(CrossUtil.cross((IEVector)pos, (IEVector)new EVector(0.0,1.0,0.0)));
		difm.set(VecMath.normalize(difm));
		difm2.set((IValRef)CrossUtil.cross((IEVector)difm, (IEVector)pos));
		pos.set(VecMath.mult(99.0, pos));

		difm.set(VecMath.mult(size, difm));
		difm2.set(VecMath.mult(size, difm2));

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldRenderer.pos(VecMath.getX(pos)+VecMath.getX(difm), VecMath.getY(pos)+VecMath.getY(difm), VecMath.getZ(pos)+VecMath.getZ(difm)).tex(0.0,0.0).color(1.0f, 1.0f, 1.0f, weathereff * alpha).endVertex();
		worldRenderer.pos(VecMath.getX(pos)+VecMath.getX(difm2), VecMath.getY(pos)+VecMath.getY(difm2), VecMath.getZ(pos)+VecMath.getZ(difm2)).tex(1.0,0.0).color(1.0f, 1.0f, 1.0f, weathereff * alpha).endVertex();
		worldRenderer.pos(VecMath.getX(pos)-VecMath.getX(difm), VecMath.getY(pos)-VecMath.getY(difm), VecMath.getZ(pos)-VecMath.getZ(difm)).tex(1.0,1.0).color(1.0f, 1.0f, 1.0f, weathereff * alpha).endVertex();
		worldRenderer.pos(VecMath.getX(pos)-VecMath.getX(difm2), VecMath.getY(pos)-VecMath.getY(difm2), VecMath.getZ(pos)-VecMath.getZ(difm2)).tex(0.0,1.0).color(1.0f, 1.0f, 1.0f, weathereff * alpha).endVertex();

		tessellator1.draw();
	}
}
