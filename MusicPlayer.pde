void musicSetup() {
  musicFolder = new File(dataPath("") + "/music");
  music = musicFolder.listFiles();
  songsNotPlayed = new ArrayList<File>();
  songQueue = new ArrayList<File>();
}

void queueAdd() {
  songQueue = new ArrayList<File>();
  for (File f : music) {
    songsNotPlayed.add(f);
  }
  playlistLength = songsNotPlayed.size();
  while (!songsNotPlayed.isEmpty()) {
    int songNum = (int) (random(1) * songsNotPlayed.size());
    songQueue.add(songsNotPlayed.get(songNum));
    songsNotPlayed.remove(songNum);
  }
}

void playSong() {
  if(songPlaying >= playlistLength){
    queueAdd();
    songPlaying = 0;
  }
  song = minim.loadFile("data/music/" + songQueue.get(songPlaying).getName(), bands);
  println("Loaded!"); //<>//
  song.play();
  framesPlayed = 0;
}

void skipSong() {
  song.skip(song.length());
  songPlaying ++;
  playSong();
}

void rewindSong() {
  song.skip(song.length());
  playSong();
}
