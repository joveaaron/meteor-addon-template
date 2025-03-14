package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BedrockFinder extends Module {
    SettingGroup sgDefault = settings.getDefaultGroup();
    private final Setting<Integer> searchY = sgDefault.add(new IntSetting.Builder()
            .name("Y")
            .range(-64, 128)
            .sliderRange(-64, 128)
                    .defaultValue(123)
            .build());
    private final Setting<String> savePath = sgDefault.add(new StringSetting.Builder()
            .name("Path")
            .defaultValue("D:/br.txt")
            .build());
    public static ExecutorService executor = Executors.newSingleThreadExecutor();

    public BedrockFinder() {
        super(AddonTemplate.CATEGORY, "BedrockFinder", "");
    }
   LinkedHashSet<BlockPos> bedrockPos = new LinkedHashSet<>();

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        executor.execute(()->{
            Chunk c = event.chunk();
            for (int x = c.getPos().getStartX(); x <= c.getPos().getEndX(); x++) {
                for (int z = c.getPos().getStartZ(); z <= c.getPos().getEndZ(); z++) {
                    BlockPos sPos = new BlockPos(x, searchY.get(), z);
                    if (c.getBlockState(sPos).getBlock().equals(Blocks.BEDROCK))
                        bedrockPos.add(sPos);
                }
            }
        });
    }

    @Override
    public void onDeactivate() {
        BufferedWriter bw = null;
        try {
            File f = new File(savePath.get());
            if (f.exists() && f.canWrite()) {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
                for (BlockPos pos : bedrockPos) {
                    String s = pos.getX() + " " + pos.getY() + " " + pos.getZ() +
                            " Bedrock";
                    bw.write(s);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            }else {
                ChatUtils.sendMsg(Text.of("File Not Found"));
            }
        } catch (Exception e) {
            ChatUtils.sendMsg(Text.of(e.getMessage()));
        } finally {
            bedrockPos.clear();
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
