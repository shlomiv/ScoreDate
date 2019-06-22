# ScoreDate

This repo is a fork of https://sourceforge.net/projects/scoredate/. 

# Changes
1. Added [Leiningen](https://github.com/technomancy/leiningen) as a build system.
   1. *To build* - install lein and run `lein uberjar`. 
   2. *To run* - double click on the jar file in a graphical setting, or run `java -jar target/score-date-0.1.0-SNAPSHOT-standalone.jar`.
                 If you put the resulting uberjar in the root folder you will also get access to the exercises.

2. Fixed a bug where once a wrong note is played everything is considered "wrong"
3. Deselecting "Keyboard sound" from the Midi Options menu makes the piano not play (either from the program or attached by midi).
   This helps when practicing with rythem, as the delay does not confuse playing.

# Text from the original repo

ScoreDate is your date with the music !
It is an open source software written in Java that helps musicians to learn music reading. It also helps you with ear training.
It is suitable for any skill, from beginners to professional users. From slow training to first sight reading.

Features:
* Notes in line exercise
* Rhythms exercise
* Score reading exercise
* Ear training exercise with 4 levels of difficulty
* Exercises - Creation, edit, save and playback
* Statistics with monthly and daily view
* Supports of 4 clefs: Violin, Bass, Alto, Tenor, with a maximum of 2 at the same time
* Supports of notes: Whole, half, dotted half, quarter, dotted quarter, eighth, triplets, pauses
* Selection of the notes range for each clef. Maximum of 4 additional lines above and below the staff
* Virtual piano, to exercise without having external devices
* Chords, intervals and accidentals exercise
* Learning mode, that shows the name of the note or the chord displayed on the staff
* Realtime playback supporting ASIO, WDMKS, DirectSound, Jack, ALSA, OSS
* Translated in 15 languages
