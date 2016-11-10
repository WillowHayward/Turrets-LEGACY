package uq.deco2800.coaster.graphics.screens;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uq.deco2800.coaster.game.entities.Entity;
import uq.deco2800.coaster.game.entities.Player;
import uq.deco2800.coaster.game.world.*;
import uq.deco2800.coaster.graphics.Viewport;
import uq.deco2800.coaster.graphics.sprites.Sprite;
import java.util.List;


public class GameScreen extends Screen {
	private Canvas canvas;
	private GraphicsContext gc;
	private Viewport viewport;
	Logger logger = LoggerFactory.getLogger(GameScreen.class);
	
	int lastLeft = 0;
	int lastTop = 0;
	int lastRight = 0;
	int lastBottom = 0;
	float firstTileSize = 0;
	boolean destructionShadowUpdate = false;
	WritableImage shadowMap;


	public GameScreen(Viewport viewport, Canvas canvas) {
		super(canvas);
		this.viewport = viewport;
		this.setVisible(true);
		this.canvas = canvas;
		gc = canvas.getGraphicsContext2D();
	}

	@Override
	public void setWidth(int newWidth) {
		canvas.setWidth(newWidth);
	}

	@Override
	public void setHeight(int newHeight) {
		canvas.setHeight(newHeight);
	}


	public void render(long ms, boolean renderBackground) {
		World.getInstance();
		
		//Background
		renderBackground(ms);
		
		//Render tiles
		renderTerrain(World.getInstance().getTiles());
	
		//Render entities
		renderEntities(World.getInstance().getAllEntities(), ms);
	}
	
	private void renderBackground(long ms) {
		gc.setFill(Color.GRAY);
		gc.fillRect(0, 0, viewport.getResWidth(), viewport.getResHeight());
	}
	
	private void renderTerrain(WorldTiles worldTiles) {

		float tileSize = viewport.getTileSideLength();
		int leftBorder = viewport.getLeftBorder();
		int topBorder = viewport.getTopBorder();

		int left = (int) Math.floor(viewport.getLeft());
		int top = (int) Math.floor(viewport.getTop());

		float subTileShiftX = (viewport.getLeft() - left) * tileSize;
		float subTileShiftY = (viewport.getTop() - top) * tileSize;
		if (firstTileSize == 0) {
			firstTileSize = tileSize;
		}

		//display sprites and light level
		for (int x = 0; x <= Chunk.CHUNK_WIDTH; x++) {
			for (int y = 0; y <= Chunk.CHUNK_HEIGHT; y++) {
				//We still want to iterate over "negative" tiles even if we don't render so we can center the map
				boolean invalidTile = false;
				if (!worldTiles.test(x, y)) {
					invalidTile = true;
				}

				if (!invalidTile) {
					Sprite sprite = worldTiles.get(x, y).getSprite();
					float xPos = (x - left) * tileSize + leftBorder;
					float yPos = (y - top) * tileSize + topBorder;
					gc.drawImage(sprite.getFrame(), xPos - subTileShiftX, yPos - subTileShiftY, tileSize, tileSize);
				}
			}
		}
	}

	private void renderEntities(List<Entity> entities, long ms) {
		float widthLeeway = viewport.getWidth() / 2; // Leeway to make sure that no sprites are cut off
		float heightLeeway = viewport.getHeight() / 2; // Leeway to make sure that no sprites are cut off
		float left = viewport.getLeft() - widthLeeway;
		float right = viewport.getRight() + widthLeeway;
		float top = viewport.getTop() - heightLeeway;
		float bottom = viewport.getBottom() + heightLeeway;
		Player player = null;
		for (Entity entity : entities) {
			if (entity instanceof Player) {
				player = (Player) entity;
			} else {
				if (entity.getX() + entity.getWidth() > left && entity.getX() < right
						&& entity.getY() + entity.getHeight() > top && entity.getY() < bottom) {
					entity.render(gc, viewport, ms);
				}
			}
		}
		if (player != null) { //Render player in front of all entities
			player.render(gc, viewport, ms);
		}
	}
}
