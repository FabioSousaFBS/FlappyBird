package br.com.cursoandroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

	private SpriteBatch batch; //classe utilizada para criar animações
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Random numeroRandomico;
	private BitmapFont fonte;
	private BitmapFont mensagem;
	private Circle passaroCirculo;
	private Rectangle retanguloCanoTopo;
	private Rectangle retanguloCanoBaixo;
	//private ShapeRenderer shape;

	//ATRIBUTOS DE CONFIGURAÇÃO
	private float larguraDispositivo;
	private float alturaDispositivo;
	private int estadoJogo = 0; //0-> jogo não iniciado; 1 -> jogo iniciado; 2 -> game over
	private int pontuacao = 0;

	private float variacao = 0;
	private float velocidadeQueda = 0;
	private float posicaoInicialVertical;
	private float posicaoMovimentoCanoHorizontal;
	private float espacoEntreCanos;
	private float deltaTime;
	private float alturaEntreCanosRandom;
	private boolean marcouPonto = false;

	//CÂMERA
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 768;
	private final float VIRTUAL_HEIGHT = 1024;


	@Override
	public void create () {

		batch = new SpriteBatch();
		numeroRandomico = new Random();
		passaroCirculo = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoTopo = new Rectangle();
		//shape = new ShapeRenderer();

		fonte = new BitmapFont();
		fonte.setColor(Color.WHITE);
		fonte.getData().setScale(6);

		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo.png");
		canoTopo = new Texture("cano_topo.png");
		gameOver = new Texture("game_over.png");

		mensagem = new BitmapFont();
		mensagem.setColor(Color.WHITE);
		mensagem.getData().setScale(3);

		//Configurações da câmera
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);



		//larguraDispositivo = Gdx.graphics.getWidth();
		//alturaDispositivo = Gdx.graphics.getHeight();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;



		posicaoInicialVertical = alturaDispositivo / 2;
		posicaoMovimentoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 300;

	}

	@Override
	public void render () {

		camera.update();

		//LIMPAR FRAMES ANTERIORES
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		deltaTime = Gdx.graphics.getDeltaTime();
		variacao += deltaTime * 10;

		if (variacao > 2) variacao = 0;

		if(estadoJogo == 0 ){
			if(Gdx.input.justTouched()){
				estadoJogo = 1;
			}
		}else {

			velocidadeQueda++;
			if (posicaoInicialVertical > 0 || velocidadeQueda < 0) {
				posicaoInicialVertical = posicaoInicialVertical - velocidadeQueda;
			}

			if(estadoJogo == 1){
				posicaoMovimentoCanoHorizontal -= deltaTime * 200;

				if (Gdx.input.justTouched()) {
					velocidadeQueda = -15;
				}

				//VERIFICA SE O CANO SAIU INTEIRAMENTE DA TELA
				if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth()) {
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
					alturaEntreCanosRandom = numeroRandomico.nextInt(400) - 200;
					marcouPonto = false;
				}

				//VERIFICA A PONTUAÇÃO
				if(posicaoMovimentoCanoHorizontal < 120){
					if( !marcouPonto ){
						pontuacao++;
						marcouPonto = true;
					}
				}
			}else{ //TELA DE GAME OVER
				if(Gdx.input.justTouched()){
					estadoJogo = 0;
					pontuacao = 0;
					velocidadeQueda = 0;
					posicaoInicialVertical = alturaDispositivo / 2;
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
				}
			}

		}

		//CONFIGURAR DADOS DE PROJEÇÃO DA CÂMERA
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosRandom);
		batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRandom);
		batch.draw(passaros[(int) variacao], 120, posicaoInicialVertical);
		fonte.draw(batch, String.valueOf(pontuacao), larguraDispositivo / 2, alturaDispositivo - 50);

		if(estadoJogo ==2 ){
			batch.draw(gameOver, larguraDispositivo	/2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
			mensagem.draw(batch, "Toque para reiniciar.", larguraDispositivo / 2 - 200, alturaDispositivo / 2 - gameOver.getHeight()/2);
		}

		batch.end();

		passaroCirculo.set(120 + passaros[0].getWidth() / 2, posicaoInicialVertical + passaros[0].getHeight() /2 , passaros[0].getWidth() / 2);

		retanguloCanoBaixo.set(posicaoMovimentoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRandom,
				canoBaixo.getWidth(),
				canoBaixo.getHeight());

		retanguloCanoTopo.set(posicaoMovimentoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + alturaEntreCanosRandom,
				canoTopo.getWidth(),
				canoTopo.getHeight());


		//DESENHAR FORMULAS
		/*shape.begin(ShapeRenderer.ShapeType.Filled);

		shape.circle(passaroCirculo.x, passaroCirculo.y, passaroCirculo.radius);
		shape.rect(retanguloCanoBaixo.x, retanguloCanoBaixo.y, retanguloCanoBaixo.width, retanguloCanoBaixo.height);
		shape.rect(retanguloCanoTopo.x, retanguloCanoTopo.y, retanguloCanoTopo.width, retanguloCanoTopo.height);
		shape.setColor(Color.RED);

		shape.end();
		*/

		//TESTANDO COLISÃO
		if(Intersector.overlaps(passaroCirculo, retanguloCanoBaixo) || Intersector.overlaps(passaroCirculo, retanguloCanoTopo)
				|| posicaoInicialVertical <= 0 || posicaoInicialVertical >= alturaDispositivo){
			estadoJogo = 2;
		}

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
}
