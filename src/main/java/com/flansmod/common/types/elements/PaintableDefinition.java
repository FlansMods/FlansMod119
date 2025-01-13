package com.flansmod.common.types.elements;

import com.flansmod.common.types.JsonField;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PaintableDefinition
{
	@JsonField
	public PaintjobDefinition[] paintjobs = new PaintjobDefinition[0];

	public static final PaintableDefinition Invalid = new PaintableDefinition();
	public boolean IsValid()
	{
		return paintjobs.length > 0;
	}


	@Nonnull
	public List<PaintjobDefinition> getBasicPaintjobs()
	{
		List<PaintjobDefinition> paints = new ArrayList<>(paintjobs.length);
		for(PaintjobDefinition paint : paintjobs)
			if(paint.entitlementKey.isEmpty())
				paints.add(paint);
		return paints;
	}
	@Nonnull
	public List<PaintjobDefinition> getPremiumPaintjobs()
	{
		List<PaintjobDefinition> paints = new ArrayList<>(paintjobs.length);
		for(PaintjobDefinition paint : paintjobs)
			if(!paint.entitlementKey.isEmpty())
				paints.add(paint);
		return paints;
	}
}
