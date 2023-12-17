#!/bin/bash

# Check if a directory is provided as an argument
if [ $# -eq 0 ]; then
    echo "Usage: $0 <directory>"
    exit 1
fi

# Store the provided directory in a vººariable
target_directory="$1"

# Check if the provided path is a valid directory
if [ ! -d "$target_directory" ]; then
    echo "Error: '$target_directory' is not a valid directory."
    exit 1
fi

# Change into the specified directory
cd "$target_directory" || exit 1

# Function to check if a command is available
isCommandAvailable() {
    command -v "$1" >/dev/null 2>&1
}

# Function to run a command
runCommand() {
    eval "$1"
}

# Check if either python or ffmpeg is not available
if ! isCommandAvailable "python" || ! isCommandAvailable "ffmpeg"; then
    os=$(uname -s | tr '[:upper:]' '[:lower:]')

    case "$os" in
        *win*)
            echo "Windows"
            if ! isCommandAvailable "choco"; then
                echo "PLEASE INSTALL Chocolatey:"
                echo "You are on Windows."
                echo "Command:"
                echo "@\"%SystemRoot%\\System32\\WindowsPowerShell\\v1.0\\powershell.exe\" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command \"[System.Net.ServicePointManager]::SecurityProtocol = 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))\" && SET \"PATH=%PATH%;%ALLUSERSPROFILE%\\chocolatey\\bin\""
                echo "Paste that into the command prompt, follow instructions, or go to:"
                echo "https://docs.chocolatey.org/en-us/choco/setup"
                exit 1
            fi

            if ! isCommandAvailable "python"; then
                runCommand "choco install python"
            fi

            if ! isCommandAvailable "ffmpeg"; then
                runCommand "choco install ffmpeg"
            fi
            ;;
        *darwin*)
            echo "MacOS"
            if ! isCommandAvailable "brew"; then
                echo "PLEASE INSTALL Homebrew:"
                echo "You are on MacOS."
                echo "Open terminal, paste the following command:"
                echo "/bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
                echo "Press enter and follow instructions, or install easily via:"
                echo "https://github.com/Homebrew/brew/releases/download/4.1.25/Homebrew-4.1.25.pkg"
                echo "Homebrew site: https://brew.sh/"
                exit 1
            fi

            if ! isCommandAvailable "python"; then
                runCommand "brew install python"
            fi

            if ! isCommandAvailable "ffmpeg"; then
                runCommand "brew install ffmpeg"
            fi
            ;;
        *)
            echo "Unknown OS (maybe Linux)"
            echo "------ Support for other OS's limited, proceed at your own risk ----"
            echo "Install ffmpeg and python"
            exit 1
            ;;
    esac
fi

echo "Initiating environment"
pythonPath="./env/bin/python"
pipPath="./env/bin/pip"

# Check if Python virtual environment exists
if [ ! -f "$pythonPath" ]; then
    runCommand "python -m venv env"
    runCommand "$pipPath install git+https://github.com/sanchit-gandhi/whisper-jax.git"
    runCommand "$pipPath install psutil"
fi
