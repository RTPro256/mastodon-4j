package org.joinmastodon.media.processing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.joinmastodon.media.config.MediaProperties;
import org.springframework.stereotype.Service;

@Service
public class FfmpegService {
    private final MediaProperties properties;

    public FfmpegService(MediaProperties properties) {
        this.properties = properties;
    }

    public boolean isAvailable() {
        return canExecute(properties.getFfmpegPath(), List.of("-version"));
    }

    public boolean isProbeAvailable() {
        return canExecute(properties.getFfprobePath(), List.of("-version"));
    }

    public Path generateVideoPreview(Path input, Path outputImage) throws IOException, InterruptedException {
        Files.createDirectories(outputImage.getParent());
        List<String> command = List.of(
                properties.getFfmpegPath(),
                "-y",
                "-i", input.toString(),
                "-ss", "00:00:01",
                "-vframes", "1",
                outputImage.toString()
        );
        execute(command);
        return outputImage;
    }

    public Path transcodeToMp4(Path input, Path output) throws IOException, InterruptedException {
        Files.createDirectories(output.getParent());
        List<String> command = List.of(
                properties.getFfmpegPath(),
                "-y",
                "-i", input.toString(),
                "-movflags", "+faststart",
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-crf", "23",
                "-c:a", "aac",
                output.toString()
        );
        execute(command);
        return output;
    }

    private void execute(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IOException("ffmpeg command failed with exit code " + exit);
        }
    }

    private boolean canExecute(String binary, List<String> args) {
        try {
            List<String> command = new java.util.ArrayList<>();
            command.add(binary);
            command.addAll(args);
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor() == 0;
        } catch (Exception ex) {
            return false;
        }
    }
}
