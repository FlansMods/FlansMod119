package com.flansmod.common.types.bullets;

import com.flansmod.common.types.Definitions;
public class BulletBagDefinitions extends Definitions<BulletBagDefinition>
{
	public BulletBagDefinitions()
	{
		super(BulletBagDefinition.FOLDER,
			  BulletBagDefinition.class,
			  BulletBagDefinition.INVALID,
			  BulletBagDefinition::new);
	}
}