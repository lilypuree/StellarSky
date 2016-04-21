package stellarium.stellars.star.brstar;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import stellarapi.api.celestials.EnumCelestialCollectionType;
import stellarapi.api.celestials.ICelestialObject;
import stellarapi.api.lib.config.IConfigHandler;
import stellarapi.api.lib.config.INBTConfig;
import stellarapi.api.lib.math.SpCoord;
import stellarium.StellarSky;
import stellarium.stellars.layer.IPerWorldImage;
import stellarium.stellars.layer.StellarObjectContainer;
import stellarium.stellars.star.BgStar;
import stellarium.stellars.star.LayerBgStar;
import stellarium.stellars.star.StarRenderCache;
import stellarium.stellars.star.StarWorldImage;
import stellarium.util.math.StellarMath;

public class LayerBrStar extends LayerBgStar<IConfigHandler, INBTConfig> {
	
	//constants
	public static final int NumStar=9110;
	public static final int Bufsize=198;

	//Zero-time axial tilt
	public static final double e=0.4090926;
	public static final Matrix3d EqtoEc = new Matrix3d();
	
	static {
		EqtoEc.set(new AxisAngle4d(1.0, 0.0, 0.0, -e));
	}

	//Initialization check
	private boolean IsInitialized=false;
	
	//File
	private byte str[];
	
	//stars
	private List<BgStar> stars = Lists.newArrayList();
	
	@Override
	public void initializeClient(IConfigHandler config,
			StellarObjectContainer<BgStar, IConfigHandler> container) throws IOException {
		this.loadStarData(StellarSky.proxy.getClientSettings().mag_Limit);
	}

	@Override
	public void initializeCommon(INBTConfig config,
			StellarObjectContainer<BgStar, IConfigHandler> container) throws IOException {
		if(!this.IsInitialized)
			this.loadStarData(4.0);
		
		for(BgStar star : this.stars)
		{
			container.loadObject("Star", star);
			container.addRenderCache(star, new StarRenderCache());
			
			//Delayed till there is code to select important stars
			// TODO select stars
			//container.addImageType(star, StarWorldImage.class);
		}
	}
	
	private void loadStarData(double magLimit) throws IOException {
		//Counter Variable
		int i, j, k;
		
		StellarSky.logger.info("Loading Bright Stars Data...");
		
		stars.clear();
		
		//Read
		str=new byte[NumStar*Bufsize];
		InputStream brs=BgStar.class.getResourceAsStream("/data/bsc5.dat");
	    BufferedInputStream bbrs = new BufferedInputStream(brs);
	    bbrs.read(str);
	    bbrs.close();
	    
	    //Input Star Information
	    j=0;
	    for(i=0; i<NumStar; i++)
	    {
	    	byte[] star_value = new byte[Bufsize];
	    	
	    	k=0;
	    	while(str[j]!='\n'){
	    		star_value[k]=str[j];
	    		j++;
	    		k++;
	    	}
	    	j++;
	    	

			if(star_value[103]==' ')
				continue;
			
			String name = new String(star_value).substring(4, 14);
			
			double mag=StellarMath.sgnize(star_value[102],
					(float)StellarMath.btoi(star_value, 103, 1)
					+StellarMath.btoi(star_value, 105, 2)*0.01f);

			double B_V=StellarMath.sgnize(star_value[109],
					(float)StellarMath.btoi(star_value, 110, 1)
					+StellarMath.btoi(star_value, 112, 2)*0.01f);

			//J2000
			double RA=StellarMath.btoi(star_value, 75, 2)*15.0f
					+StellarMath.btoi(star_value, 77, 2)/4.0f
					+StellarMath.btoi(star_value, 79, 2)/240.0f
					+StellarMath.btoi(star_value, 82, 1)/2400.0f;

			double Dec=StellarMath.sgnize(star_value[83],
					StellarMath.btoi(star_value, 84, 2)
					+StellarMath.btoi(star_value, 86, 2)/60.0f
					+StellarMath.btoi(star_value, 88, 2)/3600.0f);

			Vector3d pos = new SpCoord(RA, Dec).getVec();
			EqtoEc.transform(pos);

			if(mag > magLimit)
				continue;
			
			star_value=null;
			
	    	stars.add(new BgStar(name, mag, B_V, pos));
	    }
	    
	    str=null;
	    
	    StellarSky.logger.info("Bright Stars are Loaded!");
	    
	    IsInitialized=true;
	}

	@Override
	public String getName() {
		return "Bright Stars";
	}

	@Override
	public int searchOrder() {
		return 1;
	}

	@Override
	public boolean isBackground() {
		return true;
	}

	@Override
	public EnumCelestialCollectionType getCollectionType() {
		return EnumCelestialCollectionType.Stars;
	}

	@Override
	public Comparator<ICelestialObject> getDistanceComparator(SpCoord pos) {
		return null;
	}

	@Override
	public Predicate<ICelestialObject> conditionInRange(SpCoord pos, double radius) {
		return null;
	}

	@Override
	public Map<BgStar, IPerWorldImage> temporalLoadImagesInRange(SpCoord pos, double radius) {
		return null;
	}

}
