package com.bet.flappyedi;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

import javax.xml.soap.Text;

public class FlappyEdi extends ApplicationAdapter {

    //Assets
    private SpriteBatch batch;
    private Texture[] edivaldo;
    private Texture background;
    private Texture[] pipeBottom;
    private Texture[] pipeTop;
    private Texture gameover;
    private BitmapFont font;
    private BitmapFont message;
    private Circle edivaldoCircle;
    private Rectangle pipeBottomRectangle;
    private Rectangle pipeTopRectangle;
    private ShapeRenderer shapeRenderer;
    private Sound scoreSound;

    //Configs
    private float width;
    private float height;
    private int gameStatus = 0;
    private int score = 0;
    private float sprite = 0;
    private float freefall = 0;
    private float initialPosY;
    private float pipeMovX;
    private float pipeSpace;
    private float pipeSpaceRNG;
    private float deltaT;
    private Random rng;
    private Boolean scored = false;

    //Adjusts
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float vWidth = 768;
    private final float vHeight = 1024;

	@Override
	public void create () {
	    batch = new SpriteBatch();
	    shapeRenderer = new ShapeRenderer();

	    rng = new Random();

	    edivaldoCircle = new Circle();
	    pipeBottomRectangle = new Rectangle();
	    pipeTopRectangle = new Rectangle();

	    font = new BitmapFont();
	    font.setColor(Color.WHITE);
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("flappyedi.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 100;
        font = generator.generateFont(parameter);

        message = new BitmapFont();
        message.setColor(Color.WHITE);
        parameter.size = 60;
        message = generator.generateFont(parameter);
        generator.dispose();

	    edivaldo = new Texture[3];
	    edivaldo[0] = new Texture("passaro1.png");
        edivaldo[1] = new Texture("passaro2.png");
        edivaldo[2] = new Texture("passaro3.png");

        pipeBottom = new Texture[2];
        pipeBottom[0] = new Texture("cano_baixo.png");
        pipeBottom[1] = new Texture("cano_baixo_maior.png");

        pipeTop = new Texture[2];
        pipeTop[0] = new Texture("cano_topo.png");
        pipeTop[1] = new Texture("cano_topo_maior.png");

	    background = new Texture("fundo.png");

	    gameover = new Texture("game_over.png");

	    scoreSound = Gdx.audio.newSound(Gdx.files.internal("score.wav"));

	    camera = new OrthographicCamera();
	    camera.position.set(vWidth/2, vHeight/2, 0);
	    viewport = new StretchViewport(vWidth, vHeight, camera);

	    width = vWidth;
	    height = vHeight;

	    initialPosY = height/2 - edivaldo[0].getHeight()/2;
	    pipeMovX = width;
	    pipeSpace = 300;
	}

	@Override
	public void render () {
        deltaT = Gdx.graphics.getDeltaTime();
        sprite += deltaT * 2.5;

        if(sprite > 2){
            sprite = 0;
        }

	    if(gameStatus == 0){
            if(Gdx.input.justTouched()){
                gameStatus = 1;
            }
        } else {
            freefall++;
            if(initialPosY > 0 || freefall < 0){
                initialPosY = initialPosY - freefall;
            }

            if (gameStatus == 1){
                pipeMovX -= deltaT * 200;
                if(Gdx.input.justTouched()){
                    freefall = -20;
                }
                if(pipeMovX < -pipeTop[0].getWidth()){
                    pipeMovX = width;
                    pipeSpaceRNG = rng.nextInt(400) - 200;
                    scored = false;
                }
                if(pipeMovX < 120){
                    if(!scored){
                        score++;
                        scored = true;
                        scoreSound.play();
                    }
                }
            } else {
                if(Gdx.input.justTouched()){
                    gameStatus = 0;
                    score = 0;
                    freefall = 0;
                    initialPosY = height/2 - edivaldo[0].getHeight()/2;
                    pipeMovX = width;
                    scored = false;
                }
            }
        }

        batch.setProjectionMatrix(camera.combined);
	    batch.begin();
	    batch.draw(background, 0,0, width, height);
	    batch.draw(pipeTop[0], pipeMovX, height/2 + pipeSpace/2 + pipeSpaceRNG);
        batch.draw(pipeBottom[0], pipeMovX, height/2 - pipeBottom[0].getHeight() - pipeSpace/2 + pipeSpaceRNG);
        batch.draw(edivaldo[(int) sprite], 120, initialPosY);
        if(gameStatus == 2) {
            batch.draw(gameover, width/2 - gameover.getWidth()/2, height/2 - gameover.getHeight()/2);
            message.draw(batch, "Toque para reiniciar!", width/2 - gameover.getWidth()/2, height/2 - gameover.getHeight()/2 - 15);
        }
        font.draw(batch, String.valueOf(score), width/2 - font.getSpaceWidth()/2, height-font.getLineHeight());
        batch.end();

        edivaldoCircle.set(120 + edivaldo[0].getWidth()/2, initialPosY + edivaldo[0].getHeight()/2, edivaldo[0].getWidth()/2 );
        pipeBottomRectangle.set(pipeMovX, height/2 - pipeBottom[0].getHeight() - pipeSpace/2 + pipeSpaceRNG, pipeBottom[0].getWidth(), pipeBottom[0].getHeight());
        pipeTopRectangle.set(pipeMovX, height/2 + pipeSpace/2 + pipeSpaceRNG, pipeTop[0].getWidth(), pipeTop[0].getHeight());
        /*shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.circle(edivaldoCircle.x, edivaldoCircle.y, edivaldoCircle.radius);
        shapeRenderer.rect(pipeBottomRectangle.x, pipeBottomRectangle.y, pipeBottomRectangle.width, pipeBottomRectangle.height);
        shapeRenderer.rect(pipeTopRectangle.x, pipeTopRectangle.y, pipeTopRectangle.width, pipeTopRectangle.height);
        shapeRenderer.end();*/

        if(Intersector.overlaps(edivaldoCircle, pipeBottomRectangle) || Intersector.overlaps(edivaldoCircle, pipeTopRectangle) || initialPosY <= 0 || initialPosY >= height){
            gameStatus = 2;
        }
	}

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
