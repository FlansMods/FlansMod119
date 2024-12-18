package com.flansmod.teams.common.info;

import javax.annotation.Nonnull;
import java.util.UUID;

public record KillInfo(@Nonnull UUID killer,
					   @Nonnull UUID killed,
					   @Nonnull UUID weaponID,
					   long tick,
					   boolean headshot)
{
}
