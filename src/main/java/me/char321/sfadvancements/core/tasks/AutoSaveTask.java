package me.char321.sfadvancements.core.tasks;

import java.io.IOException;
import java.util.logging.Level;
import me.char321.sfadvancements.SFAdvancements;

public class AutoSaveTask implements Runnable {
    @Override
    public void run() {
        try {
            SFAdvancements.getAdvManager().save();
        } catch (IOException e) {
            SFAdvancements.logger().log(Level.SEVERE, e, () -> "无法保存进度!");
        }
    }
}
