package skid.krypton.module.modules.donut;

import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.player.PlayerEntity;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.TickEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.module.setting.StringSetting;
import skid.krypton.utils.embed.DiscordWebhook;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Set;

public final class TunnelBaseFinderUltraStealth extends Module {
    private final BooleanSetting enableSpawners = new BooleanSetting("Detect Spawners", true);
    private final BooleanSetting enableStorage = new BooleanSetting("Detect Storage", true);
    private final NumberSetting storageThreshold = new NumberSetting("Storage Items Needed", 1, 100, 5, 1);
    private final BooleanSetting enableDiscord = new BooleanSetting("Send Webhook", false);
    private final StringSetting webhookUrl = new StringSetting("Webhook URL", "");
    private final BooleanSetting selfPing = new BooleanSetting("Self Ping", false);
    private final StringSetting discordId = new StringSetting("Discord ID", "");
    private final NumberSetting delayMin = new NumberSetting("Min Delay", 4, 20, 8, 1);
    private final NumberSetting delayMax = new NumberSetting("Max Delay", 10, 60, 20, 1);

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Random random = new Random();

    // Blacklisted players
    private final Set<String> blacklistedPlayers = Set.of(
            "archivepedro", "shyaly", "itszdeath"
    );

    private int tickDelay = 0;
    private int idleTicks = 0;
    private boolean webhookSent = false;

    // Human-like rotation system
    private float targetYaw = 0;
    private float targetPitch = 0;
    private float currentYawVel = 0;
    private float currentPitchVel = 0;

    // Emergency systems
    private boolean emergencyStop = false;
    private boolean wasHit = false;
    private int emergencyWaitTicks = 0;
    private float lastHealth = 20.0f;

    public TunnelBaseFinderUltraStealth() {
        super("TunnelBaseFinder+", "Undetectable tunnel-based base finder", -1, Category.DONUT);
        addSettings(enableSpawners, enableStorage, storageThreshold, enableDiscord, webhookUrl, selfPing, discordId, delayMin, delayMax);
    }

    @Override
    public void onEnable() {
        tickDelay = randomDelay();
        idleTicks = 0;
        webhookSent = false;
        emergencyStop = false;
        wasHit = false;
        emergencyWaitTicks = 0;

        if (mc.player != null) {
            lastHealth = mc.player.getHealth();
            targetYaw = mc.player.getYaw();
            targetPitch = mc.player.getPitch();
        }
    }

    @Override
    public void onDisable() {
        mc.options.forwardKey.setPressed(false);
        mc.options.attackKey.setPressed(false);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Check for emergency situations
        if (checkEmergencyConditions()) {
            handleEmergency();
            return;
        }

        // Handle post-hit emergency wait
        if (wasHit) {
            handlePostHitWait();
            return;
        }

        if (tickDelay-- > 0) return;
        tickDelay = randomDelay();

        // Continue walking forward
        mc.options.forwardKey.setPressed(true);

        // Handle mining
        if (mc.crosshairTarget instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            if (!mc.world.getBlockState(pos).isAir()) {
                simulateMining(pos);
            }
        }

        // Idle activities
        if (++idleTicks > 100 && random.nextInt(5) == 0) {
            idleTicks = 0;
            performIdleActivity();
        }

        // Smooth human-like head movement
        updateSmoothRotation();

        // Check for spawners and storage
        if (!webhookSent) {
            if (enableSpawners.getValue()) {
                BlockPos spawnerPos = findSpawner();
                if (spawnerPos != null) {
                    System.out.println("[TunnelBaseFinder] SPAWNER FOUND AT: " + spawnerPos);
                    webhookSent = true;
                    sendBaseFindWebhook("Spawner Found", spawnerPos, 1, 0, 0, 0, 0, 0, 0, 0);
                    disconnect("Spawner Found");
                    toggle();
                    return;
                }
            }

            if (enableStorage.getValue()) {
                StorageResult storageResult = findStorage();
                if (storageResult != null) {
                    System.out.println("[TunnelBaseFinder] STORAGE FOUND: " + storageResult.getTotal() + " blocks, " + storageResult.itemCount + " items");
                    webhookSent = true;
                    sendBaseFindWebhook("Storage Found", storageResult.position, 0,
                            storageResult.chests, storageResult.barrels, storageResult.enderChests,
                            storageResult.shulkers, storageResult.hoppers, storageResult.furnaces,
                            storageResult.dispensersDroppers);
                    disconnect("Storage Found");
                    toggle();
                    return;
                }
            }
        }

        // Update last health for hit detection
        lastHealth = mc.player.getHealth();
    }

    private boolean checkEmergencyConditions() {
        // Check for blacklisted players
        for (PlayerEntity player : mc.world.getPlayers()) {
            String playerName = player.getGameProfile().getName().toLowerCase();
            if (blacklistedPlayers.contains(playerName)) {
                return true;
            }
        }

        // Check if player was hit (health decreased)
        if (mc.player.getHealth() < lastHealth) {
            wasHit = true;
            emergencyWaitTicks = 400; // 20 seconds at 20 TPS
            return true;
        }

        return false;
    }

    private void handleEmergency() {
        // Stop all movement immediately
        mc.options.forwardKey.setPressed(false);
        mc.options.attackKey.setPressed(false);
        emergencyStop = true;
    }

    private void handlePostHitWait() {
        // Stand completely still
        mc.options.forwardKey.setPressed(false);
        mc.options.attackKey.setPressed(false);

        emergencyWaitTicks--;

        if (emergencyWaitTicks <= 0) {
            // Time's up, disconnect
            disconnect("Emergency Protocol Activated");
            toggle();
        }
    }

    private int randomDelay() {
        return delayMin.getIntValue() + random.nextInt(delayMax.getIntValue() - delayMin.getIntValue() + 1);
    }

    private void simulateMining(BlockPos pos) {
        // Slightly adjust aim toward block with human-like targeting
        double deltaX = pos.getX() + 0.5 - mc.player.getX();
        double deltaZ = pos.getZ() + 0.5 - mc.player.getZ();
        double deltaY = pos.getY() + 0.5 - mc.player.getEyeY();

        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float newYaw = (float) (MathHelper.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f;
        float newPitch = (float) -(MathHelper.atan2(deltaY, distance) * 180.0 / Math.PI);

        // Add slight human-like inaccuracy
        newYaw += (random.nextFloat() - 0.5f) * 2.5f;
        newPitch += (random.nextFloat() - 0.5f) * 2.0f;

        targetYaw = newYaw;
        targetPitch = MathHelper.clamp(newPitch, -90f, 90f);

        // Start mining
        KeyBinding attack = mc.options.attackKey;
        attack.setPressed(true);
        mc.player.swingHand(mc.player.getActiveHand());

        // Release after short delay
        new Thread(() -> {
            try {
                Thread.sleep(75 + random.nextInt(100));
            } catch (InterruptedException ignored) {}
            attack.setPressed(false);
        }).start();
    }

    private void performIdleActivity() {
        int activityType = random.nextInt(3);

        switch (activityType) {
            case 0: // Small head movement
                targetYaw += (random.nextFloat() - 0.5f) * 8f;
                targetPitch += (random.nextFloat() - 0.5f) * 5f;
                break;
            case 1: // Quick look around
                targetYaw += random.nextBoolean() ? 15f + random.nextFloat() * 10f : -(15f + random.nextFloat() * 10f);
                break;
            case 2: // Look up/down briefly
                targetPitch += random.nextBoolean() ? 10f + random.nextFloat() * 8f : -(10f + random.nextFloat() * 8f);
                break;
        }

        targetPitch = MathHelper.clamp(targetPitch, -90f, 90f);
    }

    private void updateSmoothRotation() {
        // Human-like smooth rotation with acceleration/deceleration
        float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDiff = targetPitch - mc.player.getPitch();

        // Apply smooth acceleration
        float maxYawSpeed = 3.0f + random.nextFloat() * 1.5f;
        float maxPitchSpeed = 2.0f + random.nextFloat() * 1.0f;

        currentYawVel = MathHelper.lerp(0.15f, currentYawVel, MathHelper.clamp(yawDiff * 0.25f, -maxYawSpeed, maxYawSpeed));
        currentPitchVel = MathHelper.lerp(0.15f, currentPitchVel, MathHelper.clamp(pitchDiff * 0.25f, -maxPitchSpeed, maxPitchSpeed));

        // Add subtle micro-jitter for realism
        if (random.nextInt(8) == 0) {
            currentYawVel += (random.nextFloat() - 0.5f) * 0.2f;
            currentPitchVel += (random.nextFloat() - 0.5f) * 0.15f;
        }

        // Apply rotation if significant enough
        if (Math.abs(currentYawVel) > 0.05f || Math.abs(currentPitchVel) > 0.05f) {
            mc.player.setYaw(mc.player.getYaw() + currentYawVel);
            mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + currentPitchVel, -90f, 90f));
        }
    }

    private BlockPos findSpawner() {
        BlockPos playerPos = mc.player.getBlockPos();
        int radius = 16;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockEntity(pos) instanceof MobSpawnerBlockEntity) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private StorageResult findStorage() {
        BlockPos playerPos = mc.player.getBlockPos();
        int radius = 16;
        int threshold = storageThreshold.getIntValue();

        int totalChests = 0, totalBarrels = 0, totalEnderChests = 0;
        int totalShulkers = 0, totalHoppers = 0, totalFurnaces = 0, totalDispensersDroppers = 0;
        int totalItems = 0;
        BlockPos firstFoundPos = null;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    var blockEntity = mc.world.getBlockEntity(pos);

                    if (blockEntity instanceof Inventory inventory) {
                        int itemCount = countItemsInInventory(inventory);

                        if (blockEntity instanceof ChestBlockEntity) {
                            totalChests++;
                            totalItems += itemCount;
                            if (firstFoundPos == null) firstFoundPos = pos;
                        } else if (blockEntity instanceof BarrelBlockEntity) {
                            totalBarrels++;
                            totalItems += itemCount;
                            if (firstFoundPos == null) firstFoundPos = pos;
                        } else if (blockEntity instanceof ShulkerBoxBlockEntity) {
                            totalShulkers++;
                            totalItems += itemCount;
                            if (firstFoundPos == null) firstFoundPos = pos;
                        } else if (blockEntity instanceof HopperBlockEntity) {
                            totalHoppers++;
                            totalItems += itemCount;
                            if (firstFoundPos == null) firstFoundPos = pos;
                        } else if (blockEntity instanceof DispenserBlockEntity || blockEntity instanceof DropperBlockEntity) {
                            totalDispensersDroppers++;
                            totalItems += itemCount;
                            if (firstFoundPos == null) firstFoundPos = pos;
                        } else if (blockEntity instanceof FurnaceBlockEntity) {
                            totalFurnaces++;
                            totalItems += itemCount;
                            if (firstFoundPos == null) firstFoundPos = pos;
                        }
                    }
                }
            }
        }

        if (totalItems >= threshold && firstFoundPos != null) {
            return new StorageResult(firstFoundPos, totalItems, totalChests, totalBarrels,
                    totalEnderChests, totalShulkers, totalHoppers, totalFurnaces, totalDispensersDroppers);
        }

        return null;
    }

    private int countItemsInInventory(Inventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.getStack(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static class StorageResult {
        public final BlockPos position;
        public final int itemCount;
        public final int chests, barrels, enderChests, shulkers, hoppers, furnaces, dispensersDroppers;

        public StorageResult(BlockPos position, int itemCount, int chests, int barrels,
                             int enderChests, int shulkers, int hoppers, int furnaces, int dispensersDroppers) {
            this.position = position;
            this.itemCount = itemCount;
            this.chests = chests;
            this.barrels = barrels;
            this.enderChests = enderChests;
            this.shulkers = shulkers;
            this.hoppers = hoppers;
            this.furnaces = furnaces;
            this.dispensersDroppers = dispensersDroppers;
        }

        public int getTotal() {
            return chests + barrels + enderChests + shulkers + hoppers + furnaces + dispensersDroppers;
        }
    }

    private void sendBaseFindWebhook(String title, BlockPos pos, int spawners, int chests, int barrels,
                                     int enderChests, int shulkers, int hoppers, int furnaces, int dispensersDroppers) {
        if (!enableDiscord.getValue()) {
            System.out.println("[TunnelBaseFinder] Discord webhook disabled in settings");
            return;
        }

        if (webhookUrl.getValue().trim().isEmpty()) {
            System.out.println("[TunnelBaseFinder] Webhook URL is empty");
            return;
        }

        System.out.println("[TunnelBaseFinder] Attempting to send webhook...");

        try {
            String playerName = mc.getSession().getUsername();
            BlockPos playerPos = mc.player.getBlockPos();

            String messageContent = "";
            if (selfPing.getValue() && !discordId.getValue().trim().isEmpty()) {
                messageContent = String.format("<@%s>", discordId.getValue().trim());
            }

            StringBuilder description = new StringBuilder();
            description.append("Player **").append(playerName).append("** discovered a base at coordinates **")
                    .append(pos.getX()).append(", ").append(pos.getY()).append(", ").append(pos.getZ()).append("** containing:\\n\\n");

            if (spawners > 0) description.append("🔥 **").append(spawners).append("** Spawner(s)\\n");
            if (chests > 0) description.append("📦 **").append(chests).append("** Chest(s)\\n");
            if (barrels > 0) description.append("🛢️ **").append(barrels).append("** Barrel(s)\\n");
            if (enderChests > 0) description.append("🎆 **").append(enderChests).append("** Ender Chest(s)\\n");
            if (shulkers > 0) description.append("📫 **").append(shulkers).append("** Shulker Box(es)\\n");
            if (hoppers > 0) description.append("⚙️ **").append(hoppers).append("** Hopper(s)\\n");
            if (furnaces > 0) description.append("🔥 **").append(furnaces).append("** Furnace(s)\\n");
            if (dispensersDroppers > 0) description.append("🎯 **").append(dispensersDroppers).append("** Dispenser(s)/Dropper(s)\\n");

            int totalStorage = chests + barrels + enderChests + shulkers + hoppers + furnaces + dispensersDroppers;
            description.append("\\n**Total Storage Blocks:** ").append(totalStorage);
            description.append("\\n**Player Position:** ").append(playerPos.getX()).append(", ")
                    .append(playerPos.getY()).append(", ").append(playerPos.getZ());

            String jsonPayload = String.format("""
                {
                  "content": "%s",
                  "username": "Krypton On Crack",
                  "avatar_url": "https://imgur.com/a/y5MvbOf",
                  "embeds": [
                    {
                      "title": "🏰 Base has been Confirmed!🏰 ",
                      "description": "%s",
                      "color": 16711680,
                      "author": {
                        "name": "Base and spawner alert"
                      },
                      "footer": { "text": "Sent by Krypton On Krack" }
                    }
                  ]
                }
                """, messageContent, description.toString());

            System.out.println("[TunnelBaseFinder] JSON Payload: " + jsonPayload);
            sendWebhookRequest(jsonPayload);
        } catch (Throwable e) {
            System.out.println("[TunnelBaseFinder] Webhook failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendWebhookRequest(String jsonPayload) {
        new Thread(() -> {
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(webhookUrl.getValue()))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                java.net.http.HttpResponse<String> response = client.send(request,
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200 && response.statusCode() != 204) {
                    System.out.println("[TunnelBaseFinder] Webhook failed with status: " + response.statusCode());
                }
            } catch (Exception e) {
                System.out.println("[TunnelBaseFinder] Webhook request failed: " + e.getMessage());
            }
        }).start();
    }

    private void disconnect(String reason) {
        mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.of("[TunnelBaseFinder] " + reason)));
    }
}
