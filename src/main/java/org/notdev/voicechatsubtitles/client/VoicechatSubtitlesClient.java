package org.notdev.voicechatsubtitles.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import de.maxhenkel.voicechat.api.events.ClientEvent;
import de.maxhenkel.voicechat.api.events.ClientVoicechatInitializationEvent;
import net.fabricmc.api.ClientModInitializer;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class VoicechatSubtitlesClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */

    String python_script_text = "import os\n" +
            "import time\n" +
            "import fnmatch\n" +
            "import whisper\n" +
            "\n" +
            "model = whisper.load_model(\"tiny\")\n" +
            "\n" +
            "\n" +
            "\n" +
            "def monitor_directory(directory_path):\n" +
            "    # Store the existing files in the directory\n" +
            "    existing_files = set()\n" +
            "\n" +
            "    while True:\n" +
            "        # Get the list of files in the directory\n" +
            "        current_files = set(fnmatch.filter(os.listdir(directory_path), '*.mp3'))\n" +
            "\n" +
            "        # Find the newly added files\n" +
            "        new_files = current_files - existing_files\n" +
            "\n" +
            "        # Print the names of newly added .mp3 files\n" +
            "        for new_file in new_files:\n" +
            "            print(f\"New MP3 file added: {new_file}\")\n" +
            "            transcribe(\"whisper-input/\"+ new_file)\n" +
            "\n" +
            "        # Update the set of existing files\n" +
            "        existing_files = current_files\n" +
            "\n" +
            "        # Sleep for a short duration before checking again\n" +
            "        time.sleep(5)  # You can adjust the sleep duration as needed\n" +
            "\n" +
            "\n" +
            "def delete_and_write(input_file_path, output_directory, output_file_name, content_to_write):\n" +
            "    # Delete the existing file\n" +
            "    try:\n" +
            "        os.remove(input_file_path)\n" +
            "        print(f\"File at {input_file_path} deleted successfully.\")\n" +
            "    except OSError as e:\n" +
            "        print(f\"Error deleting the file at {input_file_path}: {e}\")\n" +
            "\n" +
            "    # Create the output directory if it doesn't exist\n" +
            "    if not os.path.exists(output_directory):\n" +
            "        os.makedirs(output_directory)\n" +
            "        print(f\"Output directory {output_directory} created.\")\n" +
            "\n" +
            "    # Write the content to the new file\n" +
            "    output_file_path = os.path.join(output_directory, output_file_name)\n" +
            "    try:\n" +
            "        with open(output_file_path, 'w') as output_file:\n" +
            "            output_file.write(content_to_write)\n" +
            "        print(f\"Content written to {output_file_path} successfully.\")\n" +
            "    except IOError as e:\n" +
            "        print(f\"Error writing to {output_file_path}: {e}\")\n" +
            "\n" +
            "\n" +
            "\n" +
            "def transcribe(path):\n" +
            "    print(\"starting\")\n" +
            "    path_parts = path.split(\"/\")\n" +
            "    desired_path = \"whisper-input/\" + path_parts[-1]\n" +
            "\n" +
            "    # decode the audio\n" +
            "    result = model.transcribe(desired_path, language=\"en\", fp16=False, verbose=False)\n" +
            "\n" +
            "    # print the recognized text\n" +
            "    print(result['text'])\n" +
            "\n" +
            "    # Example usage:\n" +
            "    delete_and_write(\n" +
            "        input_file_path=path,\n" +
            "        output_directory='whisper-output',\n" +
            "        output_file_name=path_parts[-1]+'.txt',\n" +
            "        content_to_write=result['text']\n" +
            "    )\n" +
            "\n" +
            "    return result\n" +
            "\n" +
            "if __name__ == \"__main__\":\n" +
            "    # Replace 'your_directory_path' with the path to the directory you want to monitor\n" +
            "    directory_to_monitor = 'whisper-input'\n" +
            "\n" +
            "    print(\"Background process started\")\n" +
            "\n" +
            "    if not os.path.exists(directory_to_monitor):\n" +
            "        print(f\"Error: The specified directory '{directory_to_monitor}' does not exist.\")\n" +
            "    else:\n" +
            "        monitor_directory(directory_to_monitor)";


    public static File pythonDir = new File("python");
    public static File pythonPath = new File("python/portable/bin/python3");
    public static File pythonScript = new File("python/background.py");

    @Override
    public void onInitializeClient() {
        pythonDir.mkdir();
        new File("whisper-input").mkdir();
        new File("whisper-output").mkdir();


        try {
            if (!pythonScript.exists()) {

                pythonScript.createNewFile();
                Files.write(pythonScript.toPath(), python_script_text.getBytes(StandardCharsets.UTF_8));
            }
            boolean isWindows = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows");

            System.out.println("Opening terminal");

            // Open a new terminal
            ProcessBuilder terminalProcessBuilder = new ProcessBuilder("gnome-terminal");
            Process terminalProcess = terminalProcessBuilder.start();
            terminalProcessBuilder.command("python3", "python/background.py").start();


            // Wait for the terminal to open (optional)
            int terminalExitCode = terminalProcess.waitFor();
            System.out.println("Terminal opened with exit code: " + terminalExitCode);

            // Command to run in the terminal
            String command = "ls -l;" + "python3 python/background.py";

            // Run the command in the opened terpminal
            ProcessBuilder commandProcessBuilder = new ProcessBuilder("bash", "-c", command);
            Process commandProcess = commandProcessBuilder.start();

            // Wait for the command process to complete (optional)
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
     }
    }


}
