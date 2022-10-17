import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 
import ddf.minim.effects.*; 
import ddf.minim.signals.*; 
import ddf.minim.spi.*; 
import ddf.minim.ugens.*; 
import processing.video.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class minimStellarEQ extends PApplet {









FFT fft;
int bands = 2048;
float barWidth = 20;
Bar[] bars = new Bar[bands];
int mem = 5;
int roof = 32;
float scale = 0.4f;
Bar amp;
float dotDepth = 10;
float shapeTheta = 0;
boolean cache = false;

Minim minim;
File musicFolder;
File[] music;
ArrayList<File> songsNotPlayed;
AudioPlayer song;
ArrayList<File> songQueue;
int songPlaying = 0;
int framesPlayed = 0;
int playlistLength;

public PShape makeDonut(float innerRadius, float outerRadius, float steps) {
  PShape s = createShape();
  s.beginShape();
  for (float a=0; a<TAU; a+=TAU/steps) {
    s.vertex(outerRadius*cos(a), outerRadius*sin(a));
  }
  s.beginContour();
  for (float a=0; a<TAU; a+=TAU/steps) {
    s.vertex(innerRadius*cos(-a), innerRadius*sin(-a));
  }
  s.endContour();
  s.noStroke();
  s.endShape(CLOSE);
  return s;
}

class Bar {
  float[] history;
  int memory;
  PShape donut;
  Bar(int m) {
    memory = m;
    history = new float[memory];
    for (int i = 0; i < history.length; i ++) {
      history[i] = 0;
    }
  }

  public void update() {
    for (int i = history.length - 1; i > 0; i --) {
      history[i] = history[i - 1];
    }
  }

  public float val() {
    float total = 0;
    for (int i = 0; i < history.length; i ++) {
      total += history[i];
    }
    return total / history.length;
  }
}

public void dots() {
  noStroke();
  for (float i = dotDepth; i >= 0; i --) {
    fill(255, 255 * (amp.val() / 2 > i / dotDepth ? min(dotDepth * (amp.val() / 2 - i / dotDepth), (dotDepth - i) / dotDepth) : 0));
    pushMatrix();
    translate(width / 2, height / 2);
    scale((dotDepth - i) / dotDepth);
    translate(- width / 2, - height / 2);
    for (float x = width / 8 / 2; x < width; x += width / 8) {
      for (float y = height / 6 / 2; y < height; y += height / 6) {
        ellipse(x, y, sqrt(width * width + height * height) * scale * 0.01f, sqrt(width * width + height * height) * scale * 0.01f);
      }
    }
    popMatrix();
  }
}

public void checkSongOver() {
  framesPlayed ++;
  //println(framesPlayed + " " + song.duration() * 60);
  if (!song.isPlaying()) {
    skipSong();
  }
}

public void mouseClicked() {
  if (mouseButton == LEFT) {
    skipSong();
  } else if (mouseButton == RIGHT) {
    rewindSong();
  }
}

public void setup() {
  
  background(255);
  minim = new Minim(this);
  barWidth = roof * width / bands;
  musicSetup();
  queueAdd();
  playSong();
  fft = new FFT(song.bufferSize(), song.sampleRate());

  for (int i = 0; i < bars.length; i ++) {
    bars[i] = new Bar(mem);
  }

  amp = new Bar(10);

  for (int i = bands / roof - 1; i >= 0; i--) {
    bars[i].donut = makeDonut(sqrt(width * width + height * height) * scale * i / 2 / (bands / roof - 1), sqrt(width * width + height * height) * scale * (i + 1) / 2 / (bands / roof - 1), 80);
  }
  
  noCursor();
}      

public void draw() {
  background(0);
  fft.forward(song.mix);
  dots();
  
  amp.history[0] = (song.mix.level()) * 3;
  for (int i = bands / roof - 1; i >= 0; i--) {
    bars[i].history[0] = fft.getBand(i);
    noStroke();
    bars[i].donut.setFill(color(255, bars[i].val() * 0.01f * 255 > 255 ? 255 : bars[i].val() * 0.01f * 255));
    pushMatrix();
    translate(width / 2, height / 2);
    rotate(shapeTheta / 360 * TAU);
    shape(bars[i].donut, 0, 0);  
    popMatrix();
    bars[i].update();
  }
  amp.update();
  checkSongOver();
}
public void musicSetup() {
  musicFolder = new File(dataPath("") + "/music");
  music = musicFolder.listFiles();
  songsNotPlayed = new ArrayList<File>();
  songQueue = new ArrayList<File>();
}

public void queueAdd() {
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

public void playSong() {
  if(songPlaying >= playlistLength){
    queueAdd();
    songPlaying = 0;
  }
  song = minim.loadFile("data/music/" + songQueue.get(songPlaying).getName(), bands);
  println("Loaded!");
  song.play();
  framesPlayed = 0;
}

public void skipSong() {
  song.skip(song.length());
  songPlaying ++;
  playSong();
}

public void rewindSong() {
  song.skip(song.length());
  playSong();
}
  public void settings() {  fullScreen(P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "minimStellarEQ" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
