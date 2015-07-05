package com.caberodev.squarearmy.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.caberodev.squarearmy.DrawEngine;
import com.caberodev.squarearmy.Drawer;
import com.caberodev.squarearmy.LogicEngine;
import com.caberodev.squarearmy.Thinker;
import com.caberodev.squarearmy.behavior.BehaviorMinionFollowHero;
import com.caberodev.squarearmy.util.Color;
import com.caberodev.squarearmy.util.RandomData;

/**
 * 
 * @author Javier Cabero Guerra <br>
 * 
 * Copyright 2015 (c) All Rights Reserved. <br><br>
 *        
 *        
 * World<br>
 * 
 * 	This is one of the most important classes, containing almost ALL the
 * logic in this game.
 * 
 * The information about minions and heroes is stored here.
 */
public class World implements Thinker, Drawer {

	// Constants
	public static final float CUT_DISTANCE   = 768f; /* To cut some calculus */
	private static final Double WORLD_SIZE   = (double) (Gdx.graphics.getWidth() * 2);
	private static final int MAX_NUM_HEROES  = 5;
	private final int MINIONS_MAX_X_DISTANCE = Gdx.graphics.getWidth();
	private final int MINIONS_MAX_Y_DISTANCE = Gdx.graphics.getHeight();
	private final int MAX_NUM_MINIONS 	     = 350;
	private final int HEROES_RESPAWN_TIME    = 128;
	private final int INITIAL_MINIONS 	     = 50;
	
	// Heroes
	private List<Hero>          heroes = new ArrayList<Hero>();
	private Set<Hero>       deadHeroes = new HashSet<Hero>();

	// Minions
	private int numMinions = 0;
	private Set<Minion>        minions = new HashSet<Minion>();
	private Set<Minion> neutralMinions = new HashSet<Minion>();
	private Set<Minion>    deadMinions = new HashSet<Minion>();
	
	private Hero player;
	private int numHeroes = 0;
	private int nextHeroSpawn = HEROES_RESPAWN_TIME + RandomData.nextInt(HEROES_RESPAWN_TIME);
	
	private Color[] colors = new Color[]{Color.BLUE, Color.RED, Color.CYAN, Color.GRAY, Color.GREEN, Color.WHITE, Color.YELLOW};
//	private final Double alpha   = Math.asin(Gdx.graphics.getHeight() / Math.sqrt((Gdx.graphics.getWidth()  * Gdx.graphics.getWidth()) + 
//			                		       									      (Gdx.graphics.getHeight() * Gdx.graphics.getHeight())));

	private ShapeRenderer barRenderer = new ShapeRenderer();
	
	public World() {

		// Create neutral Minions 
		for (int i = 1; i < INITIAL_MINIONS; i++) {
			Minion m = new Minion(this, RandomData.nextInt(MINIONS_MAX_X_DISTANCE), RandomData.nextInt(MINIONS_MAX_Y_DISTANCE));
			minions.add(m);
			/* Redundant list to optimize computation */
			neutralMinions.add(m);
			numMinions++;
		}

		/* Create Hero */
		player = new Hero(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		player.setWorld(this);
		player.setPlayer(true);

		// //Create an red
		// Hero redEnemy = new Hero((Gdx.graphics.getWidth() * 4) / 5,
		// Gdx.graphics.getHeight() / 2);
		// redEnemy.setWorld(this);
		// redEnemy.setColor(EntityColor.RED);
		//
		// //Create an green
		// Hero greenEnemy = new Hero((Gdx.graphics.getWidth() * 2) / 5,
		// Gdx.graphics.getHeight() / 2);
		// greenEnemy.setWorld(this);
		// greenEnemy.setColor(EntityColor.GREEN);
		//
		// //Create orange
		// Hero cianEnemy = new Hero((Gdx.graphics.getWidth() * 3) / 6,
		// Gdx.graphics.getHeight() / 2);
		// cianEnemy.setWorld(this);
		// cianEnemy.setColor(EntityColor.CIAN);

		/* Add both to the list of heroes */
		heroes.add(player);
		// heroes.add(redEnemy);
		// heroes.add(greenEnemy);
		// heroes.add(cianEnemy);
		numHeroes = heroes.size();
		
		DrawEngine.addDrawable(this);
		LogicEngine.addThinker(this);
	}

	public void think(float delta) {

		/* Update heroes */
		for (Hero h : heroes) {
			h.think(delta);
		}
		
		/* Update minions */
		for (Minion m : minions) {
			m.think(delta);
		}

		if (numMinions < MAX_NUM_MINIONS) {
			int x, y;
			int i = RandomData.nextInt(MINIONS_MAX_X_DISTANCE);
			int j = RandomData.nextInt(MINIONS_MAX_Y_DISTANCE);
			if (i % 2 == 0) {
				x = (int) player.x + Gdx.graphics.getWidth() / 2 + i;
				if (j % 2 == 0) {
					y = (int) player.y + Gdx.graphics.getHeight() / 2 + j;
				} else {
					y = (int) player.y - Gdx.graphics.getHeight() / 2 - j;
				}
			} else {
				x = (int) player.x - Gdx.graphics.getWidth() / 2 - i;
				if (j % 2 == 0) {
					y = (int) player.y + Gdx.graphics.getHeight() / 2 + j;
				} else {
					y = (int) player.y - Gdx.graphics.getHeight() / 2 - j;
				}
			}
			Minion m = new Minion(this, x, y);
			neutralMinions.add(m);
			minions.add(m);
			numMinions++;
		}

		for (Minion m : minions) {

			float xDistance = m.x - player.x;
			float yDistance = m.y - player.y;

			Double realDistance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);

			if (realDistance > WORLD_SIZE) {
				addDeadMinion(m);
			}
		}

		if (numHeroes < MAX_NUM_HEROES && nextHeroSpawn <= 0) {
			int x, y;
			int i = RandomData.nextInt(MINIONS_MAX_X_DISTANCE);
			int j = RandomData.nextInt(MINIONS_MAX_Y_DISTANCE);
			if (i % 2 == 0) {
				x = (int) player.x + Gdx.graphics.getWidth() / 2 + i;
				if (j % 2 == 0) {
					y = (int) player.y + Gdx.graphics.getHeight() / 2 + j;
				} else {
					y = (int) player.y - Gdx.graphics.getHeight() / 2 - j;
				}
			} else {
				x = (int) player.x - Gdx.graphics.getWidth() / 2 - i;
				if (j % 2 == 0) {
					y = (int) player.y + Gdx.graphics.getHeight() / 2 + j;
				} else {
					y = (int) player.y - Gdx.graphics.getHeight() / 2 - j;
				}
			}
			Hero h = new Hero(x, y);
			heroes.add(h);
			h.setWorld(this);

			boolean colorSelected = false;
			int colorIndex = RandomData.nextInt(colors.length);
			Color color = Color.GRAY;
			Set<Color> colorsUsed = getColorsUsed();
			while (!colorSelected) {
				if (!colors[colorIndex].equals(Color.GRAY) && !colors[colorIndex].equals(Color.WHITE)
						&& !colorsUsed.contains(colors[colorIndex])) {
					color = colors[colorIndex];
					colorSelected = true;
				}
				colorIndex = (colorIndex + 1) % colors.length;
			}
			h.setColor(color);

			numHeroes++; 
			
			nextHeroSpawn = HEROES_RESPAWN_TIME + RandomData.nextInt(HEROES_RESPAWN_TIME);
		} else {
			nextHeroSpawn--;
		}

		for (Hero h : heroes) {
			float xDistance = h.x - player.x;
			float yDistance = h.y - player.y;

			Double realDistance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);

			if (realDistance > WORLD_SIZE) {
				addDeadHero(h);
			}
		}
		removeDeadMinions();
		removeDeadHeroes();
	}

	private Set<Color> getColorsUsed() {
		Set<Color> colorsUsed = new HashSet<Color>();

		for (Hero h : heroes) {
			colorsUsed.add(h.getColor());
		}
		return colorsUsed;
	}

	private void removeDeadHeroes() {
		for (Hero h : deadHeroes) {
			heroes.remove(h);
			if(h.equals(player)){
				// TODO: boot.setState(2);
			}
			h.freeMinions();
			numHeroes--;
		}

		deadHeroes = new HashSet<Hero>();
	}

	public void draw() {

		/* Render heroes */
		for (Hero h : heroes) {
			h.draw();
		}

		/* Render minions */
		for (Minion m : minions) {
			m.draw();
		}

		renderArmiesLength();
		renderHeroesHealth();
//		renderEnemyLocatorArrow();
	}

//	@SuppressWarnings("unused")
//	private void renderEnemyLocatorArrow() {
//
//		for (Hero h : heroes) {
//
//			if (!getScreenRectangle().contains((int) h.x, (int) h.y)) {
//				/* Calculate arrow place */
//				float x = ((float) Gdx.graphics.getWidth() / 2) - offsetX;
//				float y = ((float) Gdx.graphics.getHeight() / 2) - offsetY;
//				float x1 = 10, y1 = 10, x2 = 100, y2 = 10, x3 = 10, y3 = 100;
//
//				float diffX = h.x - x;
//				float diffY = h.y - y;
//
//				if (diffX > 0) {
//					if (diffY > 0) {
//						/* Top right */
//						if (Math.asin(diffY / Math.sqrt((diffX * diffX) + (diffY * diffY))) < alpha) {
//							/* key point */
//							y1 = Gdx.graphics.getHeight() / 2 - offsetY + diffY;
//							x1 = Gdx.graphics.getWidth() - 10 - offsetX;
//
//							x2 = x1 - 10;
//							y2 = y1 + 10;
//
//							x3 = x2;
//							y3 = y1 - 10;
//
//						} else {
//							/* key point */
//							y1 = Gdx.graphics.getHeight() - offsetY - 10;
//							x1 = Gdx.graphics.getWidth() / 2 - offsetX + diffX;
//
//							x2 = x1 - 10;
//							y2 = y1 - 10;
//
//							x3 = x1 + 10;
//							y3 = y2;
//						}
//					} else {
//						/* Bottom right */
//
//						if (Math.asin(diffY / Math.sqrt((diffX * diffX) + (diffY * diffY))) > -alpha + (1 / 2)) {
//							/* key point */
//							y1 = Gdx.graphics.getHeight() / 2 - offsetY + diffY;
//							x1 = Gdx.graphics.getWidth() - 10 - offsetX;
//
//							x2 = x1 - 10;
//							y2 = y1 + 10;
//
//							x3 = x2;
//							y3 = y1 - 10;
//
//						} else {
//							/* key point */
//							y1 = -offsetY + 10;
//							x1 = Gdx.graphics.getWidth() / 2 - offsetX + diffX;
//							x2 = x1 - 10;
//							y2 = y1 + 10;
//
//							x3 = x1 + 10;
//							y3 = y2;
//						}
//					}
//				} else {
//					if (diffY > 0) {
//						/* Top left */
//						if (Math.asin(diffY / Math.sqrt((diffX * diffX) + (diffY * diffY))) < alpha + (1 / 2)) {
//							/* key point */
//							y1 = Gdx.graphics.getHeight() / 2 - offsetY + diffY;
//							x1 = 10 - offsetX;
//
//							x2 = x1 + 10;
//							y2 = y1 + 10;
//
//							x3 = x2;
//							y3 = y1 - 10;
//
//						} else {
//							/* key point */
//							y1 = Gdx.graphics.getHeight() - offsetY - 10;
//							x1 = Gdx.graphics.getWidth() / 2 - offsetX + diffX;
//
//							x2 = x1 - 10;
//							y2 = y1 - 10;
//
//							x3 = x1 + 10;
//							y3 = y2;
//						}
//					} else {
//						/* Bottom Left */
//
//						if (Math.asin(diffY / Math.sqrt((diffX * diffX) + (diffY * diffY))) > -alpha + (1 / 2)) {
//							/* key point */
//							y1 = Gdx.graphics.getHeight() / 2 - offsetY + diffY;
//							x1 = 10 - offsetX;
//
//							x2 = x1 + 10;
//							y2 = y1 + 10;
//
//							x3 = x2;
//							y3 = y1 - 10;
//
//						} else {
//							/* key point */
//							y1 = -offsetY + 10;
//							x1 = Gdx.graphics.getWidth() / 2 - offsetX + diffX;
//							x2 = x1 - 10;
//							y2 = y1 + 10;
//
//							x3 = x1 + 10;
//							y3 = y2;
//						}
//					}
//				}
//
//				/* TODO: Render arrow 
//				EntityColor color = h.getColor();
//				glColor3f(color.red, color.green, color.blue);
//
//				glBegin(GL_TRIANGLES);
//				glVertex2f(x1, y1);
//				glVertex2f(x2, y2);
//				glVertex2f(x3, y3);
//				glEnd();
//				*/
//			}
//		}
//
//	}

	float health_bar_scale = 3;
	float army_bar_scale = 1;
	
	private void renderHeroesHealth() {
		// All bars will be rendered on top left 
		int numHero = 1;
		
		for (Hero hero : heroes) {
			// Get info 
			Color color = hero.getColor();

			// Render bar
			barRenderer.setColor(color.r, color.g, color.b, color.a);
			barRenderer.begin(ShapeType.Filled);
			barRenderer.rect(15, numHero * 15, hero.getHealth() * health_bar_scale, 10);
			barRenderer.end();
			numHero++;
		}
	}

	private void renderArmiesLength() {
		// All bars will be rendered on top left 
		int numHero = 1;
		for (Hero hero : heroes) {
			// Get info 
			int numMinions = 1 + hero.getNumMinions();
			Color color = hero.getColor();

			// Render bar 
			barRenderer.setColor(color.r, color.g, color.b, color.a);
			barRenderer.begin(ShapeType.Filled);
			barRenderer.rect(Gdx.graphics.getWidth() - 15, numHero * 15, -numMinions * health_bar_scale, 10);
			barRenderer.end();
			numHero++;
		}
	}
	
	
	private void removeDeadMinions() {
		for (Minion m : deadMinions) {
			removeMinion(m);
			numMinions--;
		}

		deadMinions = new HashSet<Minion>();
	}

	public void setHeroes(List<Hero> heroes) {
		this.heroes = heroes;
	}

	public List<Hero> getHeroes() {
		return heroes;
	}

	public void addDeadMinion(Minion m) {
		deadMinions.add(m);
	}

	public void setMinions(Set<Minion> minions) {
		this.minions = minions;
	}

	public List<Hero> getHeroes(List<Hero> heroes) {
		return heroes;
	}

	public Set<Minion> getMinions(Set<Minion> minions) {
		return minions;
	}

	/* Heroes will call for minions for their army */
	public void requestMinionsFor(Hero hero) {

		Set<Minion> minionsCaptured = new HashSet<Minion>();

		for (Minion m : neutralMinions) {

			float xDistance = m.x - hero.x;
			float yDistance = m.y - hero.y;

			Double realDistance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);

			/* If close enough */
			if (realDistance <= hero.getMinionsCallDistance()) {

				/* Minion captured */
				m.setBehavior(new BehaviorMinionFollowHero(hero, m));
				minionsCaptured.add(m);
				hero.addMinion(m);
			}
		}

		neutralMinions.removeAll(minionsCaptured);
	}

	public void freeMinion(Minion m) {
		neutralMinions.add(m);
	}

	public Set<Minion> getMinions() {
		return minions;
	}

	public void removeMinion(Minion m) {
		/* We don't know where the minion is so we remove it from everywhere */

		minions.remove(m);

		for (Hero h : heroes) {
			h.removeMinion(m);
		}
	}

	public Set<Minion> getDeadMinions() {
		return deadMinions;
	}

	public void setDeadMinions(Set<Minion> deadMinions) {
		this.deadMinions = deadMinions;
	}

	public void addDeadHero(Hero h) {
		deadHeroes.add(h);
	}

	public Set<Minion> getNeutralMinions() {
		return neutralMinions;
	}
}
