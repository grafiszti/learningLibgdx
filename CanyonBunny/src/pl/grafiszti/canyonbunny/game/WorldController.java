package pl.grafiszti.canyonbunny.game;

import pl.grafiszti.canyonbunny.util.CameraHelper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class WorldController extends InputAdapter {
	private static final String TAG = WorldController.class.getName();

	public Sprite[] testSprites;
	public int selectedSprite;
	
	public CameraHelper cameraHelper;
	
	public WorldController() {
		init();
	}

	private void init() {
		Gdx.input.setInputProcessor(this);
		cameraHelper = new CameraHelper();
		initTestObjects();
	}
	
	private void initTestObjects(){
		//create new array for 5 sprites
		testSprites = new Sprite[5];
		
		//create a list of texture regions
		Array<TextureRegion> regions = new Array<TextureRegion>();
		
		regions.add(Assets.instance.bunny.head);
		regions.add(Assets.instance.feather.feather);
		regions.add(Assets.instance.goldCoin.goldCoin);
		
		//create new sprites using a random texture region
		for(int i = 0; i < testSprites.length; i++){
			Sprite spr = new Sprite(regions.random());
			
			//define sprite size to be 1m x 1m in game world
			spr.setSize(1, 1);
			
			//set origin to sprite center
			spr.setOrigin(spr.getWidth() / 2.0f, spr.getHeight() / 2.0f);
			
			//calculate random position of sprite
			spr.setPosition(MathUtils.random(-2.0f, 2.0f), 
							MathUtils.random(-2.0f, 2.0f));
			
			//put new sprite into array
			testSprites[i] = spr;
			
			//set first sprite as selected one
			selectedSprite = 0;
		}
	}
	
	@Override
	public boolean keyUp(int keycode){
		//reset game world
		if(keycode == Keys.R){
			init();
			Gdx.app.debug(TAG, "Game world resetted");
		}
		
		//select next sprite
		else if(keycode == Keys.SPACE){
			selectedSprite = (selectedSprite + 1) % testSprites.length;
			
			//update camera target to follow currently selected sprite
			if(cameraHelper.hasTarget()){
				cameraHelper.setTarget(testSprites[selectedSprite]);
			}
			
			Gdx.app.debug(TAG, "Sprite #" + selectedSprite + " selected");
		}
		//toggle camera follow
		else if(keycode == Keys.ENTER){
			cameraHelper.setTarget(cameraHelper.hasTarget() ? null : testSprites[selectedSprite]);
		}
		
		Gdx.app.debug(TAG, "camera follow enabled:" + cameraHelper.hasTarget());
		return false;
	}

	private Pixmap createProceduralPixmap(int width, int height) {
		Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
		
		//fill square with red color at 50% opacity
		pixmap.setColor(1, 0, 0, 0.5f);
		pixmap.fill();
		
		//draw a yellow-colored X shape on square
		pixmap.setColor(1, 1, 0, 1);
		pixmap.drawLine(0, 0, width, height);
		pixmap.drawLine(width, 0, 0, height);
		
		//draw a cyan-colored border around square
		pixmap.setColor(0, 1, 1, 1);
		pixmap.drawRectangle(0, 0, width, height);
		
		return pixmap;
	}
	
	private void updateTestObjects(float deltaTime){
		//get current rotation from selected sprite 
		float rotation = testSprites[selectedSprite].getRotation();
		
		//rotate sprite by random degrees by second
		rotation += MathUtils.random(40.0f, 100.0f) * deltaTime;
		//wrap around at 360 degrees
		
		rotation %= 360;
		
		//set new rotation value to selected sprite
		testSprites[selectedSprite].setRotation(rotation);
	}

	public void update(float deltaTime) {
		handleDebugInput(deltaTime);
		updateTestObjects(deltaTime);
		cameraHelper.update(deltaTime);
	}
	
	private void handleDebugInput(float deltaTime){
		if(Gdx.app.getType() != ApplicationType.Desktop) {
			return;
		}
		
		//Selected Sprite controls
		float sprMoveSpeed = 5 * deltaTime;
		
		if(Gdx.input.isKeyPressed(Keys.A)){
			moveSelectedSprite(-sprMoveSpeed, 0);
		}
		if(Gdx.input.isKeyPressed(Keys.D)){
			moveSelectedSprite(sprMoveSpeed, 0);
		}
		if(Gdx.input.isKeyPressed(Keys.W)){
			moveSelectedSprite(0, sprMoveSpeed);
		}
		if(Gdx.input.isKeyPressed(Keys.S)){
			moveSelectedSprite(0, -sprMoveSpeed);
		}
		
		//camera controls (move)
		float camMoveSpeed = 5 * deltaTime;
		float camMoveSpeedAccelerationFactor = 5;
		if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)){
			camMoveSpeed *= camMoveSpeedAccelerationFactor;
		}
		if(Gdx.input.isKeyPressed(Keys.LEFT)){
			moveCamera(-camMoveSpeed, 0);
		}
		if(Gdx.input.isKeyPressed(Keys.RIGHT)){
			moveCamera(camMoveSpeed, 0);
		}
		if(Gdx.input.isKeyPressed(Keys.UP)){
			moveCamera(0, camMoveSpeed);
		}
		if(Gdx.input.isKeyPressed(Keys.DOWN)){
			moveCamera(0, -camMoveSpeed);
		}
		if(Gdx.input.isKeyPressed(Keys.BACKSPACE)){
			cameraHelper.setPosition(0, 0);
		}
		
		//camera controls (zoom)
		float camZoomSpeed = 1 * deltaTime;
		float camZoomSpeedAccelerationFactor = 5;
		if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)){
			camZoomSpeed *= camZoomSpeedAccelerationFactor;
		}
		if(Gdx.input.isKeyPressed(Keys.COMMA)){
			cameraHelper.addZoom(camZoomSpeed);
		}
		if(Gdx.input.isKeyPressed(Keys.PERIOD)){
			cameraHelper.addZoom(-camZoomSpeed);
		}
		if(Gdx.input.isKeyPressed(Keys.SLASH)){
			cameraHelper.setZoom(1);
		}
		
	}
	
	private void moveCamera(float x, float y){
		x += cameraHelper.getPosition().x;
		y += cameraHelper.getPosition().y;
		cameraHelper.setPosition(x, y);
	}
	
	private void moveSelectedSprite(float x, float y){
		testSprites[selectedSprite].translate(x, y);
	}
}
