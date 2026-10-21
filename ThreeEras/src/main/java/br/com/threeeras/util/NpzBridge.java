package br.com.threeeras.util;

import java.io.File;
import java.nio.file.Files;

public final class NpzBridge {
    private NpzBridge() {}

    public static void loadManifest(File baseDir) {
        File mf = new File(baseDir, "manifest.json");
        if (!mf.exists()) {
            System.out.println("[ThreeEras] manifest.json não encontrado em " + baseDir);
            return;
        }
        try {
            String json = new String(Files.readAllBytes(mf.toPath()));
            System.out.println("[ThreeEras] manifest carregado: " + json.length() + " bytes");
        } catch (Exception e) {
            System.out.println("[ThreeEras] erro ao ler manifest: " + e.getMessage());
        }
    }
}
