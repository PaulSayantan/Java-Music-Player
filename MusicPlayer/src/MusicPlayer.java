/*
 * Copyright 2020-2022 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
A java program to play music from YouTube.

Author: Sayantan Paul
*/

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class MusicPlayer {

    public static void main(String[] args) throws IOException, InterruptedException {
        String cwd = System.getProperty("user.dir");
        String wavPath = cwd + "\\sample.wav";
        String mp3Path = cwd + "\\audio.mp3";

        //Deleting previous Audio files created during any previous Runtime of the code
        Files.deleteIfExists(Paths.get(wavPath));
        Files.deleteIfExists(Paths.get(mp3Path));

        Scanner sc = new Scanner(System.in);
        System.out.print("\nEnter Song name: ");
        String songName = sc.nextLine();
        System.out.println("Getting your music.....");

        //  Downloading the audio files as mp3 from YouTube
        Process download = Runtime.getRuntime().exec("youtube-dl -q --ignore-errors -x --audio-format mp3 -o \"audio.%(ext)s\" \"ytsearch: " + songName);
        download.waitFor();
        System.out.println("Put on your headphones now !!!");

        //  converting the audio files from mp3 to wav
        Process convert = Runtime.getRuntime().exec("ffmpeg -i audio.mp3 -acodec pcm_s16le -ar 44100 sample.wav");
        convert.waitFor();

        System.out.println("\n\t\tMUSIC PLAYER");
        System.out.println("Manually close the player when the music playback ends.");
        Music player = new Music();
        player.PlayMusic(wavPath);

    }
}


class Music {

    long PauseTime;
    Clip musicClip;
    double musicLength;
    Scanner sc = new Scanner(System.in);


    // PlayMusic function to orchestrate the whole music-playback
    void PlayMusic(String musicLocation) {
        try {
            File musicPath = new File(musicLocation);

            if (musicPath.exists()) {

                /*
                    creating AudioStream from the downloaded audio and saving it into a Clip Object
                    Here, audio -> AudioStream
                          musicClip -> Clip object
                */
                AudioInputStream audio = AudioSystem.getAudioInputStream(musicPath);
                musicClip = AudioSystem.getClip();
                //  opening the AudioStream inside Clip Object
                musicClip.open(audio);
                //Starting the Clip Object will start playing the music
                musicClip.start();

                musicLength = musicClip.getMicrosecondLength() - 10000000;

                while (musicClip.isOpen()) {
                    //  Player Menu
                    System.out.println("\nPress : \n\tP to pause\tR to Resume\tH to Restart\tS to skip to certain time\n\t\tE to exit\n");
                    char ch = sc.next().charAt(0);

                    switch (ch) {
                        case 'P':
                            Pause(musicClip);
                            break;
                        case 'R':
                            Resume(PauseTime, musicClip);
                            break;
                        case 'H':
                            musicClip.stop();
                            PauseTime = 0;
                            System.out.println("Music Stopped. Restarting.....");
                            Resume(PauseTime, musicClip);
                            break;
                        case 'E':
                            musicClip.close();
                            sc.close();
                            break;
                        case 'S':
                            skip(musicClip);
                            break;
                        default:
                            if (musicClip.isRunning()) {
                                return;
                            } else {
                                System.exit(0);
                            }
                            break;
                    }
                }

            } else {
                System.out.println("Can't find file");
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    //  Skip function to skip within music, according to user-defined skip-time
    private void skip(Clip musicClip) {
        System.out.printf(" Total Music Duration: %.3f minutes.\n", musicLength / 60000000);
        System.out.println("Enter skip time in format mm:ss :-");
        String time = sc.next();
        long converter = microsecondConverter(time);

        if (converter > 0 && converter < musicLength) {
            PauseTime = converter;
            musicClip.stop();
            Resume(PauseTime, musicClip);
        } else {
            System.out.println("Invalid time-skip not allowed");
        }

    }


    //Converting User-defined skip-time into microseconds.
    private long microsecondConverter(String time) {
        int minutes = Integer.parseInt(time.split(":")[0]);
        int seconds = Integer.parseInt(time.split(":")[1]);

        return (minutes * 60000000) + (seconds * 1000000);

    }


    //  Resume function to Resume the music playback after it's paused
    private void Resume(long time, Clip musicClip) {

        if (musicClip.isActive()) {
            System.out.println("Can't Resume a playing music.");
            return;
        }

        musicClip.setMicrosecondPosition(time);
        musicClip.start();
        System.out.println("Playback Resumed");
    }


    //  Pause function to pause the music in-between it's playback
    private void Pause(Clip musicClip) {

        if (!musicClip.isActive()) {
            System.out.println("Can't stop a paused music.");
            return;
        }
        PauseTime = musicClip.getMicrosecondPosition();
        musicClip.stop();
        System.out.println("Playback Paused");
    }
}

