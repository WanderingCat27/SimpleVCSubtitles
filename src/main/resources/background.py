import os
import atexit
from whisper_jax import FlaxWhisperPipline
# Instantiate pipeline
pipeline = FlaxWhisperPipline("openai/whisper-base")

def create_directories():
    input_directory = 'whisper/input'
    output_directory = 'whisper/output'

    # Create 'whisper/input' directory if not exists
    if not os.path.exists(input_directory):
        os.makedirs(input_directory)

    # Create 'whisper/output' directory if not exists
    if not os.path.exists(output_directory):
        os.makedirs(output_directory)

def transcribe_and_save(file_path, output_directory='whisper/output/'):
    try:
        # JIT compile the forward call - slow, but we only do once
        text = pipeline(os.path.join('whisper/input/', file_path))['text']

        # Used cached function thereafter - super fast!!
        print("Transcription:", text)

        # Save the transcription to a file
        output_file_path = os.path.join(output_directory, 'transcription.txt')
        with open(output_file_path, 'a') as output_file:
            output_file.write(text + '\n')

        print("Transcription saved to:", output_file_path)

    except Exception as e:
        print("Error:", str(e))

def delete_contents(directory):
    # Delete the contents of the specified directory
    for file_name in os.listdir(directory):
        file_path = os.path.join(directory, file_name)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            elif os.path.isdir(file_path):
                os.rmdir(file_path)
        except Exception as e:
            print(f"Error deleting {file_path}: {e}")

def cleanup():
    # Delete the contents of 'whisper/input' and 'whisper/output' on script exit
    delete_contents('whisper/input')
    delete_contents('whisper/output')

def main():
    create_directories()
    atexit.register(cleanup)

    while True:
        # Get input file from the user
        input_file = input("Enter the path to an audio file (or 'exit' to quit): ")

        # Check if the user wants to exit
        if input_file.lower() == 'exit':
            break



        # Transcribe and save the file
        transcribe_and_save(input_file)
    print("startin")
    main()
