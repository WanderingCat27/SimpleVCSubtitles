#!/bin/bash

# Check if <dir> is provided as a command line argument
if [ $# -eq 0 ]; then
    echo "Usage: $0 <dir>"
    exit 1
fi

# Install Homebrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install necessary packages
brew install python
brew install git
python -m pip install --user virtualenv
python -m pip install --upgrade pip
python -m venv "$1/env"
source "$1/env/bin/activate"
pip install git+https://github.com/openai/whisper.git
pip install setuptools-rust

cd $1
git clone https://git.ffmpeg.org/ffmpeg.git ffmpeg
cd ffmpeg
./configure
make

