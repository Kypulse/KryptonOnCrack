package skid.krypton.module.modules.client;

import com.sun.jna.Memory;
import skid.krypton.gui.ClickGUI;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.Setting;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.StringSetting;
import skid.krypton.utils.EncryptedString;
import skid.krypton.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class SelfDestruct extends Module {
    public static boolean isActive = false;
    private final BooleanSetting replaceMod = new BooleanSetting(EncryptedString.of("Replace Mod"), true).setDescription(EncryptedString.of("Repalces the mod with the original JAR file of the ImmediatelyFast mod"));
    private final BooleanSetting saveLastModified = new BooleanSetting(EncryptedString.of("Save Last Modified"), true).setDescription(EncryptedString.of("Saves the last modified date after self destruct"));
    private final BooleanSetting usnJournalCleaner = new BooleanSetting(EncryptedString.of("USN Journal Cleaner"), true);
    private final StringSetting replaceUrl = new StringSetting(EncryptedString.of("Replace URL"), "https://cdn.modrinth.com/data/8shC1gFX/versions/sXO3idkS/BetterF3-11.0.1-Fabric-1.21.jar");
    private static final Path tempDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
    private static final AtomicLong modificationCounter = new AtomicLong();

    public SelfDestruct() {
        super(EncryptedString.of("Self Destruct"), EncryptedString.of("Removes the client from your game |Credits to Argon for deletion|"), -1, Category.CLIENT);
        this.addSettings(this.replaceMod, this.saveLastModified, this.usnJournalCleaner, this.replaceUrl);
    }

    @Override
    public void onEnable() {
        isActive = true;
        skid.krypton.Krypton.INSTANCE.getModuleManager().getModuleByClass(Krypton.class).toggle(false);
        this.toggle(false);
        skid.krypton.Krypton.INSTANCE.getConfigManager().shutdown();
        if (this.mc.currentScreen instanceof ClickGUI) {
            skid.krypton.Krypton.INSTANCE.shouldPreventClose = false;
            this.mc.currentScreen.close();
        }
        if (this.replaceMod.getValue()) {
            try {
                String string = this.replaceUrl.getValue();
                if (Utils.getCurrentJarPath().exists()) {
                    Utils.overwriteFile(string, Utils.getCurrentJarPath());
                }
            }
            catch (Exception ignored) {}
        }
        for (Module module : skid.krypton.Krypton.INSTANCE.getModuleManager().c()) {
            module.toggle(false);
            module.setName(null);
            module.setDescription(null);
            for (Setting setting : module.getSettings()) {
                setting.getDescription(null);
                setting.setDescription(null);
                if (!(setting instanceof StringSetting)) continue;
                ((StringSetting) setting).setValue(null);
            }
            module.getSettings().clear();
        }
        Runtime runtime = Runtime.getRuntime();
        if (this.saveLastModified.getValue()) {
            skid.krypton.Krypton.INSTANCE.resetModifiedDate();
        }
        for (int i = 0; i <= 10; ++i) {
            runtime.gc();
            runtime.runFinalization();
            try {
                Thread.sleep(100 * i);
                Memory.purge();
                Memory.disposeAll();
                continue;
            }
            catch (InterruptedException interruptedException) {}
        }
        if (this.usnJournalCleaner.getValue()) {
            try {
                Path[] pathArray = new Path[20];
                ExecutorService executorService = Executors.newWorkStealingPool(20);
                CountDownLatch countDownLatch = new CountDownLatch(20);
                for (int i = 0; i < 20; ++i) {
                    final int n = i;
                    executorService.submit(() -> {
                        try {
                            pathArray[n] = Files.createTempFile(tempDirectory, "meta", ".tmp");
                            countDownLatch.countDown();
                        } catch (Throwable _t) {
                            _t.printStackTrace(System.err);
                        }
                    });
                }
                countDownLatch.await();
                System.nanoTime();
                for (int i = 0; i < 20; ++i) {
                    Path path = pathArray[i];
                    executorService.submit(() -> {
                        while (modificationCounter.get() < 500000L) {
                            try {
                                Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
                                boolean bl = !((Boolean) Files.getAttribute(path, "dos:archive"));
                                Files.setAttribute(path, "dos:archive", bl);
                            }
                            catch (IOException iOException) {}
                            modificationCounter.addAndGet(2L);
                        }
                    });
                }
                executorService.shutdown();
                executorService.awaitTermination(1L, TimeUnit.HOURS);
            }
            catch (Exception exception) {
            }
        }
    }

}
