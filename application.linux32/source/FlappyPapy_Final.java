import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class FlappyPapy_Final extends PApplet {



// Changer ici les valeurs par les dimensions en pixel du moniteur :
int largeur_ecran = 1920;
int hauteur_ecran = 1080;

// The serial port:
Serial myPort;

boolean style = true;

int ventilo_value = 10;
int switch_timer = 0;
int score_counter = 0;

float[] scores = { 0, 0, 0 };

Camera c;
Player p;
PipeSet[] ps;

// Mettre ici aussi la largeur de l'écran :
static final int NUMPIPES = 1600;

int numPipes;
int vitesse=2; // vitesse d'avancement
static final int GAMEOVER = 1;
static final int GAME = 0;
int gameState;
int inByte = 0;
boolean pouet;
int time = 10000;

boolean is_communicating = false;

boolean direction = false;
int timer = 0;
int timer_max = 10;

int[] bird_life = { -5, 0  };
int previous_y = 0;
int largeur_piaf = 30;
boolean score = true;

boolean is_value = false;

public void setup() {
  
  
  // List all the available serial ports:
  printArray(Serial.list());

  // Open the port you are using at the rate you want:
  
  myPort = new Serial(this, "/dev/ttyUSB0", 9600);
  //myPort = new Serial(this, Serial.list()[32], 9600);
  
  //size(1200, 640);
  initGame();
}

public void draw() {
        
  inByte = myPort.read();
  println(inByte);
  
  if ( is_value == false ) {
    if ( inByte != - 1 && inByte != 1 ) {
      is_value = true;
      time = 0; } }
  else { time += 1;
    if ( inByte == - 1 || inByte == 1 ) {
      is_value = false; } } 
  
  switch(gameState) {
  case 1:
    background(0);
    textSize(130);
    textAlign(CENTER);
    fill(255);
    textSize(30);
    
    if ( score ) {
    if ( (p.diametre*p.pos.x)/10000000 > scores[0] ) {
      scores[2] = scores[1];
      scores[1] = scores[0];
      scores[0] = p.diametre*p.pos.x/10000000; }
      
    else if ( (p.diametre*p.pos.x)/10000000 > scores[1] ) {
      scores[2] = scores[1];
      scores[1] = p.diametre*p.pos.x/10000000; }
      
    else if ( (p.diametre*p.pos.x)/10000000 > scores[2] ) {
      scores[2] = p.diametre*p.pos.x/10000000; }
      
    score = false; }
      
    textSize(45);
    text("1st : " + scores[0], 250, 110 );
    textSize(35);
    text("2nd : " + scores[1], 200, 160 );
    textSize(25);
    text("3rd : " + scores[2], 150, 210 );
    
    pouet = false;
    if ( score_counter < 300 ) { score_counter += 1; }
    else { 
      score_counter = 0;
      score = true;
      pouet = true; }
    
    if ( (p.diametre*p.pos.x)/10000000 > 1.4f ) {
          textSize(45);
          text("Score :  " + (p.diametre*p.pos.x)/10000000 + " BTC", width/2-textWidth("score")/2, height/2+150);
          text("'POPOPOP !'", width/2-textWidth("POPOPOP !")/2, height/2-32);
          textSize(20);
          text("Bravo ! En tant que nouveau riche,\nnous vous offrons ce moment de détente.", width/2, height/2+200);
          style = false; }

    else { style = true;
          textSize(55);
          text("'POP !'", width/2-textWidth("POP !")/2, height/2-32);
          text("Score :  " + (p.diametre*p.pos.x)/10000000 + " BTC", width/2-textWidth("score")/2, height/2+150); }
    
    if (keyPressed || pouet) {
       if (key == 'N'|| key=='n' || pouet ){
          initGame();
          gameState = 0; } }
    break;
    
  case 0: 
      if (keyPressed) {
       if (key == 'S'|| key=='s'){
          if ( switch_timer == 0 ) {
            switch_timer = 20;
          if ( style ) { style = false; }
          else { style = true; } } } }
    
    if ( switch_timer > 0 ) { switch_timer -= 1; }
    if ( style ) { drawBG(); }
    else { background( 180, 210, 250 ); }
    fill(0, 255, 0);
    translate(-c.pos.x, -c.pos.y);
    if ( style ) { p.draw(c); }
    drawPipes();
    c.draw();
    if ( style == false ) { p.draw(c); }
    translate(c.pos.x, c.pos.y);
    //drawFG();
    drawGUI();
    if (p.pos.y  > height - 120)  gameState = GAMEOVER;
    break;
  
} }


public void initGame() {
  gameState = 0;
  ps = new PipeSet[NUMPIPES];
  c = new Camera();
  p = new Player();
  makePipes();
  noCursor();
}

public void drawBG() { // dessin du fond
  background(0, 0, 0);
  
  // quadrillage du fond
   stroke(255);
  for (int i=10;i<width;i+=50){ // verticales
   line(i,0,i,width);
    }
  for (int i=10;i<height;i+=50){ // horizontales
   line(0,i,height*2,i);

    fill(255);//texte de deco
    text(p.pos.x*i/p.diametre,10,i);
    text(p.pos.x*i/p.pos.y,500,i); 
    text(p.pos.y*i/p.pos.x,1000,i);
  }
}

public void drawFG() { // aucune idée de a quoi ça sert ?
  fill(0,0, 148);

  fill(221, 216, 148);
  noStroke();
  rect(-20, height-100, width+50, 300);
  stroke(0);
  fill(153, 230, 90);
  rect(-20, height-110, width+50, 10);
}

public void makePipes() {
  float x = 300; // distance de depart du joueur / 1er tuyaux
  float y = random(100, 100); // hauteur de depart du joueur
  float h = 300;// random(75, 125); // taille du passage au debut, permet le niveau de difficultée 100 - 300 - 500

  for (int i = 0; i < NUMPIPES; i++) {
    ps[i] = new PipeSet(x, y, h);
    y = ps[i].pos.y + random(-50,50);  
    if(y<100) y = ps[i].pos.y + random(0,50);
    if(y>400) y = ps[i].pos.y + random(-50,0);
    // y = random(100, 400);
    if(h>0)h--;// = 200;// random(100, 200); // random(50,75); // resserrage du tuyau !
    x += ps[i].bWidth;
  }
}

public void drawPipes() {
  for (int i = 0; i < NUMPIPES; i++) { 
    if (PVector.dist(p.pos, ps[i].pos) < NUMPIPES)
      ps[i].draw();
    if (p.pos.x > ps[i].pos.x && p.pos.x < ps[i].pos.x + ps[i].bWidth && p.pos.y > ps[i].pos.y && p.pos.y < ps[i].pos.y + ps[i].bHeight)
      // test de collisision, actuellement jus le centre de la boule, a voir en rajoutant p.diametre
      
    {
      if (!ps[i].scored)
        p.score++; 
      ps[i].scored = true;
    }
    if (p.pos.x > ps[i].pos.x && p.pos.x < ps[i].pos.x + ps[i].bWidth && (p.pos.y < ps[i].pos.y || p.pos.y > ps[i].pos.y + ps[i].bHeight))
      gameState = GAMEOVER;
  }
}

public void drawGUI() {
  textSize(18);
  fill(0);
  //text("Distance: " + (int)(p.pos.x/100), 0, height - 40);
  textAlign(RIGHT);
  if ( style ) {
  text("Score: " , 800, height - 20);
  textAlign(LEFT);
  text(p.diametre/p.pos.x +"BTC" , 800, height - 20); //p.score = p.diametre/p.pos.x a utiliser pour un hight score!
} }

class Camera {
  PVector pos; //Camera's position 
  //The Camera should sit in the top left of the window

  Camera() {
    pos = new PVector(0, 0);
    //You should play with the program and code to see how the staring position can be changed
  }

  public void draw() {
    pos.x+=2;
  }
}

class PipeSet {
  PVector pos;
  float bHeight, bWidth,c1Pipe,c2Pipe,c3Pipe;
  boolean scored;
  int graphisme = PApplet.parseInt( random( 256 ) );
  int color_random = PApplet.parseInt( random( 150 ) );
  int base_color, shadow_color;

PipeSet(float x, float y, float h) {
    pos = new PVector(x, y); 
    bHeight = h;
    bWidth = 50; // largeur , des tuyaux
    
    c1Pipe=51+random(-20,20); // test de couleur aleatoire pour les tuyaux
    c2Pipe= 133+random(-20,20);
    c3Pipe= 255;
    
    base_color = color(255 - color_random, 255 - color_random, 204 - color_random );
    shadow_color = color(205 - color_random, 205 - color_random, 154 - color_random );
    
    scored = false;
  }

  public void draw() {
    drawPipes();
  }

  public void drawPipes() { // les dessins des obstacle ( les barre sont de la decos, les ligne sont les limites de la cave)
    
    if ( style ) {
    // barres
    noStroke();
    fill(c1Pipe, c2Pipe, c3Pipe,200); // opacitée
    rect(pos.x, 0, bWidth, pos.y-random(-20,80));// rectangle du haut
    //fill(204, 204, 255);
    rect(pos.x, pos.y+bHeight+random(-30,80), bWidth, height); // rectangle du bas + test random , effet cool !
    //    strokeWeight(10);
    strokeCap(ROUND);
    stroke(0,255,0);
    fill(0,255,0);
    strokeWeight(5);
    line(pos.x, pos.y, pos.x+bWidth, pos.y); 
    text(pos.y,pos.x,pos.y-3);
    
    stroke(255,0,0);
    fill(255,0,0);
    line(pos.x,pos.y+bHeight,pos.x+bWidth,pos.y+bHeight);
     text(pos.y+bHeight,pos.x,pos.y+bHeight+16);
   
    noStroke();
    strokeWeight(1); }
    
    PVector top_collision = new PVector( pos.x,pos.y-largeur_piaf/2 );
    PVector bottom_collision = new PVector( pos.x,pos.y+bHeight+largeur_piaf/2 );
    
    if ( style == false ) {
      draw_a_sublime_house( bottom_collision, 50, base_color, shadow_color, graphisme );   
      draw_wires( top_collision, 50, previous_y );
      previous_y = PApplet.parseInt(pos.y);}
  }
}

class Player {
  PVector pos;
  float score;
  int diametre;
  
  Player() {
    score = 0;
    diametre=30;//diametre de l'inflation
    pos = new PVector(100, 100);
  }

  public void inflate(int i){ // methode pour gonfler le ballon
    diametre+=i;
  }


  public void draw(Camera c) { // le controle du mouvement
    drawBird();
    
    if ( ( inByte != - 1 && inByte != 1 ) || (keyPressed) ) {
        pos.y-=2.5f; 
        p.inflate(1);
      }
    pos.y += 2; // vitesse de descente
    pos.x += vitesse; // vitesse de scroling joueur
  }

  public void drawBird() { // l'avatar !
    if ( style ) {
      fill(214, 189, 47);
      noStroke();
      ellipse(pos.x, pos.y, p.diametre, p.diametre); // juste un rond !
       stroke(255);
      strokeWeight(1);
      line(pos.x+p.diametre/2.61f,pos.y+p.diametre/2.61f,pos.x,pos.y);
      line(pos.x+p.diametre/2.61f,pos.y+p.diametre/2.61f,pos.x+10+p.diametre/2.61f,pos.y+p.diametre/2.61f);
      text(p.diametre,pos.x+10+p.diametre/2.61f,pos.y+5+p.diametre/2.61f); }
    
    if ( style == false ) {
      update_bird_life( bird_life );
      draw_the_amazing_bird( pos, bird_life ); }
  }
}

// Addition des fonctiions par Simon :
public void draw_the_amazing_bird( PVector pos, int[] bird_life ) {
  
  //println( bird_life[0] );
  
  float pos_x = pos.x;
  float pos_y = pos.y;
  
  noStroke();
  //stroke( 255 );
  
  fill(0, 204, 0);
  triangle( pos_x + 25, pos_y - 10, pos_x + 13, pos_y - 20, pos_x + 15, pos_y - 10); // Attention madame, c'est un punk !
  fill(255, 204, 0);
  triangle( pos_x + 20, pos_y - 10, pos_x + 3, pos_y - 23, pos_x + 10, pos_y - 10);
  fill(102, 153, 255);
  triangle( pos_x + 5, pos_y - 10, pos_x - 7, pos_y - 27, pos_x + 15, pos_y - 10);
  
  fill(255, 104, 0);
  triangle( pos_x + 28, pos_y, pos_x + 40, pos_y + 5, pos_x + 15, pos_y + 10); // Le badubek.
  fill(255, 204, 0);
  triangle( pos_x + 25, pos_y - 5, pos_x + 45, pos_y, pos_x + 25, pos_y + 5); // Le odubek.
 
  fill( 55, 0, 205 );
  ellipse( pos_x - 50, pos_y - 15, 40, 8 ); // Plume première.
  triangle( pos_x - 50, pos_y - 11, pos_x - 50, pos_y - 19, pos_x - 105, pos_y - 15 );
  fill( 155, 0, 225 );
  ellipse( pos_x - 40, pos_y - 10, 35, 8 ); // Plume seconde.
  triangle( pos_x - 40, pos_y - 6, pos_x - 50, pos_y - 14, pos_x - 85, pos_y - 10 );
  fill( 255, 0, 255 );
  ellipse( pos_x - 30, pos_y - 5, 30, 8 ); // Plume dernière.
  triangle( pos_x - 30, pos_y - 1, pos_x - 30, pos_y - 9, pos_x - 65, pos_y - 5 );
  
  fill(204, 0, 204);
  ellipse( pos_x, pos_y, 60, 30 ); // Le corps !
  fill(234, 0, 234);
  ellipse( pos_x + 5, pos_y - 2, 55, 26 ); // Et puis son fabuleux dégradé !
  
  noStroke();
  fill( 255 );
  ellipse( pos_x + 25, pos_y - 5, 7, 7 );
  fill( 0 );
  ellipse( pos_x + 29, pos_y - 5, 4, 4 );
  
  fill(155, 0, 55);
  ellipse( pos_x - 5, pos_y - bird_life[0], 35, 8 ); // Plume première.
  fill( 205, 0, 25 );
  ellipse( pos_x - 10 - 2 * bird_life[0], pos_y - 2 * bird_life[0], 35, 8 ); // Plume seconde.
  triangle( pos_x - 10 - 2 * bird_life[0], pos_y - 2 * bird_life[0] - 4, pos_x - 10 - 4 * bird_life[0], pos_y - 2 * bird_life[0] + 4, pos_x - 40 - 4 * bird_life[0], pos_y - 2 * bird_life[0] );
  fill( 155, 0, 0 );
  ellipse( pos_x - 20 - 4 * bird_life[0], pos_y - 3 * bird_life[0], 35, 8 ); // Plume dernière.
  triangle( pos_x - 20 - 4 * bird_life[0], pos_y - 3 * bird_life[0] - 4, pos_x - 20 - 4 * bird_life[0], pos_y - 3 * bird_life[0] + 4, pos_x - 60 - 4 * bird_life[0], pos_y - 3 * bird_life[0] );
}

public int[] update_bird_life( int[] bird_life ) {
  if ( bird_life[1] == 0 ) {
    if ( bird_life[0] < 5 ) { bird_life[0]++; }
    else { bird_life[1] = 1; } }
  else {
    if ( bird_life[0] > -5 ) { bird_life[0]--; }
    else { bird_life[1] = 0; } }
  return bird_life; }
  
public void draw_a_sublime_house( PVector pos, int house_width, int base_color, int shadow_color, int graphisme ) {
  
  noStroke();
  
  fill( base_color );
  rect( pos.x + 1, pos.y, house_width - 2, hauteur_ecran - pos.y ); // La façade. C'est un rectangle. Un jour nous habiterons tous des maisons rondes. Mais non j'rigole.
  fill( shadow_color );
  triangle( pos.x + 2, pos.y + 4, pos.x + house_width - 2, pos.y + 4, pos.x + house_width - 2, hauteur_ecran );
  
  if ( graphisme % 6 == 0 ) {
    for ( int degrade_magnifique = 0; degrade_magnifique < 40; degrade_magnifique++ ) {
      if ( degrade_magnifique < 37 ) { fill( 55 + degrade_magnifique * 5, 0, 0 ); }
      else if ( degrade_magnifique < 39 ) { fill(0); }
      else { fill( shadow_color ); }
      for ( int i = 0; i * 10 < house_width; i++ ) { ellipse( pos.x + 5 + i * 10, pos.y + degrade_magnifique - 35, 10, 10 ); } } }
     
  else {
    fill( shadow_color );
    rect( pos.x, pos.y - 40, house_width, 40 );
    fill( base_color );
    rect( pos.x + 2, pos.y - 38, house_width - 4, 36 ); }
    
  if ( graphisme % 7 == 0 ) {
    stroke( 50 + ( graphisme % 7 ) * 5 );
    for ( int i = 0; i * 7 < hauteur_ecran - pos.y; i++ ) {
      line( pos.x + 1, pos.y + 7 * i, pos.x + house_width - 2, pos.y + 7 * i );
    } }
  
  stroke( 255, 0, 0 );
  //line( pos.x, pos.y, pos.x + house_width, pos.y ); // Début de la hitbox ! Décommenter si besoin.
  
  if ( graphisme % 5 == 0 ) {
    for ( int i = 0; i < 5*3; i++ ) {
      PVector window_pos = new PVector( pos.x + 5 + ( i % 3 ) * 16, pos.y + 15 + 24 * ( ( i - i % 3 ) / 3 ) );
      draw_a_window_please( window_pos ); 
    } }
  else if ( graphisme % 4 == 0 ) {
    for ( int i = 0; i < graphisme % 7; i++ ) {
      noStroke();
      fill(128, 64, 0);
      rect( pos.x + 5, pos.y + 25 + i * 25, house_width - 10, 15 );
      fill(204, 136, 0);
      triangle( pos.x + 5, pos.y + 40 + i * 25, pos.x + house_width - 5, pos.y + 25 + i * 25, pos.x + house_width - 5, pos.y + 40 + i * 25 );
      fill( 235 );
      rect( pos.x + 8, pos.y + 28 + i * 25, house_width - 16, 9 );
      fill(153, 204, 255);
      triangle( pos.x + 10, pos.y + 37 + i * 25, pos.x + house_width - 8, pos.y + 29 + i * 25, pos.x + house_width - 8, pos.y + 37 + i * 25 );
      } }
}

public void draw_a_window_please( PVector pos ) {
  
  stroke( 0 );
  fill( 255 );
  rect( pos.x, pos.y, 8, 12 );
  fill(153, 204, 255);
  triangle( pos.x + 8, pos.y, pos.x + 8, pos.y + 12, pos.x, pos.y + 12 );
  
  line( pos.x, pos.y, pos.x, pos.y + 12 );
  //line( pos.x, pos.y + 6, pos.x + 6, pos.y + 6 );
  line( pos.x + 4, pos.y, pos.x + 4, pos.y + 12 );
  line( pos.x + 8, pos.y, pos.x + 8, pos.y + 12 );
  
  stroke(204, 102, 0);
  line( pos.x - 1, pos.y - 1, pos.x + 9, pos.y - 1 );
  line( pos.x + 1, pos.y + 11, pos.x + 7, pos.y + 11 );

}

public void draw_wires( PVector pos, int wire_width, int previous_y ) {
  
  stroke(0);
  for ( int i = 0; i < 5; i++ ) { line( pos.x, pos.y - i * 3, pos.x + wire_width - 18, pos.y - 2 - i * 3 ); }
  
  noStroke();
  fill( 100 );
  rect( pos.x + wire_width - 12, pos.y - 5, 2, 8 );
  rect( pos.x + wire_width - 18, pos.y - 22, 6, 25 );
  fill( 130 );
  ellipse( pos.x + wire_width - 15, pos.y - 22, 6, 6 );

  fill(128, 64, 0);
  
  rect( pos.x + wire_width - 10, 0, 10, pos.y + 3 );
  ellipse( pos.x + wire_width - 5, pos.y + 3, 10, 5 );
  
  stroke(204, 136, 0);
  line( pos.x + wire_width - 8, 0, pos.x + wire_width - 8, pos.y - 35 );
  line( pos.x + wire_width - 6, 53, pos.x + wire_width - 6, pos.y - 350 );
  line( pos.x + wire_width - 6, pos.y - 53, pos.x + wire_width - 6, pos.y - 17 );
  line( pos.x + wire_width - 6, pos.y - 253, pos.x + wire_width - 6, pos.y - 147 );
  line( pos.x + wire_width - 3, pos.y - 222, pos.x + wire_width - 3, pos.y );
  line( pos.x + wire_width - 3, pos.y - 422, pos.x + wire_width - 3, pos.y - 375 );
  line( pos.x + wire_width - 1, 12, pos.x + wire_width - 1, 44 );
  
  if ( previous_y - 20 < pos.y ) {
    noStroke();
    fill( 80 );
    rect( pos.x, previous_y - 20, 4, pos.y - previous_y + 20 );
    ellipse( pos.x + 2, pos.y, 4, 4 );
    fill( 130 );
    ellipse( pos.x + 2, previous_y - 20, 4, 4 ); }
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#FF6699", "--hide-stop", "FlappyPapy_Final" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
