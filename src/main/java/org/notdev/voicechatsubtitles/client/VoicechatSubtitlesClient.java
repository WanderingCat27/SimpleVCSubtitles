package org.notdev.voicechatsubtitles.client;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import org.notdev.config.Config;
import org.notdev.config.ConfigBuilder;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class VoicechatSubtitlesClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */

    String whisperVersion = "base";

    String python_script_text = "import threading\n"
            + "import time\n"
            + "import psutil\n"
            + "import os\n"
            + "import time\n"
            + "import fnmatch\n"
            + "import shutil\n"
            + "from whisper_jax import FlaxWhisperPipline\n"
            + "import sys\n"
            + "import atexit\n"
            + "def delete_directory(directory):\n"
            + " try:\n"
            + "     shutil.rmtree(directory)\n"
            + "     print(f\"Deleted directory: {directory}\")\n"
            + " except Exception as e:\n"
            + "     print(f\"Error deleting directory {directory}: {e}\")\n"
            + "def cleanup_on_exit():\n"
            + "# Replace these paths with the actual paths of the directories you want to delete\n"
            + "     directory1 = \"whisper-input\"\n"
            + "     directory2 = \"whisper-output\"\n"
            + "     directory3 = \"whisper\"\n"
            + "     delete_directory(directory1)\n"
            + "     delete_directory(directory2)\n"
            + "     delete_directory(directory3)\n"
            + "# Register the cleanup_on_exit function\n"
            + "atexit.register(cleanup_on_exit)\n"
            + "# Check if at least one command-line argument is provided\n"
            + "if len(sys.argv) < 2:\n"
            + " print(\"Usage: python programName.py <your_long_value>\")\n"
            + " sys.exit(1)\n"
            + "# Retrieve the long value from the command-line argument\n"
            + "try:\n"
            + " pid = int(sys.argv[1])\n"
            + "except ValueError:\n"
            + " print(\"Error: The provided value is not a valid integer.\")\n"
            + " sys.exit(1)\n"
            + "# Now you can use the \'long_value\' variable in your program\n"
            + "print(\"The provided long value is:\", pid)\n"
            + "pipeline = FlaxWhisperPipline(\"openai/whisper-base\")\n"
            + "sleep_time = 2\n"
            + "def monitor_directory(directory_path):\n"
            + "# Store the existing files in the directory\n"
            + " existing_files = set()\n"
            + " while True:\n"
            + "# kill if java mod stops\n"
            + "     if not is_java_running():\n"
            + "         exit(1)\n"
            + "# Get the list of files in the directory\n"
            + "     current_files = set(fnmatch.filter(os.listdir(directory_path), \'*.mp3\'))\n"
            + "# Find the newly added files\n"
            + "     new_files = current_files - existing_files\n"
            + "# Print the names of newly added .mp3 files\n"
            + "     for new_file in new_files:\n"
            + "         print(f\"New MP3 file added: {new_file}\")\n"
            + "         transcribe(\"whisper-input/\"+ new_file)\n"
            + "# Update the set of existing files\n"
            + "     existing_files = current_files\n"
            + "# Sleep for a short duration before checking again\n"
            + "     time.sleep(sleep_time) # You can adjust the sleep duration as needed\n"
            + "def delete_and_write(input_file_path, output_directory, output_file_name, content_to_write):\n"
            + "# Delete the existing file\n"
            + " try:\n"
            + "     os.remove(input_file_path)\n"
            + "     print(f\"File at {input_file_path} deleted successfully.\")\n"
            + " except OSError as e:\n"
            + "     print(f\"Error deleting the file at {input_file_path}: {e}\")\n"
            + "# Create the output directory if it doesn\'t exist\n"
            + " if not os.path.exists(output_directory):\n"
            + "     os.makedirs(output_directory)\n"
            + " print(f\"Output directory {output_directory} created.\")\n"
            + "# Write the content to the new file\n"
            + " output_file_path = os.path.join(output_directory, output_file_name)\n"
            + " try:\n"
            + "     with open(output_file_path, \'w\') as output_file:\n"
            + "         output_file.write(content_to_write)\n"
            + "         print(f\"Content written to {output_file_path} successfully.\")\n"
            + " except IOError as e:\n"
            + "     print(f\"Error writing to {output_file_path}: {e}\")\n"
            + "def transcribe(path):\n"
            + " print(\"starting\")\n"
            + " path_parts = path.split(\"/\")\n"
            + " desired_path = \"whisper-input/\" + path_parts[-1]\n"
            + "# JIT compile the forward call - slow, but we only do once\n"
            + " result = pipeline(desired_path, task=\"translate\")\n"
            + "# Example usage:\n"
            + " delete_and_write(\n"
            + " input_file_path=path,\n"
            + " output_directory=\'whisper-output\',\n"
            + " output_file_name=path_parts[-1]+\'.txt\',\n"
            + " content_to_write=result[\'text\']\n"
            + " )\n"
            + " return result\n"
            + "def is_java_running():\n"
            + " try:\n"
            + "     process = psutil.Process(pid)\n"
            + "     return process.is_running()\n"
            + " except psutil.NoSuchProcess:\n"
            + "     return False\n"
            + "if __name__ == \"__main__\":\n"
            + "# Replace \'your_directory_path\' with the path to the directory you want to monitor\n"
            + " directory_to_monitor = \'whisper-input\'\n"
            + " print(\"Background process started\")\n"
            + " if not os.path.exists(directory_to_monitor):\n"
            + "     print(f\"Error: The specified directory \'{directory_to_monitor}\' does not exist.\")\n"
            + " else:\n"
            + "     monitor_directory(directory_to_monitor)\n";


    public static File pythonDir = new File("python");
    public static File pythonPath = new File("env/bin/python");
    public static File pipPath = new File("env/bin/pip");

    public static File pythonScript = new File("python/background.py");

    public static MinecraftClient CLIENT;
    public static Process pythonProcess;



    @Override
    public void onInitializeClient() {



        long pid = ProcessHandle.current().pid();
        System.out.println("This java program's pid should be: " + pid);

//        Config.FILE_NAME = "subtitlesSVC.config";
//        File config = new File("config/" + Config.FILE_NAME);
//
//        System.out.println("b4 config check");
//        if (!config.exists()) {
//            System.out.println("after config check");
//            new File("config").mkdir();
//            try {
//                config.createNewFile();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            System.out.println("creating config");
//            ConfigBuilder builder = new ConfigBuilder();
//            builder.addItem("version", "base");
//            builder.addItem("(possible values, mod will not work if you put incorrect value):", "tiny, base, small");
//            System.out.println("saving config");
//            Config.save(builder);
//        } else {
//            whisperVersion = Config.load().getString("version");
//        }
        System.out.println("Config Initialized");
        ClientLifecycleEvents.CLIENT_STARTED.register(new ClientLifecycleEvents.ClientStarted() {
            @Override
            public void onClientStarted(MinecraftClient client) {
                CLIENT = client;
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(new ClientLifecycleEvents.ClientStopping() {
            @Override
            public void onClientStopping(MinecraftClient client) {
                if (pythonProcess != null) pythonProcess.destroyForcibly();
                clearData();

            }
        });
        pythonDir.mkdir();
        new File("whisper-input").mkdir();
        new File("whisper-output").mkdir();
        new File("whisper").mkdir();
        System.out.println("Made directories");


        try {
            System.out.println("running script");
                pythonScript.createNewFile();
                Files.write(pythonScript.toPath(), python_script_text.getBytes(StandardCharsets.UTF_8));
            boolean isWindows = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows");

            Thread scriptProc = new Thread(() -> pythonProcess = runCommand(pythonPath.getPath() + " " + pythonScript.getPath() + " " + pid + " > output.txt"));
            scriptProc.start();

            // Wait for the command process to complete (optional)
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("ran script");
    }

    public static Process runCommand(String command) {
        System.out.println("attempting command: " + command);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/zsh");
            Process process = processBuilder.start();

            OutputStream out = process.getOutputStream();
            out.write(command.getBytes());
            out.flush();
            out.close();

            int exitCode = process.waitFor();
            System.out.println("Command exited with code: " + exitCode);
            return process;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to run " + command + ", try again");
        }


    }

    public static void clearData() {
        for (File file : new File("whisper").listFiles()) {
            while (!file.delete()) ;
        }
        for (File file : new File("whisper-input").listFiles()) {
            while (!file.delete()) ;
        }
        for (File file : new File("whisper-output").listFiles()) {
            while (!file.delete()) ;
        }
    }


}
