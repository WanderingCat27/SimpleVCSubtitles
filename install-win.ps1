# Check if <dir> is provided as a command line argument
if ($args.Length -eq 0) {
    Write-Host "Usage: $((Get-Item $MyInvocation.MyCommand.Path).Name) <dir>"
    exit 1
}

# Install Chocolatey
@"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "[System.Net.ServicePointManager]::SecurityProtocol = 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"

# Install necessary packages using Chocolatey
choco install -y python
choco install -y git
choco install -y ffmpeg
choco install -y git
python -m pip install --upgrade pip
python -m venv "$($args[0])\env"
$($args[0])\env\Scripts\Activate.ps1
pip install git+https://github.com/openai/whisper.git
pip install setuptools-rust
cd $1
git clone https://git.ffmpeg.org/ffmpeg.git ffmpeg
cd ffmpeg
.\configure
make