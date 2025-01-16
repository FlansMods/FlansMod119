package com.flansmod.common.types.bullets.elements;

import com.flansmod.common.types.Constants;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.abilities.elements.EAbilityTarget;

public class TrailDefinition
{
    @JsonField(Docs = "The particle to be spawned")
    public String particle = "";

    @JsonField(Docs = "Particle initial speed")
    public float speed = 0;

    @JsonField(Docs = "When to start spawning")
    public int fromTick = 0;

    @JsonField(Docs = "When to stop")
    public int toTick = Integer.MAX_VALUE;

    @JsonField(Docs = "Which model parts to spawn on. If no input will default to the centre of the projectile")
    public String[] spawnPoints = new String[0];

    @JsonField(Docs = "Can we spawn underwater")
    public boolean spawnUnderwater = false;

    @JsonField(Docs = "Should we only spawn underwater")
    public boolean onlySpawnUnderwater = false;
}
