package org.andlabs.andengine.extension.physicsloader;

import java.io.IOException;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class AndenginePhysicsLoaderSimpleExample extends SimpleBaseGameActivity {

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private static final FixtureDef FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 0.5f, 0.5f);

	private BitmapTextureAtlas mBitmapTextureAtlas;

	private ITextureRegion mBigAssetTextureRegion;

	private Scene mScene;

	private PhysicsWorld mPhysicsWorld;
	private Sprite mBigAsset;
	private TiledTextureRegion mCircleFaceTextureRegion;

	private ZoomCamera mCamera;

	@Override
	public EngineOptions onCreateEngineOptions() {
		mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	protected void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
				this.getTextureManager(), 1024, 512, TextureOptions.BILINEAR);
		this.mBigAssetTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"simple_asset.png", 0, 0);
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"face_circle_tiled.png", 0, 256, 2, 1); // 64x32

		this.mBitmapTextureAtlas.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));
		// this.mScene.setOnSceneTouchListener(this);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,
				SensorManager.GRAVITY_EARTH), false);

		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();

		this.mBigAsset = new Sprite(0, CAMERA_HEIGHT
				- mBigAssetTextureRegion.getHeight(),
				this.mBigAssetTextureRegion, vertexBufferObjectManager);
		this.mScene.attachChild(mBigAsset);

		final PhysicsEditorLoader loader = new PhysicsEditorLoader();
		try {
			loader.load(this, mPhysicsWorld, "simple_asset.xml", mBigAsset,
					false, false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		final AnimatedSprite face;
		final Body body;
		face = new AnimatedSprite(300, 0, this.mCircleFaceTextureRegion,
				vertexBufferObjectManager);
		mCamera.setChaseEntity(face);

		body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face,
				BodyType.DynamicBody, FIXTURE_DEF);

		this.mScene.attachChild(face);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face,
				body, true, true));

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		return this.mScene;
	}

}