# VickyE's Utility Plugin
**Currently in Development**

### Version 0.0.1:
**Particle System**
- (ParticleTask.java) was made for easy addition of particles to moves instead of using the default project korra 'Element Particle System' or there about.
- System allows for use of both redstone and dust particle transition. but it is to note that when entering parameters the first color is for the REDSTONE and the other two colors are for the DUST_COLOR_TRANSITION
- The System also implements a ParticleEffect(Pattern) Module where from a list of in-built Presets u can create Paths for the particles to follow
- Because of the sheer complexity of this System the parameters needed are a GODLY amount.
```java
    public ParticleTask(long startTime, Arrow arrow, double radiusH, double radiusM, double heightStep, Color headColor, Color transitionColorStart, Color transitionColorEnd,
                        int headCount, int middleCount,
                        double spreadXH, double spreadYH, double spreadZH,
                        double spreadXM, double spreadYM, double spreadZM,
                        float speedH, float speedM,
                        long lagBehind, double backwardVelocity,
                        float sizeH, float sizeM,
                        Particle particleH, Particle particleM,
                        ParticleTypeEffect.ParticleTypeEffects effectTypeH, ParticleTypeEffect.ParticleTypeEffects effectTypeM,
                        double rFreq, double pFreq, double angleStep, ParticleTypeEffect.SpacingMode spacingMode, int circleNumber,
                        float yaw, float pitch)
```
**DeathMessage and Cause System**
- This was done by implementing a `Metadata` to the player of "customCause" that can allow for different types so as to make different deathMessages for each cause (allows for more than one type). It uses this Implementation:
```java
          try {
                Map<String, CustomEffect> effects = new HashMap<>();

                Bleeding bleeding = new Bleeding(this); // The Effect
                effects.put("bleeding", bleeding);

                DeathMessages.add("bleeding", "{player} bled to death.");
                DeathMessages.add("bleeding", "{player} couldn't stop the bleeding.");

                getServer().getPluginManager().registerEvents(new DeathListener(effects), this);
                getServer().getPluginManager().registerEvents(new SpawnListener(customDamageHandler), this);

                getLogger().info(ANSIColor.colorize("Effects and DeathMessages have been sucessfully Registered.", ANSIColor.PURPLE));
            }catch(Exception e){
                exceptionOccurred = true; // was called in the file as boolean exceptionOccured = false; its internal
                getLogger().info(ANSIColor.colorize("Unable to register Effects and DeathMessages..... Error:" + e.getMessage(), ANSIColor.RED_BOLD));
            }finally {
                if (exceptionOccurred) {
                    getLogger().info(ANSIColor.colorize("Continuing with Loading Some Errors Might Occur", ANSIColor.LIGHT_RED));
                }
            }
```
**Custom Effects**
- It uses a custom Effects Implementation class to make custom effects to run custom events or things on the specified `Living Entity`. It also works seamlessly works with the custom Death Messages. an example implementation is this:
```java
  public class Bleeding implements CustomEffect {

    private final Plugin plugin;
    private final Map<UUID, Integer> bleedingEntities = new HashMap<>();
    private final EffectType effectType = EffectType.HARMFUL;

    public Bleeding(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void applyEffect(LivingEntity entity, int durationInSeconds, int level) {
        if (bleedingEntities.containsKey(entity.getUniqueId())) {
            // Entity is already bleeding
            return;
        }

        entity.setMetadata("isBleeding_v", new FixedMetadataValue(plugin, true));
        // Add the entity to the bleeding list
        bleedingEntities.put(entity.getUniqueId(), durationInSeconds);

        // Schedule a repeating task to deal damage over time
        new BukkitRunnable() {
            int remainingDuration = durationInSeconds;

            @Override
            public void run() {
                if (remainingDuration <= 0 || !bleedingEntities.containsKey(entity.getUniqueId())) {
                    stopEffect(entity);  // Stop the bleeding
                    this.cancel();  // Stop the task
                    return;
                }

                if (!entity.isDead() && entity.isValid()) {
                    // Apply potion effect and custom damage
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, level, true, false, false));
                    applyCustomBleedingDamage(entity, level);
                }

                remainingDuration--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20L is 1 second (20 ticks)
    }

    @Override
    public void stopEffect(LivingEntity entity) {
        if (bleedingEntities.containsKey(entity.getUniqueId())) {
            entity.removeMetadata("isBleeding_v", plugin);
            bleedingEntities.remove(entity.getUniqueId());
            entity.sendMessage("Your bleeding has stopped.");
        }
    }

    @Override
    public boolean isEntityAffected(LivingEntity entity) {
        return bleedingEntities.containsKey(entity.getUniqueId());
    }

    @Override
    public EffectType getEffectType() {
        return effectType;
    }

    private void applyCustomBleedingDamage(LivingEntity entity, int level) {
        vicky_utils pluginUtils = (vicky_utils) Bukkit.getPluginManager().getPlugin(plugin.getName());
        double customDamage = calculateBleedingDamage(level);

        assert pluginUtils != null;
        pluginUtils.getCustomDamageHandler().applyCustomDamage(entity, customDamage, "bleeding");
    }

    private double calculateBleedingDamage(int level) {
        // Damage calculation formula for bleeding
        return -34 * Math.exp(-0.5 * (level * Math.cos(34.1) + level * Math.sin(39))) + 34;
    }
}
```
- Lengthy hehe. But this is the general idea.

**ANSI colors and Hex Gradient Generators**
- This allows for colors to be added to Logs and to create Gradients. Thats all.